package jpeg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.beans.beancontext.BeanContextChildSupport

object DCTDataChisel {
      // val inputMatrix = Vec(
      //   Vec( 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S),
      //   Vec( 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S),
      //   Vec( 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S),
      //   Vec( 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S),
      //   Vec( 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S),
      //   Vec( 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S),
      //   Vec( 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S),
      //   Vec( 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S, 128.S))
    
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
