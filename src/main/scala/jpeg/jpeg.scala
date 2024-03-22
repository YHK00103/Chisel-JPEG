package jpeg

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._


class JpegEncodeChisel(p: JpegParams) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Valid(new Bundle {
            val pixelDataIn = Input(Vec(p.givenRows, Vec(p.givenCols, SInt(9.W))))
        }))
        
        val dctOut = Output(Vec(8, Vec(8, SInt(32.W))))
        val quantOut = Output(Vec(p.numRows, Vec(p.numCols, SInt(32.W))))
        val zigzagOut = Output(Vec(p.totalElements, SInt(9.W)))
        val encoded = Output(Vec(p.maxOutRLE, SInt(8.W)))

        // val encodedDataIn = Input(Vec(64, SInt(8.W)))
        // val decoded = Output(Vec(p.givenRows, Vec(p.givenCols, SInt(8.W))))
    })


    val dctModule = Module(new DCTChisel)
    dctModule.io.in.valid := io.in.valid
    dctModule.io.in.bits.matrixIn := io.in.bits.pixelDataIn
    io.dctOut := dctModule.io.dctOut.bits

    val quantModule = Module(new QuantizationChisel(p))
    quantModule.io.in.valid := dctModule.io.dctOut.valid
    quantModule.io.in.bits.data := dctModule.io.dctOut.bits
    io.quantOut := quantModule.io.out.bits

    for (i <- 0 until 8) {
        for (j <- 0 until 8) {
            quantModule.io.quantTable(i)(j) := p.getQuantTable(i)(j).S
        }
    }
        // quantModule.io.quantTable := p.getQuantTable

    val zigzagModule = Module(new ZigZagChisel(p))
    zigzagModule.io.in.valid := quantModule.io.out.valid
    zigzagModule.io.in.bits.matrixIn := quantModule.io.out.bits
    io.zigzagOut := zigzagModule.io.zigzagOut.bits

    val encodeModule = Module(new RLEChiselEncode(p))
    encodeModule.io.in.valid := zigzagModule.io.zigzagOut.valid
    encodeModule.io.in.bits.data := zigzagModule.io.zigzagOut.bits
    io.encoded := encodeModule.io.out.bits

    // when(encodeModule.io.out.valid) {
    //     printf("Content of encode:\n")
    //     for (i <- 0 until p.maxOutRLE) {
    //         printf("%d ", encodeModule.io.out.bits(i))
    //     }
    //     printf("\n")
    // }


}


    // val dctModule = DCTChisel(io.pixelDataIn, ???, ???)
    // val quantizationModule = QuantizationChisel(p, ???, ???, ???)
    // val zigzagModule = ZigZagChisel(p, ???, ???)
    // val rleModule = RLEChiselEncode(p, ???, ???, ???)
    
    // io.encoded = ???
    