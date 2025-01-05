package jpeg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class HuffmanDcEncoderTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "HuffmanDcEncoder"

  it should "encode DC values correctly" in {
    test(new HuffmanDcEncoder) { dut =>
      def processValue(value: Int) = {
        dut.io.in.bits.value.poke(value.S)
        dut.io.in.bits.isLuminance.poke(true.B)
        dut.io.in.valid.poke(true.B)
        dut.clock.step(1)
        
        println(s"\nProcessing value: $value")
        println(s"Encoded bits: 0x${dut.io.out.bits.bits.peek().litValue.toString(16)}")
        println(s"Code length: ${dut.io.out.bits.length.peek().litValue}")
      }

      // 測試一系列值
      val testValues = Seq(0, 1, -1, 2, -2, 3, -3)
      testValues.foreach(processValue)
    }
  }
}