package jpeg

import chisel3._
import chisel3.util._

class HuffmanDcEncoder extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Valid(new Bundle {
      val value = SInt(12.W)
      val isLuminance = Bool()
    }))
    val out = Valid(new Bundle {
      val bits = UInt(16.W)
      val length = UInt(8.W)
    })
  })

  // 計算絕對值
  val absValue = Mux(io.in.bits.value < 0.S, -io.in.bits.value, io.in.bits.value).asUInt

  // 計算 category
  val category = Wire(UInt(4.W))
  when(absValue === 0.U) {
    category := 0.U
  }.elsewhen(absValue <= 1.U) {
    category := 1.U
  }.elsewhen(absValue <= 3.U) {
    category := 2.U
  }.elsewhen(absValue <= 7.U) {
    category := 3.U
  }.elsewhen(absValue <= 15.U) {
    category := 4.U
  }.elsewhen(absValue <= 31.U) {
    category := 5.U
  }.elsewhen(absValue <= 63.U) {
    category := 6.U
  }.elsewhen(absValue <= 127.U) {
    category := 7.U
  }.elsewhen(absValue <= 255.U) {
    category := 8.U
  }.elsewhen(absValue <= 511.U) {
    category := 9.U
  }.elsewhen(absValue <= 1023.U) {
    category := 10.U
  }.otherwise {
    category := 11.U
  }

  // DC 亮度的 Huffman 表
  val luminanceDcTable = VecInit(Seq(
    "b00".U(16.W),         // 0: 00
    "b010".U(16.W),        // 1: 010
    "b011".U(16.W),        // 2: 011
    "b100".U(16.W),        // 3: 100
    "b101".U(16.W),        // 4: 101
    "b110".U(16.W),        // 5: 110
    "b1110".U(16.W),       // 6: 1110
    "b11110".U(16.W),      // 7: 11110
    "b111110".U(16.W),     // 8: 111110
    "b1111110".U(16.W),    // 9: 1111110
    "b11111110".U(16.W),   // 10: 11111110
    "b111111110".U(16.W)   // 11: 111111110
  ))

  // DC 色度的 Huffman 表
  val chrominanceDcTable = VecInit(Seq(
    "b00".U(16.W),         // 0: 00
    "b01".U(16.W),         // 1: 01
    "b10".U(16.W),         // 2: 10
    "b110".U(16.W),        // 3: 110
    "b1110".U(16.W),       // 4: 1110
    "b11110".U(16.W),      // 5: 11110
    "b111110".U(16.W),     // 6: 111110
    "b1111110".U(16.W),    // 7: 1111110
    "b11111110".U(16.W),   // 8: 11111110
    "b111111110".U(16.W),  // 9: 111111110
    "b1111111110".U(16.W), // 10: 1111111110
    "b11111111110".U(16.W) // 11: 11111111110
  ))

  // 選擇表並獲取編碼
  val selectedTable = Mux(io.in.bits.isLuminance, luminanceDcTable, chrominanceDcTable)
  val huffmanCode = selectedTable(category)

  // 編碼長度表
  val lengthTable = VecInit(Seq(
    2.U,  // category 0
    3.U,  // category 1
    3.U,  // category 2
    3.U,  // category 3
    3.U,  // category 4
    3.U,  // category 5
    4.U,  // category 6
    5.U,  // category 7
    6.U,  // category 8
    7.U,  // category 9
    8.U,  // category 10
    9.U   // category 11
  ))

  // 輸出
  io.out.bits.bits := huffmanCode
  io.out.bits.length := lengthTable(category)
  io.out.valid := io.in.valid
}