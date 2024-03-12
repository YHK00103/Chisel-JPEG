package jpeg

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._
import java.rmi.dgc.DGC
import scala.math.round
import chisel3.experimental._
import scala.math.cos
import scala.math.Pi 

object DCTState extends ChiselEnum { 
    val loading, shifting, calculating, waiting = Value 
}

class DCTChisel extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new Bundle {
            val matrixIn = Input(Vec(8, Vec(8, SInt(9.W))))
        }))
        val shiftedOut = Output(Vec(8, Vec(8, SInt(9.W)))) // Test output to check shiftedblock
        val dctOut = Valid(Vec(8, Vec(8, SInt(16.W))))//FixedPoint(16.W, 8.BP))))
    })

    val matrixInput  = Reg(Vec(8, Vec(8, SInt(9.W))))
    val shiftedBlock = Reg(Vec(8, Vec(8, SInt(9.W))))
    val matrixOutput = Reg(Vec(8, Vec(8, SInt(16.W))))//FixedPoint(16.W, 8.BP)))) //SInt(9.W))))//
    val readyIn   = RegInit(true.B) 
    val validOut  = RegInit(false.B)


    io.in.ready  := readyIn
    io.dctOut.valid := validOut

    io.dctOut.bits := matrixOutput
    io.shiftedOut := DontCare

    def DCT(matrix: Vec[Vec[SInt]]): Vec[Vec[SInt]] = {
        val dctMatrix = Wire(Vec(8, Vec(8, SInt(16.W))))

        // Compute DCT
        for (u <- 0 until 8) {
            for (v <- 0 until 8) {
                var sum = 0.S
                for (i <- 0 until 8) {
                    for (j <- 0 until 8) {
                        val pixelValue = matrix(i)(j)
                        // Scale the cosine values to preserve precision
                        val cosVal = (math.cos((2 * i + 1) * u * Pi / 16) * math.cos((2 * j + 1) * v * Pi / 16) * 100).toInt.S

                        sum = sum + pixelValue * cosVal
                    }
                }
                val alphaU = if (u == 0) 1 else math.sqrt(2) / 2
                val alphaV = if (v == 0) 1 else math.sqrt(2) / 2
                val scaledSum = (alphaU.toInt.S * alphaV.toInt.S * sum / 4.S)
                dctMatrix(u)(v) := scaledSum
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

    } 

}

