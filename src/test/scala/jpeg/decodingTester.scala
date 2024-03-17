package jpeg

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.language.experimental

/**
  * Class to hold RLE Decode Test function
  */
class RLEChiselDecode extends AnyFlatSpec with ChiselScalatestTester {
    /**
      * Performs RlE Decoding Tests
      *
      * @param data Data to Decode
      */
    def doDecodeRLETest(data: Seq[Int]): Unit = {
        test(new decodeRLE).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.io.in.valid.poke(true.B)
            dut.io.state.expect(RLEDecodingState.idle)

            val length = data.length
            // dut.io.in.bits.length.poke(length)
            for (i <- 0 until length) {
                dut.io.in.bits.data(i).poke(data(i).S)
            }
            dut.clock.step()
            dut.io.state.expect(RLEDecodingState.decode)
            dut.io.in.valid.poke(false.B)
            var sum = 0
            for (i <- 0 until length){
                if(i % 2 == 0){
                    sum += data(i)
                    dut.clock.step(data(i))
                }
            }
            // println(sum)
            dut.clock.step(7)

            dut.io.state.expect(RLEDecodingState.idle)
            // Testing purposes
            // Printing each element of the array
            // val bitsArray: Vec[SInt] = dut.io.out.bits
            // for (element <- bitsArray) {
            //     println(element.peek())
            // }
            // println("---")
            dut.io.state.expect(RLEDecodingState.idle)
        }

    }
    behavior of "RLEChiselDecode"
    it should "decode 4:1, 4:2, 4:3, 4:4, 4:5, 5:6" in {
        val test = Seq(4, 1, 4, 2, 4, 3, 4, 4, 4, 5, 5, 6)
        doDecodeRLETest(test)
    }
    it should "decode 3:1, 5:2, 2:3, 6:4, 1:5, 8:6" in {
        val test = Seq(3, 1, 5, 2, 2, 3, 6, 4, 1, 5, 8, 6)
        doDecodeRLETest(test)
    }

    // it should "decode 4:1, 4:2, 4:3, 4:4, 4:5, 5:6, 5:7, 3:8, 2:9, 5:10" in {
    //     val test = Seq(
    //         4, 1, 
    //         4, 2, 
    //         4, 3, 
    //         4, 4, 
    //         4, 5, 
    //         5, 6, 
    //         5, 7, 
    //         3, 8, 
    //         2, 9, 
    //         5, 10)
    //     doDecodeRLETest(test)
    // }
    // it should "decode 3:1, 5:2, 2:3, 6:4, 1:5, 8:6, 2:8, 1:10, 5:4, 7:3" in {
    //     val test = Seq(
    //         3, 1, 
    //         5, 2, 
    //         2, 3, 
    //         6, 4, 
    //         1, 5, 
    //         8, 6, 
    //         2, 8, 
    //         1, 10, 
    //         5, 4, 
    //         7, 3)
    //     doDecodeRLETest(test)
    // }

}

/**
  * Class to hold delta decoding test function
  */
class DeltaChiselDecode extends AnyFlatSpec with ChiselScalatestTester {
    /**
      * Performs Delta Decoding Tests
      *
      * @param data Data to decode
      */
    def doDecodeDeltaTest(data: Seq[Int]): Unit = {
        val p = JpegParams(8, 8, 0)
        test(new decodeDelta(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.io.in.valid.poke(true.B)
            dut.io.state.expect(DecodingState.idle)

            for (i <- 0 until p.totalElements) {
                dut.io.in.bits.data(i).poke(data(i).S)
            }

            dut.clock.step()
            dut.io.state.expect(DecodingState.decode)
            dut.io.in.valid.poke(false.B)
            dut.clock.step(p.totalElements)

            val jpegEncoder = new jpegEncode(false, List.empty, 0)
            val expected = jpegEncoder.decodeDelta(data)

            // Testing purposes
            // Printing each element of the array
            // val bitsArray: Vec[SInt] = dut.io.out.bits
            // for (element <- bitsArray) {
            //     println(element.peek())
            // }

            for( i <- 0 until p.totalElements){
                dut.io.out.bits(i).expect(expected(i).S)
            }
            dut.io.state.expect(DecodingState.idle)
        }
    }
    behavior of "DeltaChiselDecode"
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
