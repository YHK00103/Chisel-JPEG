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
        // Encoded input data (RLE or Delta)
        val in = Flipped(Valid(new Bundle {
            val encodedRLE = Valid(Vec(p.maxOutRLE, SInt(9.W)))
        }))

        // Decoded Output
        val decodedPixels = Output(Vec(p.givenRows, Vec(p.givenCols, SInt(9.W))))
        
        // Intermediate testing outputs (optional for debugging)
        //val inverseDCTOut = Output(Vec(8, Vec(8, SInt(32.W))))
        //val inverseQuantOut = Output(Vec(p.numRows, Vec(p.numCols, SInt(32.W))))
        //val inverseZigzagOut = Output(Vec(p.totalElements, SInt(9.W)))
        
        val decodeRLEOut = Output(Vec(p.totalElements, SInt(9.W)))
        val decodeDeltaOut = Output(Vec(p.totalElements, SInt(p.w16)))
        val inverseZigzagOut = Output(Vec(p.numRows, Vec(p.numCols, SInt(9.W))))
        val inverseQuantOut = Output(Vec(p.numRows, Vec(p.numCols, SInt(12.W))))
        val inverseDCTOut = Output(Vec(8, Vec(8, SInt(9.W))))  
    })

    // Dontcare for output yet
    io.decodedPixels := DontCare

    // Decoding Module
    // RLE decode
    val RLEdecodeModule = Module(new RLEChiselDecode(p))
    RLEdecodeModule.io.in.valid := io.in.valid
    RLEdecodeModule.io.in.bits.data := io.in.bits.encodedRLE
    io.decodeRLEOut := RLEdecodeModule.io.out.bits

    // Delta decode
    val DeltadecodeModule = Module(new DeltaChiselDecode(p))
    DeltadecodeModule.io.in.valid := RLEdecodeModule.io.out.valid
    DeltadecodeModule.io.in.bits.data := RLEdecodeModule.io.out.bits
    io.decodeDeltaOut := DeltadecodeModule.io.out.bits


    // Inverse ZigZag Module
    val inversezigzagModule = Module(new InverseZigZagChisel(p))
    inversezigzagModule.io.in.valid := DeltadecodeModule.io.out.valid
    inversezigzagModule.io.in.bits.zigzagIn := DeltadecodeModule.io.out.bits
    io.inverseZigzagOut := inversezigzagModule.io.matrixOut.bits
    

    // Inverse Quantization Module
    val inversequantModule = Module(new InverseQuantizationChisel(p))
    inversequantModule.io.in.valid := inversezigzagModule.io.matrixOut.valid
    inversequantModule.io.in.bits.data := inversezigzagModule.io.matrixOut.bits
    io.inverseQuantOut := inversequantModule.io.out.bits

    // Converts Quantization Table back to SInt
    inversequantModule.io.quantTable.zipWithIndex.foreach { case (row, i) =>
        row.zipWithIndex.foreach { case (element, j) =>
            element := p.getQuantTable(i)(j).S
        }
    }

    // Inverse DCT (IDCT) Module
    val idctModule = Module(new IDCTChisel) 
    idctModule.io.in.valid := inversezigzagModule.io.matrixOut.valid
    idctModule.io.in.bits.matrixIn := inversezigzagModule.io.matrixOut.bits
    io.inverseDCTOut := idctModule.io.idctOut.bits

    // Output
    when(idctModule.io.idctOut.valid) {
        io.decodedPixels := idctModule.io.idctOut.bits
    }
}

