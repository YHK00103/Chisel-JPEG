package jpeg

import chisel3._
import chisel3.util._

object Quantization {
  def quantization(data: Vec[Vec[SInt]], quantTable: Vec[Vec[SInt]]): Vec[Vec[SInt]] = {
    val result = Wire(Vec(8, Vec(8, SInt(data(0)(0).getWidth.W))))

    for (i <- 0 until 8) {
      for (j <- 0 until 8) { 
        val resultValue = data(i)(j) / quantTable(i)(j)
        val rounded = (resultValue + (resultValue >= 0.S).asSInt) / 2.S
        result(i)(j) := rounded
      }
    }

    result
  }
}
