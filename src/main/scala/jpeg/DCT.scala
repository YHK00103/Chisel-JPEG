package jpeg

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._
import java.rmi.dgc.DGC
import chisel3.experimental._
import scala.math.cos
// import scala.math.Pi 

class DCTChisel extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new Bundle {
            val matrixIn = Input(Vec(8, Vec(8, SInt(9.W))))
        }))
        val shiftedOut = Output(Vec(8, Vec(8, SInt(9.W))))
        val dctOut = Valid(Vec(8, Vec(8, FixedPoint(16.W, 8.BP))))
    })

    object DCTState extends ChiselEnum { val loading, shifting, calculating, waiting = Value }
    val matrixInput  = Reg(Vec(8, Vec(8, SInt(9.W))))
    val shiftedBlock = Reg(Vec(8, Vec(8, SInt(9.W))))
    val matrixOutput = Reg(Vec(8, Vec(8, FixedPoint(16.W, 8.BP))))
    val readyIn   = RegInit(true.B) 
    val validOut  = RegInit(false.B)

    

    io.in.ready  := readyIn
    io.dctOut.valid := validOut

    io.dctOut.bits := matrixOutput

    def DCT(matrix: Vec[Vec[SInt]]): Vec[Vec[FixedPoint]] = {
        val dctMatrix = Wire(Vec(8, Vec(8, FixedPoint(16.W, 8.BP))))
        val Pi1 = scala.math.Pi.toDouble //F(16.BP)

        // for (u <- 0 until 8) {
        //     for (v <- 0 until 8) {
        //         val sum = (0 until 8).foldLeft(0.0.F(16.BP)) { (accI, i) =>
        //             (0 until 8).foldLeft(accI) { (accJ, j) =>
        //                 val pixelValue = matrix(i)(j).asFixedPoint(8.BP)
        //                 val tempSum = accJ + pixelValue * cos((2 * i + 1) * u * Pi.F(16.BP) / 16) * cos((2 * j + 1) * v * Pi.F(16.BP) / 16)
        //                 tempSum
        //             }
        //         }
        //         val alphaU = if (u == 0) 1.0.F(16.BP) else math.sqrt(2).F(16.BP) / 2.0.F(16.BP)
        //         val alphaV = if (v == 0) 1.0.F(16.BP) else math.sqrt(2).F(16.BP) / 2.0.F(16.BP)
        //         dctMatrix(u)(v) := (alphaU * alphaV * sum / 4.0.F(16.BP))//.toDouble
        //     }
        // }
        // dctMatrix
        for (u <- 0 until 8) {
            for (v <- 0 until 8) {
                var sum = 0.F(16.BP)
                for (i <- 0 until 8) {
                    for (j <- 0 until 8) {
                        val pixelValue = matrix(i)(j).asFixedPoint(8.BP)
                        val cosI = cos((2 * i + 1).toDouble * u.toDouble * Pi1 / 16.0)
                        val cosJ = cos((2 * j + 1).toDouble * v.toDouble * Pi1 / 16.0)
                        sum = sum + pixelValue * cosI * cosJ
                    }
                }
                val alphaU = if (u == 0) 1.0.F(16.BP) else 1.0.F(16.BP) / math.sqrt(2).F(16.BP)
                val alphaV = if (v == 0) 1.0.F(16.BP) else 1.0.F(16.BP) / math.sqrt(2).F(16.BP)
                dctMatrix(u)(v) := (alphaU * alphaV * sum / 4.0.F(16.BP))
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