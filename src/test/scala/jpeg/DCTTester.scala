package jpeg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.math.round


object DCTDataChisel {
      val inputMatrix = Seq(
        Seq(62.S, 55.S, 55.S, 54.S, 49.S, 48.S, 47.S, 55.S),
        Seq(62.S, 57.S, 54.S, 52.S, 48.S, 47.S, 48.S, 53.S),
        Seq(61.S, 60.S, 52.S, 49.S, 48.S, 47.S, 49.S, 54.S),
        Seq(63.S, 61.S, 60.S, 60.S, 63.S, 65.S, 68.S, 65.S),
        Seq(67.S, 67.S, 70.S, 74.S, 79.S, 85.S, 91.S, 92.S),
        Seq(82.S, 95.S, 101.S, 106.S, 114.S, 115.S, 112.S, 117.S),
        Seq(96.S, 111.S, 115.S, 119.S, 128.S, 128.S, 130.S, 127.S),
        Seq(109.S, 121.S, 127.S, 133.S, 139.S, 141.S, 140.S, 133.S))

      val shifted = Seq(
        Seq(-66.S, -73.S, -73.S, -74.S, -79.S, -80.S, -81.S, -73.S),
        Seq(-66.S, -71.S, -74.S, -76.S, -80.S, -81.S, -80.S, -75.S),
        Seq(-67.S, -68.S, -76.S, -79.S, -80.S, -81.S, -79.S, -74.S),
        Seq(-65.S, -67.S, -68.S, -68.S, -65.S, -63.S, -60.S, -63.S),
        Seq(-61.S, -61.S, -58.S, -54.S, -49.S, -43.S, -37.S, -36.S),
        Seq(-46.S, -33.S, -27.S, -22.S, -14.S, -13.S, -16.S, -11.S),
        Seq(-32.S, -17.S, -13.S, -9.S, 0.S, 0.S, 2.S, -1.S),
        Seq(-19.S, -7.S, -1.S, 5.S, 11.S, 13.S, 12.S, 5.S))

      val dctOutput = Seq(
        Seq(-369.63, -29.67, -2.64, -2.47, -1.13, -3.71, -1.48, -0.08),
        Seq(-231.08, 44.92, 24.49, -0.27, 9.3, 3.91, 4.29, -1.35),
        Seq(62.85, 8.53, -7.58, -2.66, 0.32, -0.41, 0.51, -0.83),
        Seq(12.5, -14.61, -3.48, -3.44, 2.43, -1.33, 2.72, -0.38),
        Seq(-4.88, -3.86, 0.87, 3.56, 0.13, 5.12, 1.13, 0.48),
        Seq(-0.48, 3.19, -1.43, 0.2, -1.06, -1.48, -1.13, 0.9),
        Seq(4.41, 2.28, -1.74, -1.57, 1.09, -2.74, 1.08, -1.41),
        Seq(-10.19, -1.82, 5.91, -0.42, 0.3, 0.42, -0.98, 0.0))

      val dctOutputRounded = Seq(
        Seq(-370, -30, -3, -2, -1, -4, -1, 0),
        Seq(-231, 45, 24, 0, 9, 4, 4, -1),
        Seq(63, 9, -8, -3, 0, 0, 1, -1),
        Seq(12, -15, -3, -3, 2, -1, 3, 0),
        Seq(-5, -4, 1, 4, 0, 5, 1, 0),
        Seq(0, 3, -1, 0, -1, -1, -1, 1),
        Seq(4, 2, -2, -2, 1, -3, 1, -1),
        Seq(-10, -2, 6, 0, 0, 0, -1, 0)
      )
}

class DCTTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "DCTChisel"
  it should "compute shifted block correctly" in {
    test(new DCTChisel) { dut =>
      val inputMatrix = DCTDataChisel.inputMatrix
      val shiftedBlock = DCTDataChisel.shifted
      val dctOut = DCTDataChisel.dctOutput

      dut.io.in.valid.poke(true.B)
      dut.io.in.ready.expect(true.B)
      // load in input matrix
      for (i <- 0 until 8) {
        for (j <- 0 until 8) {
          dut.io.in.bits.matrixIn(i)(j).poke(inputMatrix(i)(j))
        }
      }

      // Take step to load in matrix
      dut.clock.step()

      // Take step to load shifted block/go to calc state
      dut.clock.step()

      // Compare DUT output with expected out
      for (i <- 0 until 8) {
        for (j <- 0 until 8) {
          dut.io.shiftedOut(i)(j).expect(shiftedBlock(i)(j))
        }
      }

      // Take step to go to waiting/load calculation
      dut.clock.step()
      // Take step to go to waiting/load calculation
      dut.clock.step()
      dut.clock.step()
      for (i <- 0 until 8) {
        for (j <- 0 until 8) {
          dut.io.dctOut.bits(i)(j).expect(round(dctOut(i)(j)))
        }
      }
    }
  }

}


