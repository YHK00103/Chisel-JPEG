package jpeg

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._
import java.rmi.dgc.DGC
import scala.math.round
// import chisel3.experimental._
// import scala.math.cos
// import scala.math.Pi 


class CosineLUT(val period: Int, val amplitude: Int) {
  require(period > 0)

  private val B: Double = (2.0 * math.Pi) / period.toDouble

  def apply(index: Int): Int = (amplitude.toDouble * math.cos(B * index)).toInt

  def printTable(): Unit = {
    println("cosine lookup table:")
    for (i <- 0 until period) {
      val value = apply(i)
      println(s"Index $i: $value")
    }
  }
}

class DCTChisel extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new Bundle {
            val matrixIn = Input(Vec(8, Vec(8, SInt(9.W))))
        }))
        val shiftedOut = Output(Vec(8, Vec(8, SInt(9.W))))
        val dctOut = Valid(Vec(8, Vec(8, SInt(9.W))))//FixedPoint(16.W, 8.BP))))
    })



    object DCTState extends ChiselEnum { val loading, shifting, calculating, waiting = Value }
    val matrixInput  = Reg(Vec(8, Vec(8, SInt(9.W))))
    val shiftedBlock = Reg(Vec(8, Vec(8, SInt(9.W))))
    val matrixOutput = Reg(Vec(8, Vec(8, SInt(9.W))))//FixedPoint(16.W, 8.BP))))
    val readyIn   = RegInit(true.B) 
    val validOut  = RegInit(false.B)


    io.in.ready  := readyIn
    io.dctOut.valid := validOut

    io.dctOut.bits := matrixOutput

    def DCT(matrix: Vec[Vec[SInt]]): Vec[Vec[SInt]] = {
        val dctMatrix = Wire(Vec(8, Vec(8, SInt(9.W)))) //FixedPoint(16.W, 8.BP))))
        // val Pi1 = scala.math.Pi.SInt//.toDouble //F(16.BP)

        val cosLUT = new CosineLUT(16, 1)
  
        for (u <- 0 until 8) {
            for (v <- 0 until 8) {
                var sum = 0.S//F(16.BP)
                for (i <- 0 until 8) {
                    for (j <- 0 until 8) {
                        val pixelValue = matrix(i)(j) //.toDouble //.asFixedPoint(8.BP)
                        //val cosI = cos((2 * i + 1).toDouble * u.toDouble * Pi1 / 16.0)
                        //val cosJ = cos((2 * j + 1).toDouble * v.toDouble * Pi1 / 16.0)
                        var lutIndexI = round((2 * i + 1) * u * (3 / 16)) //.toInt
                        var lutIndexJ = round((2 * j + 1) * v * (3 / 16)) //.toInt
                        val cosI = cosLUT(lutIndexI.toInt)
                        val cosJ = cosLUT(lutIndexJ.toInt)
                        
                        sum = sum + pixelValue.S * (cosI * cosJ).S//.toInt
                    }
                }
                val alphaU = if (u == 0) 1.S else 1.S/ math.sqrt(2)
                val alphaV = if (v == 0) 1.S else 1.S / math.sqrt(2)
                dctMatrix(u)(v) := (alphaU * alphaV * sum / 4.0.U)
            }
        }
        dctMatrix
    }





    val state = RegInit(DCTState.waiting)
    when(state === DCTState.waiting) {
        when (io.in.fire) {
            matrixInput := io.in.bits.matrixIn
            state := DCTState.shifting
            validOut := false.B
            readyIn := false.B
        }
    } .elsewhen (state === DCTState.shifting) {
        for (i <- 0 until 8) {
            for (j <- 0 until 8) {
                shiftedBlock(i)(j) := io.in.bits.matrixIn(i)(j) -& 128.S
            }
        }
        io.shiftedOut := shiftedBlock
        state := DCTState.calculating
    } .elsewhen (state === DCTState.calculating) {
       matrixOutput := DCT(shiftedBlock)
       state := DCTState.waiting
    } //.elsewhen (state === DCTState.waiting)

}