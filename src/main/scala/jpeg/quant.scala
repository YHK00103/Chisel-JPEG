package jpeg

import chisel3._
import chisel3.util._

object Quantization {
    def apply(data: Vec[Vec[SInt]], quantTable: Vec[Vec[SInt]], result: Vec[Vec[SInt]]) = {
        val mod = Module(new Quantization)
        mod.io.data := data
        mod.io.quantTable := quantTable
        result := mod.io.result
        mod
    }
}

class Quantization extends Module {
    val io = IO(new Bundle{
        val data = Input(Vec(8, Vec(8, SInt(8.W))))
        val quantTable = Input(Vec(8, Vec(8, SInt(8.W))))
        val result = Output(Vec(8, Vec(8, SInt(8.W))))
    })

    

}
// object Quantization {
//   def quantization(data: Vec[Vec[SInt]], quantTable: Vec[Vec[SInt]]): Vec[Vec[SInt]] = {
//     val result = Wire(Vec(8, Vec(8, SInt(data(0)(0).getWidth.W))))

//     for (i <- 0 until 8) {
//       for (j <- 0 until 8) { 
//         val resultValue = data(i)(j) / quantTable(i)(j)
//         val rounded = (resultValue + (resultValue >= 0.S).asSInt) / 2.S
//         result(i)(j) := rounded
//       }
//     }

//     result
//   }
// }
