package jpeg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.beans.beancontext.BeanContextChildSupport


object ZigZagInput {
    val inputMatrix = Seq(
        Seq(1, 2, 3, 4, 5, 6, 7, 8),
        Seq(9, 10, 11, 12, 13, 14, 15, 16),
        Seq(17, 18, 19, 20, 21, 22, 23, 24),
        Seq(25, 26, 27, 28, 29, 30, 31, 32),
        Seq(33, 34, 35, 36, 37, 38, 39, 40),
        Seq(41, 42, 43, 44, 45, 46, 47, 48),
        Seq(49, 50, 51, 52, 53, 54, 55, 56),
        Seq(57, 58, 59, 60, 61, 62, 63, 64)
    )

    val expectedOutput = Seq(
        1, 9, 17, 10, 3, 4, 11, 18, 25, 33, 
        26, 19, 12

    )

    val in8x8 = Seq(
        Seq(10, 11, 12, 13, 14, 15, 16, 17),
        Seq(18, 19, 20, 21, 22, 23, 24, 25),
        Seq(26, 27, 28, 29, 30, 31, 32, 33),
        Seq(34, 35, 36, 37, 38, 39, 40, 41),
        Seq(42, 43, 44, 45, 46, 47, 48, 49),
        Seq(50, 51, 52, 53, 54, 55, 56, 57),
        Seq(58, 59, 60, 61, 62, 63, 64, 65),
        Seq(66, 67, 68, 69, 70, 71, 72, 73)
    )

    val out8x8 = Seq(10, 11, 18, 26, 19, 12, 
        13, 20, 27, 34, 42, 35, 28, 21, 14, 15, 
        22, 29, 36, 43, 50, 58, 51, 44, 37, 30, 
        23, 16, 17, 24, 31, 38, 45, 52, 59, 66, 
        67, 60, 53, 46, 39, 32, 25, 33, 40, 47, 
        54, 61, 68, 69, 62, 55, 48, 41, 49, 56, 
        63, 70, 71, 64, 57, 65, 72, 73
    )

}


class ZigZagChiselTester extends AnyFlatSpec with ChiselScalatestTester {
    it should "perform zigzag on matrix" in {
        test(new ZigZagChisel()) { dut =>
            // input the matrix
            dut.io.in.valid.poke(true.B)
            // for (row <- ZigZagInput.inputMatrix) {
            //     for (element <- row) {
            //         dut.io.in.bits.matrixIn.zipWithIndex.foreach { case (row, index) =>
            //             row(index).poke(element)
            //         }
            //     }
            // }

            for (i <- 0 until 8) {
                for (j <- 0 until 8) {
                    dut.io.in.bits.matrixIn(i)(j).poke(ZigZagInput.in8x8(i)(j))
                }
            }
            
            dut.clock.step() // Load in the matrix 
            dut.io.in.valid.poke(false.B)
            dut.clock.step(64) // Process zigzag

            // Check if the output matches expected 
            dut.io.zigzagOut.valid.expect(true.B)
            dut.io.in.ready.expect(true.B)
            for (i <- 0 to 63) {
                dut.io.zigzagOut.bits(i).expect(ZigZagInput.out8x8(i).S)
            }
            dut.clock.step() // back to idle
        }
    }
}

