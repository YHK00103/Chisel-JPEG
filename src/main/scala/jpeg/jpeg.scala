package jpeg

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._

case class jpegParams() {
    val rowLen: Int = 8
    val colLen: Int = 8
}

class jpegEncodeChisel extends Module {
}