package jpeg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.beans.beancontext.BeanContextChildSupport

/**
  * Top level test harness for JPEGDecoder
  */
class JPEGDecodeChiselTest extends AnyFlatSpec with ChiselScalatestTester {

    /**
        * Tests the functionality of jpegDecodeChisel
        *
        * @param data Input encoded data (could be RLE or delta encoded data)
        * @param decoded Expected decoded pixel output
        */
    def doJPEGDecodeChiselTest(data: Seq[Int], p: JPEGParams): Unit = {
        test(new JPEGDecodeChisel(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.clock.setTimeout(0)

            println("Starting Decode")
            // Initialize input
            dut.io.in.valid.poke(true.B)
            // Set input encoded data
            for (i <- 0 until data.length) {
                dut.io.in.bits.encodedDataIn(i).poke(data(i).S)
            }

            // Take a step
            dut.clock.step(3)
            
            // Testing Run Length Decoding or Delta Decoding based on input params
            if (p.encodingChoice) {
                // Testing Run Length Decoding
                val expectedDecodedRLE = jpegDecodeRLE(data)
                dut.clock.step()
                dut.clock.step(expectedDecodedRLE.length)

                for (i <- 0 until expectedDecodedRLE.length) {
                    dut.io.decodedDataOut(i).expect(expectedDecodedRLE(i).S)
                }
                println("Passed Run Length Decoding")
            } else {
                // Testing Delta Decoding
                val expectedDecodedDelta = jpegDecodeDelta(data)
                dut.clock.step()
                dut.clock.step(expectedDecodedDelta.length)

                for (i <- 0 until expectedDecodedDelta.length) {
                    dut.io.decodedDataOut(i).expect(expectedDecodedDelta(i).S)
                }
                println("Passed Delta Decoding")
            }
            println("Completed Decoding\n")
        }
    }

    // Mock-up of the decoding functions for RLE and Delta (to be implemented)
    def jpegDecodeRLE(encodedData: Seq[Int]): Seq[Int] = {
        // Implement RLE decoding logic here
        encodedData // Placeholder
    }

    def jpegDecodeDelta(encodedData: Seq[Int]): Seq[Int] = {
        // Implement Delta decoding logic here
        encodedData // Placeholder
    }

    behavior of "Top-level JPEG Decode Chisel"
    
    it should "Decode using RLE - IN1 - QT1" in {
        val p = JPEGParams(8, 8, 1, true)
        val inputData = EncodedData.in1
        doJPEGDecodeChiselTest(inputData, p)
    }

    it should "Decode using Delta Encoding - IN1 - QT1" in {
        val p = JPEGParams(8, 8, 1, false)
        val inputData = EncodedData.in1
        doJPEGDecodeChiselTest(inputData, p)
    }

    // OPTIONAL WORKING TESTS
    // it should "Decode using RLE - IN1 - QT2" in {
    //     val p = JPEGParams(8, 8, 2, true)
    //     val inputData = EncodedData.in1
    //     doJPEGDecodeChiselTest(inputData, p)
    // }

    // it should "Decode using Delta Encoding - IN1 - QT2" in {
    //     val p = JPEGParams(8, 8, 2, false)
    //     val inputData = EncodedData.in1
    //     doJPEGDecodeChiselTest(inputData, p)
    // }

    // it should "Decode using RLE - IN2 - QT1" in {
    //     val p = JPEGParams(8, 8, 1, true)
    //     val inputData = EncodedData.in2
    //     doJPEGDecodeChiselTest(inputData, p)
    // }

    // it should "Decode using Delta Encoding - IN2 - QT1" in {
    //     val p = JPEGParams(8, 8, 1, false)
    //     val inputData = EncodedData.in2
    //     doJPEGDecodeChiselTest(inputData, p)
    // }
}
