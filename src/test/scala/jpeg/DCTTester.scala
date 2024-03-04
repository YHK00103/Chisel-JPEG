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


      )
    
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
