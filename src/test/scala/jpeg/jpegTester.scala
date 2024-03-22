package jpeg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.beans.beancontext.BeanContextChildSupport

/**
  * Top level test harness
  */
class JPEGEncodeChiselTest extends AnyFlatSpec with ChiselScalatestTester {
    /**
        * Tests the functionality of jpegEncodeChisel
        *
        * @param data Input pixel data
        * @param encoded Expected encoded output
        */
    def doJPEGEncodeChiselTest(data: Seq[Seq[Int]], p: JPEGParams): Unit = {
        test(new JPEGEncodeChisel(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.clock.setTimeout(0)

            println("Starting Encode")
            // Testing DCT
            val jpegEncoder = new jpegEncode(false, List.empty, 0)
            val expectedDCT = jpegEncoder.DCT(data)
            val expectedDCTInt: Seq[Seq[Int]] = expectedDCT.map(_.map(_.toInt))
            val convertedMatrix: Seq[Seq[SInt]] = expectedDCT.map(row => row.map(value => value.toInt.S))

            // Initialize input
            dut.io.in.valid.poke(true.B)
            // Set input pixel data
            for (i <- 0 until p.givenRows) {
                for (j <- 0 until p.givenCols) {
                dut.io.in.bits.pixelDataIn(i)(j).poke(data(i)(j).S)
                }
            }

            // Take step
            dut.clock.step(3)
            for (i <- 0 until 8) {
                for (j <- 0 until 8) {
                    dut.io.dctOut(i)(j).expect(convertedMatrix(i)(j))
                }
            }
            println("Passed Discrete Cosine Transform")
            
            // Testing Quant
            val expectedQuant = jpegEncoder.scaledQuantization(expectedDCTInt, p.getQuantTable)
            dut.clock.step()
            dut.clock.step(64)
            for (r <- 0 until p.numRows) {
                for (c <- 0 until p.numCols) {
                    dut.io.quantOut(r)(c).expect(expectedQuant(r)(c).S)
                }
            }
            println("Passed Quantization")

            // Testing Zigzag
            val expectedZigzag = jpegEncoder.zigzagParse(expectedQuant)
            dut.clock.step()
            dut.clock.step(p.totalElements)

            for(i <- 0 until expectedZigzag.length){
                dut.io.zigzagOut(i).expect(expectedZigzag(i).S)
            }
            println("Passed Zigzag")

            // Testing Encode
            if(p.encodingChoice){
                val expectedEncode = jpegEncoder.RLE(expectedZigzag)
                dut.clock.step()
                dut.clock.step(p.totalElements)

                // Check the output
                for (i <- 0 until expectedEncode.length) {
                    dut.io.encodedRLE(i).expect(expectedEncode(i).S)
                }
                println("Passed Run Length Encoding")
            }
            else{
                val expectedEncode = jpegEncoder.delta(expectedZigzag)
                dut.clock.step()
                dut.clock.step(p.totalElements)

                // Check the output
                for (i <- 0 until p.totalElements) {
                    dut.io.encodedDelta(i).expect(expectedEncode(i).S)
                }
                println("Passed Delta Encoding")
            }
            println("Completed Encoding\n")
        }
    }

    
    behavior of "Top-level JPEG Encode Chisel"
    it should "Encodes using RLE - QT1" in {
        val p = JPEGParams(8, 8, 1, true)
        val inputData = DCTData.in1 
        doJPEGEncodeChiselTest(inputData, p)
    }

    it should "Encodes using Delta Encoding - QT1" in {
        val p = JPEGParams(8, 8, 1, false)
        val inputData = DCTData.in1 
        doJPEGEncodeChiselTest(inputData, p)
    }

    // OPTIONAL WORKING TESTS
    // REMOVED SINCE IT TAKES 20 MINS TO COMPLETE
    // it should "Encodes using RLE - QT2" in {
    //     val p = JPEGParams(8, 8, 2, true)
    //     val inputData = DCTData.in1 
    //     doJPEGEncodeChiselTest(inputData, p)
    // }

    // it should "Encodes using Delta Encoding - QT2" in {
    //     val p = JPEGParams(8, 8, 2, false)
    //     val inputData = DCTData.in1 
    //     doJPEGEncodeChiselTest(inputData, p)
    // }

    // it should "Encodes using RLE - IN2 - QT1" in {
    //     val p = JPEGParams(8, 8, 1, false)
    //     val inputData = DCTData.in2 
    //     doJPEGEncodeChiselTest(inputData, p)
    // }

    // it should "Encodes using RLE - IN2 - QT2" in {
    //     val p = JPEGParams(8, 8, 2, false)
    //     val inputData = DCTData.in2 
    //     doJPEGEncodeChiselTest(inputData, p)
    // }

    // it should "Encodes using Delta Encoding - IN2 - QT1" in {
    //     val p = JPEGParams(8, 8, 1, false)
    //     val inputData = DCTData.in2 
    //     doJPEGEncodeChiselTest(inputData, p)
    // }

    // it should "Encodes using Delta Encoding - IN2 - QT2" in {
    //     val p = JPEGParams(8, 8, 2, false)
    //     val inputData = DCTData.in2 
    //     doJPEGEncodeChiselTest(inputData, p)
    // }
}
