package jpeg

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._


class jpegEncodeChisel(p: JpegParams) extends Module {
    val io = IO(new Bundle {
        val pixelDataIn = Input(Vec(p.givenRows, Vec(p.givenCols, SInt(8.W))))
        val encoded = Output(Vec(p.maxOutRLE, SInt(8.W)))

        // val encodedDataIn = Input(Vec(64, SInt(8.W)))
        // val decoded = Output(Vec(p.givenRows, Vec(p.givenCols, SInt(8.W))))
    })

    
}