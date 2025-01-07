package jpeg

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._

/**
  * Performs JPEG Decompression
  *
  * @param p JPEG Parameters
  * 
  * IO
  * @param encodedRLE RLE encoded pixel data or Delta encoded data
  * 
  * @return decodedPixels Decoded pixel data
  * 
  */
class JPEGDecodeChisel(p: JPEGParams) extends Module {
    val io = IO(new Bundle {
        val encodedRLE = Flipped(Valid(Vec(p.maxOutRLE, SInt(p.w8))))  // Encoded input data (RLE or Delta)
        
        // Decoded Output
        val decodedPixels = Output(Vec(p.givenRows, Vec(p.givenCols, SInt(9.W))))
        
        // Intermediate testing outputs (optional for debugging)
        val inverseDCTOut = Output(Vec(8, Vec(8, SInt(32.W))))
        val inverseQuantOut = Output(Vec(p.numRows, Vec(p.numCols, SInt(32.W))))
        val inverseZigzagOut = Output(Vec(p.totalElements, SInt(9.W)))
    })

    // Dontcare for output yet
    io.decodedPixels := DontCare

    // Step 1: Decoding - RLE to Zigzag or Delta to Zigzag
    val decodeModule = if (p.encodingChoice.B) {
        Module(new RLEDChiselDecode(p)) // Decoding RLE
    } else {
        Module(new DeltaChiselDecode(p)) // Decoding Delta encoding
    }

    decodeModule.io.in.valid := io.encodedRLE.valid
    decodeModule.io.in.bits.data := io.encodedRLE.bits
    io.inverseZigzagOut := decodeModule.io.out.bits

    // Step 2: Zigzag to Quantization matrix (inverse Zigzag)
    val zigzagModule = Module(new ZigZagChisel(p))
    zigzagModule.io.in.valid := decodeModule.io.out.valid
    zigzagModule.io.in.bits.matrixIn := decodeModule.io.out.bits
    io.inverseZigzagOut := zigzagModule.io.zigzagOut.bits

    // Step 3: Inverse Quantization
    val quantModule = Module(new QuantizationChisel(p))
    quantModule.io.in.valid := zigzagModule.io.zigzagOut.valid
    quantModule.io.in.bits.data := zigzagModule.io.zigzagOut.bits
    io.inverseQuantOut := quantModule.io.out.bits

    // Converts Quantization Table back to SInt
    quantModule.io.quantTable.zipWithIndex.foreach { case (row, i) =>
        row.zipWithIndex.foreach { case (element, j) =>
            element := p.getQuantTable(i)(j).S
        }
    }

    // Step 4: Inverse DCT (IDCT)
    val idctModule = Module(new IDCTChisel)  // IDCT to reverse DCT
    idctModule.io.in.valid := quantModule.io.out.valid
    idctModule.io.in.bits.matrixIn := quantModule.io.out.bits
    io.inverseDCTOut := idctModule.io.idctOut.bits

    // Step 5: Final output (Decoded pixel data)
    when(idctModule.io.idctOut.valid) {
        io.decodedPixels := idctModule.io.idctOut.bits
    }
}

