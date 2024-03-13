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
    Seq(-10, -2, 6, 0, 0, 0, -1, 0))

  val scaledOutput = Seq(
    Seq(-362232500, -29286250, -2604000, -2500750, -1102500, -3547250, -1452500, -42000),
    Seq(-228012750, 44592500, 24272500, -137500, 9225000, 3705000, 4165000, -1390000),
    Seq(61932500, 8460000, -7540000, -2665000, 312500, -417500, 527500, -842500),
    Seq(11880750, -14430000, -3492500, -3485000, 2417500, -1090000, 2705000, -335000),
    Seq(-4777500, -3807500, 867500, 3492500, 405000, 5115000, 1122500, 472500),
    Seq(465500, 3030000, -1430000, 390000, -1072500, -1622500, -1152500, 862500),
    Seq(4284000, 2217500, -1700000, -1535000, 1082500, -2735000, 1105000, -1415000),
    Seq(-9962750, -1812500, 5812500, -397500, 237500, 442500, -1052500, 2500))
}

class DCTTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "DCTChisel"
  it should "compute DCT correctly" in {
    test(new DCTChisel) { dut =>
      val inputMatrix = DCTDataChisel.inputMatrix
      val shiftedBlock = DCTDataChisel.shifted
      // val dctOut = DCTDataChisel.scaledOutput
      
      val jpegEncoder = new jpegEncode(false, List.empty, 0)
      val dctOut = jpegEncoder.DCT(DCTData.shifted)
      val convertedMatrix: Seq[Seq[SInt]] = dctOut.map(row => row.map(value => value.toInt.S))

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
          dut.io.dctOut.bits(i)(j).expect(convertedMatrix(i)(j))
        }
      }
    }
  }
  // it should "compute DCT correctly" in {
  //   test(new DCT) { dut =>
  //     val inputMatrix = Seq(
  //       Seq(139.S, 144.S, 149.S, 153.S, 155.S, 155.S, 155.S, 155.S),
  //       Seq(144.S, 151.S, 153.S, 156.S, 159.S, 156.S, 156.S, 156.S),
  //       Seq(150.S, 155.S, 160.S, 163.S, 158.S, 156.S, 156.S, 156.S),
  //       Seq(159.S, 161.S, 162.S, 160.S, 160.S, 159.S, 159.S, 159.S),
  //       Seq(159.S, 160.S, 161.S, 162.S, 162.S, 155.S, 155.S, 155.S),
  //       Seq(161.S, 161.S, 161.S, 161.S, 160.S, 157.S, 157.S, 157.S),
  //       Seq(162.S, 162.S, 161.S, 163.S, 162.S, 157.S, 157.S, 157.S),
  //       Seq(162.S, 162.S, 161.S, 161.S, 163.S, 158.S, 158.S, 158.S)
  //     )

  //     // Load input matrix
  //     for (i <- 0 until 8) {
  //       for (j <- 0 until 8) {
  //         dut.io.input(i)(j).poke(inputMatrix(i)(j))
  //       }
  //     }

  //     dut.clock.step()

  //     // Output matrix after DCT computation
  //     val expectedDCT = Seq(
  //       Seq(1837.S, 197.S, 150.S, -3.S, -28.S, -1.S, -3.S, 3.S),
  //       Seq(-89.S, -24.S, -3.S, 1.S, 2.S, -6.S, -1.S, -1.S),
  //       Seq(7.S, 6.S, 2.S, -5.S, -1.S, 1.S, -2.S, -1.S),
  //       Seq(-12.S, 1.S, -3.S, 3.S, 1.S, -3.S, 1.S, -2.S),
  //       Seq(-1.S, 3.S, -3.S, -1.S, -1.S, -2.S, 2.S, 1.S),
  //       Seq(0.S, -2.S, -1.S, -3.S, -1.S, -2.S, -2.S, -1.S),
  //       Seq(0.S, 2.S, -2.S, -1.S, -1.S, -1.S, -1.S, -1.S),
  //       Seq(0.S, 0.S, -1.S, 0.S, -1.S, 0.S, 0.S, -1.S)
  //     )

  //     // Compare DUT output with expected DCT
  //     for (i <- 0 until 8) {
  //       for (j <- 0 until 8) {
  //         dut.io.output(i)(j).expect(expectedDCT(i)(j))
  //       }
  //     }
  //   }
  // }


}
  // it should "dct Chisel test" in {
  //   test(new jpegEncodeChisel) { dut =>

  //     // Start the DUT
  //     dut.io.matrixIn.zip(DCTDataChisel.inputMatrix).foreach { case (ioRow, inputRow) =>
  //       ioRow.zip(inputRow).foreach { case (ioElem, inputElem) =>
  //         ioElem.poke(inputElem)
  //       }
  //     }

  //     // Wait a few cycles for computation
  //     dut.clock.step(10)

  //     // Read the DCT output
  //     val dctOutput = dut.io.dctOut.peek()

  //     // Print the DCT matrix
  //     println("DCT Output:")
  //     dctOutput.foreach { row =>
  //       println(row.map(_.peek().toString).mkString(" "))
  //     }
  //   }
  // }
// }

