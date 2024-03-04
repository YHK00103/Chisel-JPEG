package jpeg

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._

class DCTChisel extends Module {
    val io = IO(new Bundle {
        val matrixIn = Input(Vec(8, Vec(8, SInt(9.W))))
        val dctOut = Output(Vec(8, Vec(8, SInt(9.W)))) //FixedPoint(16.W, 8.BP))))
    })

    val shiftedBlock = Reg(Vec(8, Vec(8, SInt(9.W))))

    for (i <- 0 until 8) {
        for (j <- 0 until 8) {
            shiftedBlock(i)(j) := io.matrixIn(i)(j) -& 128.S
        }
    }

    io.dctOut := shiftedBlock


    // def DCT(matrix: Vec[Vec[SInt]], width: Int, height: Int): Vec[Vec[FixedPoint]] = {
    //     val dctMatrix = Wire(Vec(8, Vec(8, FixedPoint(16.W, 8.BP))))

    //     for (u <- 0 until 8) {
    //         for (v <- 0 until 8) {
    //             val sum = (0 until 8).foldLeft(0.0.F(16.BP)) { (accI, i) =>
    //                 (0 until 8).foldLeft(accI) { (accJ, j) =>
    //                     val pixelValue = matrix(i)(j).asFixedPoint(8.BP)
    //                     val tempSum = accJ + pixelValue * cos((2 * i + 1) * u * Pi.F(16.BP) / 16) * cos((2 * j + 1) * v * Pi.F(16.BP) / 16)
    //                     tempSum
    //                 }
    //             }
    //             val alphaU = if (u == 0) 1.0.F(16.BP) else math.sqrt(2).F(16.BP) / 2.0.F(16.BP)
    //             val alphaV = if (v == 0) 1.0.F(16.BP) else math.sqrt(2).F(16.BP) / 2.0.F(16.BP)
    //             dctMatrix(u)(v) := (alphaU * alphaV * sum / 4.0.F(16.BP)).toDouble
    //         }
    //     }
    //     dctMatrix
    // }

    // io.dctOut := DCT(io.matrixIn, 8, 8)
}