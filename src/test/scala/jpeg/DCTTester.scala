package jpeg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.beans.beancontext.BeanContextChildSupport

object DCTDataChisel {
      val inputMatrix = Vec(
        Vec( 62.S, 55.S, 55.S, 54.S, 49.S, 48.S, 47.S, 55.S),
        Vec( 62.S, 57.S, 54.S, 52.S, 48.S, 47.S, 48.S, 53.S),
        Vec( 61.S, 60.S, 52.S, 49.S, 48.S, 47.S, 49.S, 54.S),
        Vec( 63.S, 61.S, 60.S, 60.S, 63.S, 65.S, 68.S, 65.S),
        Vec( 67.S, 67.S, 70.S, 74.S, 79.S, 85.S, 91.S, 92.S),
        Vec( 82.S, 95.S, 101.S, 106.S, 114.S, 115.S, 112.S, 117.S),
        Vec( 96.S, 111.S, 115.S, 119.S, 128.S, 128.S, 130.S, 127.S),
        Vec( 109.S, 121.S, 127.S, 133.S, 139.S, 141.S, 140.S, 133.S))

      val shifted = Vec(
        Vec(-66.S, -73.S, -73.S, -74.S, -79.S, -80.S, -81.S, -73.S),
        Vec(-66.S, -71.S, -74.S, -76.S, -80.S, -81.S, -80.S, -75.S),
        Vec(-67.S, -68.S, -76.S, -79.S, -80.S, -81.S, -79.S, -74.S),
        Vec(-65.S, -67.S, -68.S, -68.S, -65.S, -63.S, -60.S, -63.S),
        Vec(-61.S, -61.S, -58.S, -54.S, -49.S, -43.S, -37.S, -36.S),
        Vec(-46.S, -33.S, -27.S, -22.S, -14.S, -13.S, -16.S, -11.S),
        Vec(-32.S, -17.S, -13.S, -9.S, 0.S, 0.S, 2.S, -1.S),
        Vec(-19.S, -7.S, -1.S, 5.S, 11.S, 13.S, 12.S, 5.S))

      val dctOutput = Vec(
        Vec(-369.63, -29.67, -2.64, -2.47, -1.13, -3.71, -1.48, -0.08),
        Vec(-231.08, 44.92, 24.49, -0.27, 9.3, 3.91, 4.29, -1.35),
        Vec(62.85, 8.53, -7.58, -2.66, 0.32, -0.41, 0.51, -0.83),
        Vec(12.5, -14.61, -3.48, -3.44, 2.43, -1.33, 2.72, -0.38),
        Vec(-4.88, -3.86, 0.87, 3.56, 0.13, 5.12, 1.13, 0.48),
        Vec(-0.48, 3.19, -1.43, 0.2, -1.06, -1.48, -1.13, 0.9),
        Vec(4.41, 2.28, -1.74, -1.57, 1.09, -2.74, 1.08, -1.41),
        Vec(-10.19, -1.82, 5.91, -0.42, 0.3, 0.42, -0.98, 0.0))
}

class DCTTester extends AnyFlatSpec with ChiselScalatestTester {
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
}
