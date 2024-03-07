package jpeg

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.language.experimental

// class decodeRLETest extends AnyFlatSpec with ChiselScalatestTester {
//     def doDecodeRLETest(data: Seq[Int]): Unit = {
//         test(new decodeRLE).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
//             dut.io.in.valid.poke(true.B)
//             dut.io.in.ready.expect(true.B)
//             dut.io.state.expect(DecodingState.idle)
//         }
//     }
//     behavior of "DecodeRLEChisel"
//     it should "decode 5:3, 6:2, 7:5, 8:4, 9:3, 10:2, 11:3, 12:5, 13:4, 14:4, 15:3, 16:5, 17:6, 18:7, 19:5, 20:3" in {
//         val test = Seq(
//             5, 5, 5, 6, 6, 7, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 10, 10, 11, 
//             11, 11, 12, 12, 12, 12, 12, 13, 13, 13, 13, 14, 14, 14, 14, 15, 
//             15, 15, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 18, 18, 18, 
//             18, 18, 18, 18, 19, 19, 19, 19, 19, 20, 20, 20
//         )
//         doDecodeRLETest(test)
//     }
// }

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
            val bits3Array: Vec[SInt] = dut.io.in.bits.data
            for (i <- 0 until bits3Array.size) {
                val element = bits3Array(i)
                println(element.peek())
            }
            println("---")
            dut.io.state.expect(DecodingState.decode)
            dut.io.in.ready.expect(false.B)
            dut.clock.step(64)

            val jpegEncoder = new jpegEncode(false, List.empty, 0)
            val expected = jpegEncoder.decodeDelta(data)


            // Printing each element of the array
            val bitsArray: Vec[SInt] = dut.io.out.bits
            for (element <- bitsArray) {
                println(element.peek())
            }

            for( i <- 0 until 64){
                dut.io.out.bits(i).expect(expected(i).S)
            }
            dut.io.state.expect(DecodingState.idle)
        }
    }
    behavior of "DecodeDeltaChisel"
    it should "decode 1 to 64" in {
        val test = Seq(
            1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        )
        doDecodeDeltaTest(test)
    }
}
