package jpeg

import chisel3._
import chisel3.util._

/**
  * Performs JPEG Decompression
  *
  * @param p JPEG Parameters
  * 
  * IO
  * @param encodedRLE RLE encoded data input
  * @param encodedDelta Delta encoded data input
  * @param encodingChoice True for RLE, False for Delta
  * 
  * @return dctOut IDCT output used in testing
  * @return quantOut Inverse Quantization output used in testing
  * @return zigzagOut Inverse Zigzag output used in testing
  * @return pixelDataOut Final decoded pixel data
  */
class JPEGDecodeChisel(p: JPEGParams) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Valid(new Bundle {
            val encodedRLE = Vec(p.maxOutRLE, SInt(p.w8))
            val encodedDelta = Vec(p.totalElements, SInt(p.w8))
            val encodingChoice = Bool()
        }))
        
        // Testing Outputs
        val dctOut = Output(Vec(8, Vec(8, SInt(32.W))))
        val quantOut = Output(Vec(p.numRows, Vec(p.numCols, SInt(32.W))))
        val zigzagOut = Output(Vec(p.totalElements, SInt(9.W)))

        // Final Decoded Output
        val pixelDataOut = Valid(Vec(p.givenRows, Vec(p.givenCols, SInt(9.W))))
    })

    // Don't care for test outputs initially
    io.dctOut := DontCare
    io.quantOut := DontCare
    io.zigzagOut := DontCare
    
    // First stage: Entropy Decoding (RLE or Delta)
    val rleDecoder = Module(new RLEChiselDecode(p))
    val deltaDecoder = Module(new DeltaChiselDecode(p))

    // Connect entropy decoders based on encoding choice
    when(io.in.valid) {
        when(io.in.bits.encodingChoice){
            // RLE Path
            rleDecoder.io.in.valid := true.B
            rleDecoder.io.in.bits.data := io.in.bits.encodedRLE
            rleDecoder.io.length := io.in.bits.encodedRLE.length.U

            deltaDecoder.io.in.bits.valid := false.B
        }.otherwise{
            deltaDecoder.io.in.bits.valid := true.B
            deltaDecoder.io.in.bits.data := io.in.bits.encodedDelta
            rleDecoder.io.in.valid := false.B
        }
    }

    // Second stage: Inverse Zigzag
    val invZigzag = Module(new InverseZigZagChisel(p))
    invZigzag.io.in.valid := Mux(io.in.bits.encodingChoice, 
                                rleDecoder.io.out.valid,
                                deltaDecoder.io.out.valid)
    invZigzag.io.in.bits.zigzagIn := Mux(io.in.bits.encodingChoice,
                                        rleDecoder.io.out.bits,
                                        deltaDecoder.io.out.bits)
    io.zigzagOut := invZigzag.io.matrixOut.bits

    // Third stage: Inverse Quantization
    val invQuant = Module(new InverseQuantizationChisel(p))
    invQuant.io.in.valid := invZigzag.io.matrixOut.valid
    invQuant.io.in.bits.data := invZigzag.io.matrixOut.bits

    // Connect quantization table
    invQuant.io.quantTable.zipWithIndex.foreach { case (row, i) =>
        row.zipWithIndex.foreach { case (element, j) =>
            element := p.getQuantTable(i)(j).S
        }
    }
    io.quantOut := invQuant.io.out.bits

    // Fourth stage: Inverse DCT (needs to be implemented)
    // TODO: Implement InverseDCTChisel and connect it here
    // val invDCT = Module(new InverseDCTChisel)
    // invDCT.io.in.valid := invQuant.io.out.valid
    // invDCT.io.in.bits.matrixIn := invQuant.io.out.bits
    // io.dctOut := invDCT.io.out.bits
    // io.pixelDataOut := invDCT.io.out

    // Temporary: Direct connection to output (remove after IDCT implementation)
    io.pixelDataOut.valid := invQuant.io.out.valid
    io.pixelDataOut.bits := RegNext(VecInit(Seq.fill(p.givenRows)(VecInit(Seq.fill(p.givenCols)(0.S(9.W))))))
}