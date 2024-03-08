package jpeg

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.language.experimental

class decodeRLETest extends AnyFlatSpec with ChiselScalatestTester {
    def doDecodeRLETest(data: Seq[Int]): Unit = {
        test(new decodeRLE).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.io.in.valid.poke(true.B)
            dut.io.in.ready.expect(true.B)
            dut.io.state.expect(RLEDecodingState.idle)

            val length = data.length
            dut.io.in.bits.length.poke(length)
            for (i <- 0 until length) {
                dut.io.in.bits.data(i).poke(data(i).S)
            }
            dut.clock.step()
            dut.io.state.expect(RLEDecodingState.decode)
            dut.io.in.ready.expect(false.B)
            dut.clock.step(999)
            // Testing purposes
            // Printing each element of the array
            val bitsArray: Vec[SInt] = dut.io.out.bits
            for (element <- bitsArray) {
                println(element.peek())
            }
            dut.io.state.expect(RLEDecodingState.idle)
        }

    }
    behavior of "DecodeRLEChisel"
    it should "decode 5:3, 6:2, 7:5, 8:4, 9:3, 10:2, 11:3, 12:5, 13:4, 14:4, 15:3, 16:5, 17:6, 18:7, 19:5, 20:3" in {
        val test = Seq(1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, 1, 8, 1, 9, 1, 10, 1, 11, 1, 
        12, 1, 13, 1, 14, 1, 15, 1, 16, 1, 17, 1, 18, 1, 19, 1, 20, 1, 21, 
        1, 22, 1, 23, 1, 24, 1, 25, 1, 26, 1, 27, 1, 28, 1, 29, 1, 30, 1, 31, 
        1, 32, 1, 33, 1, 34, 1, 35, 1, 36, 1, 37, 1, 38, 1, 39, 1, 40, 1, 41, 
        1, 42, 1, 43, 1, 44, 1, 45, 1, 46, 1, 47, 1, 48, 1, 49, 1, 50, 1, 51, 
        1, 52, 1, 53, 1, 54, 1, 55, 1, 56, 1, 57, 1, 58, 1, 59, 1, 60, 1, 61, 
        1, 62, 1, 63, 1, 64)
        doDecodeRLETest(test)
    }
}

class DecodeDeltaTest extends AnyFlatSpec with ChiselScalatestTester {
    def doDecodeDeltaTest(data: Seq[Int]): Unit = {
        test(new decodeDelta).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.io.in.valid.poke(true.B)
            dut.io.in.ready.expect(true.B)
            dut.io.state.expect(DecodingState.idle)

            for (i <- 0 until 64) {
                dut.io.in.bits.data(i).poke(data(i).S)
            }

            dut.clock.step()
            dut.io.state.expect(DecodingState.decode)
            dut.io.in.ready.expect(false.B)
            dut.clock.step(64)

            val jpegEncoder = new jpegEncode(false, List.empty, 0)
            val expected = jpegEncoder.decodeDelta(data)

            // Testing purposes
            // Printing each element of the array
            // val bitsArray: Vec[SInt] = dut.io.out.bits
            // for (element <- bitsArray) {
            //     println(element.peek())
            // }

            for( i <- 0 until 64){
                dut.io.out.bits(i).expect(expected(i).S)
            }
            dut.io.state.expect(DecodingState.idle)
        }
    }
    behavior of "DecodeDeltaChisel"
    it should "decode 1 to 64" in {
        val test = Seq(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 
            19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 
            35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 
            51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64
        )
        doDecodeDeltaTest(test)
    }
    it should "decode 64 to 1" in {
        val test = Seq(
            64, 63, 62, 61, 60, 59, 58, 57, 56, 55, 54, 53, 52, 51, 50, 49,
            48, 47, 46, 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33,
            32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17,
            16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1
        )
        doDecodeDeltaTest(test)
    }

    it should "decode 64 of the same int(10 and 0)" in {
        val test1 = Seq.fill(64)(10)
        doDecodeDeltaTest(test1)
        val test2 = Seq.fill(64)(0)
        doDecodeDeltaTest(test2)
    }
}
