package jpeg

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class DeltaTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Delta Encoding"

  it should "correctly do delta encoding" in {
    test(new Delta).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        dut.io.in.valid.poke(true.B)
        dut.io.in.ready.expect(true.B)
        dut.io.state.expect(DeltaState.idle)
        val rand = scala.util.Random

        // Iterate through each element of the Vec
        for (i <- 0 until 64) {
            // Generate a random SInt value
            val randomSInt = rand.nextInt(64).S // Change 256 to your desired maximum value

            // Poke the random SInt into io.in.bits(i)
            dut.io.in.bits.data(i).poke(randomSInt)
        }
        // Run the clock
        dut.clock.step()

        dut.io.state.expect(DeltaState.encode)
        dut.io.in.ready.expect(false.B)
        dut.clock.step(64)
        dut.io.state.expect(DeltaState.idle)

    }
  }
}
