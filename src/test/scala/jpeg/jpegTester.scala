package jpeg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.beans.beancontext.BeanContextChildSupport

/**
  * Top level test harness
  */
class jpegEncodeChiselTester extends AnyFlatSpec with ChiselScalatestTester {
    /**
        * Tests the functionality of jpegEncodeChisel
        *
        * @param data Input pixel data
        * @param encoded Expected encoded output
        */
    def doJpegEncodeChiselTest(data: Seq[Seq[Int]], encoded: Seq[Int], p: JpegParams): Unit = {
        test(new JpegEncodeChisel(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.clock.setTimeout(800)

            // Testing DCT
            val jpegEncoder = new jpegEncode(false, List.empty, 0)
            val expectedDCT = jpegEncoder.DCT(DCTData.in1)
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
                //printf("pased %d, %d", i.S, j.S)
                }
            }
            println("passed DCT")
            
            // Testing Quant
            val expectedQuant = jpegEncoder.scaledQuantization(DCTData.scaledOut1, p.getQuantTable)
            dut.clock.step()
            dut.clock.step(64)
            for (r <- 0 until p.numRows) {
                for (c <- 0 until p.numCols) {
                    dut.io.quantOut(r)(c).expect(expectedQuant(r)(c).S)
                }
            }
            println("passed Quant")

            // Testing Zigzag
            val expectedZigzag = jpegEncoder.zigzagParse(expectedQuant)
            dut.clock.step()
            dut.clock.step(p.totalElements)

            for(i <- 0 until expectedZigzag.length){
                dut.io.zigzagOut(i).expect(expectedZigzag(i).S)
            }
            println("passed Zigzag")

            // Testing Encode
            val expectedEncode = jpegEncoder.RLE(expectedZigzag)
            dut.clock.step()
            dut.clock.step(p.totalElements)
            // println(expectedEncode)

            // Check the output
            for (i <- 0 until expectedEncode.length) {
                dut.io.encoded(i).expect(expectedEncode(i).S)
            }
            println("passed Encode")


        }
    }

    
    behavior of "Top-level Chisel"
    it should "perform encoding correctly" in {
        val p = JpegParams(8, 8, 1)
        val inputData = DCTData.in1 
        val expectedEncoded = Seq.fill(p.maxOutRLE)(1)
        doJpegEncodeChiselTest(inputData, expectedEncoded, p)
    }
}
