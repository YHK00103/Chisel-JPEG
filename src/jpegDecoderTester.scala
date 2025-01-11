package jpeg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
  * JPEG 解碼的測試程式
  */
class JPEGDecodeChiselTest extends AnyFlatSpec with ChiselScalatestTester {

    /**
        * 測試 JPEG 解碼過程
        *
        * @param encodedData RLE 或 Delta 編碼的輸入數據
        * @param expectedDecodedPixels 預期的解碼後像素數據
        * @param p JPEG 參數
        */

    // Note RLE,和delta解碼各自獨立tester和Decoder需要修改

    def doJPEGDecodeChiselTest(encodedData: Seq[Seq[Int]], p: JPEGParams): Unit = {
        test(new JPEGDecodeChisel(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        dut.clock.setTimeout(0)

        println("Starting Dncode")
        // Initialize input
        dut.io.in.valid.poke(true.B)
        // Set input data
        for (i <- 0 until .maxOutRLE) {
            dut.io.in.bits.encodedRLE(i).poke(encodedData(i).S)
        }

        // Testing RLE
        val jpegDecoder = new jpegEncode(false, List.empty, 0)
        val expectedRLE = jpegDecoder.decodeRLE(encodedData)

        // Testing Delta
        val expectedDelta = jpegDecoder.decodeDelta(expectedRLE)


        // Testing inverse ZigZag
        val expectedInverseZigzag = jpegDecoder.zigzagDecode(expectedDelta)
        dut.clock.step()
        dut.clock.step(64)

        for(i <- 0 until p.numRows){
            (ij <- 0 until p.numCols){
                dut.io.inverseZigzagOut(i)(j).expect(expectedInverseZigzag(i)(j).S)
            }
        }
        println("Passed inverse Zigzag")


        // Testing inverse Quantization
        val expectedInverseQuant = jpegDecoder.inverseQuantization(expectedInverseZigzag, p.getQuantTable)
        dut.clock.step()
        dut.clock.step(64)

        for(i <- 0 until p.numRows){
            (ij <- 0 until p.numCols){
                dut.io.inverseQuantOut(i)(j)..expect(expectedInverseQuant(i)(j).S)
            }
        }
        println("Passed inverse Quantization")


        // Testing inverse DCT
        val expectedIDCT = jpegDecoder.inverseDCT(expectedInverseQuant)
        dut.clock.step(3)
        for (i <- 0 until 8) {
            for (j <- 0 until 8) {
                dut.io.inverseDCTOut(i)(j).expect(expectedIDCT(i)(j))
            }
        }
        println("Passed Inverse Discrete Cosine Transform")
    
        

        
        }
    }

  behavior of "JPEG 解碼器"

  it should "解碼使用 RLE 編碼的數據" in {
    val p = JPEGParams(8, 8, 1, true)
    val encodedData = Seq(Seq(4, 0, 3, 255), Seq(2, 1, 1, 0)) // 測試用的編碼數據
    val expectedDecodedPixels = Seq(Seq(0, 0, 0, 0, 255, 255, 255), Seq(1, 1, 0, 0, 0, 0, 0)) // 預期解碼結果
    doJPEGDecodeChiselTest(encodedData, expectedDecodedPixels, p)
  }

  it should "解碼使用 Delta 編碼的數據" in {
    val p = JPEGParams(8, 8, 1, false)
    val encodedData = Seq(Seq(5, 0, 0, 0), Seq(2, -1, 0, 0)) // 測試用的編碼數據
    val expectedDecodedPixels = Seq(Seq(5, 5, 5, 5), Seq(2, 1, 1, 1)) // 預期解碼結果
    doJPEGDecodeChiselTest(encodedData, expectedDecodedPixels, p)
  }
}
