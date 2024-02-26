package jpeg

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._

case class jpegParams() {
    val rowLen: Int = 8
    val colLen: Int = 8
}

class jpegEncodeChisel extends Module {
  val io = IO(new Bundle {
    val pixelMatrixIn = Input(Vec(8, Vec(8, SInt(8.W))))
    val dctOut = Output(Vec(8, Vec(8, FixedPoint(16.W, 8.BP))))
  })


}