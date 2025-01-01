package jpeg

import chisel3._
import chisel3.util._

/**
  * Creates FSM states for Huffman encoding
  */
object HuffmanState extends ChiselEnum {
    val idle, encode, output = Value
}

/**
  * Buffer for shifting and packing Huffman codes
  */
class HuffmanBuffer extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Valid(new Bundle {
            val bits = UInt(16.W)
            val length = UInt(4.W)
        }))
        val out = Valid(UInt(8.W))
        val flush = Input(Bool())
    })

    val buffer = RegInit(0.U(24.W))  // 用於儲存位元
    val count = RegInit(0.U(5.W))    // 追踪buffer中有效位元數
    val valid = RegInit(false.B)

    // 當buffer中有8位或以上時輸出
    when(count >= 8.U) {
        io.out.valid := true.B
        io.out.bits := buffer(7, 0)
        buffer := buffer >> 8
        count := count - 8.U
    }.elsewhen(io.flush && count > 0.U) {
        // 在flush時輸出剩餘位元
        io.out.valid := true.B
        io.out.bits := buffer(7, 0)
        buffer := 0.U
        count := 0.U
    }.otherwise {
        io.out.valid := false.B
        io.out.bits := 0.U
    }

    // 輸入新的編碼
    when(io.in.valid) {
        buffer := Cat(buffer(23, 0), io.in.bits.bits) >> io.in.bits.length
        count := count + io.in.bits.length
    }
}

/**
  * Performs Huffman encoding for JPEG
  * 
  * @param p JPEG Parameters
  * 
  * IO
  * @param data Input data to encode
  * @param isDC Whether the input is DC coefficient
  * @param isLuminance Whether to use luminance tables
  * @param out Encoded output
  */
class HuffmanEncoder(p: JPEGParams) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Valid(new Bundle {
            val data = SInt(16.W)
            val isDC = Bool()
            val isLuminance = Bool()
        }))
        val out = Valid(UInt(8.W))
        val state = Output(HuffmanState())
    })

    // 狀態機管理
    val state = RegInit(HuffmanState.idle)
    io.state := state

    // Huffman緩衝器
    val buffer = Module(new HuffmanBuffer)
    io.out := buffer.io.out

    // 計算位元數的函數
    def getBitsNeeded(value: SInt): UInt = {
        val absValue = value.abs
        Mux(absValue === 0.S, 0.U,
        Mux(absValue <= 1.S, 1.U,
        Mux(absValue <= 3.S, 2.U,
        Mux(absValue <= 7.S, 3.U,
        Mux(absValue <= 15.S, 4.U,
        Mux(absValue <= 31.S, 5.U,
        Mux(absValue <= 63.S, 6.U,
        Mux(absValue <= 127.S, 7.U,
        Mux(absValue <= 255.S, 8.U,
        Mux(absValue <= 511.S, 9.U,
        Mux(absValue <= 1023.S, 10.U,
        11.U)))))))))))
    }

    // 生成附加位編碼
    def getAdditionalBits(value: SInt, length: UInt): UInt = {
        val absValue = value.abs
        Mux(value >= 0.S,
            absValue.asUInt,
            (~absValue.asUInt + 1.U)(length - 1, 0))
    }

    // 暫存器
    val inputReg = RegInit(0.S(16.W))
    val categoryReg = RegInit(0.U(4.W))
    val codeReg = RegInit(0.U(16.W))
    val codeLengthReg = RegInit(0.U(4.W))
    val flushReg = RegInit(false.B)

    // DC亮度Huffman表
    val dcLumTable = VecInit(Seq(
        (2.U, "b00".U),    // 0
        (3.U, "b010".U),   // 1
        (3.U, "b011".U),   // 2
        (3.U, "b100".U),   // 3
        (3.U, "b101".U),   // 4
        (3.U, "b110".U),   // 5
        (4.U, "b1110".U),  // 6
        (5.U, "b11110".U), // 7
        (6.U, "b111110".U) // 8+
    ))

    // 狀態機邏輯
    switch(state) {
        is(HuffmanState.idle) {
            when(io.in.valid) {
                inputReg := io.in.bits.data
                categoryReg := getBitsNeeded(io.in.bits.data)
                state := HuffmanState.encode
                flushReg := false.B
            }
        }

        is(HuffmanState.encode) {
            val (length, code) = Mux(io.in.bits.isDC && io.in.bits.isLuminance,
                dcLumTable(categoryReg),
                dcLumTable(0)) // 暫時只使用DC亮度表，之後可擴展

            val additionalBits = getAdditionalBits(inputReg, categoryReg)
            val fullCode = Cat(code, additionalBits)
            val fullLength = length + categoryReg

            buffer.io.in.valid := true.B
            buffer.io.in.bits.bits := fullCode
            buffer.io.in.bits.length := fullLength
            
            state := HuffmanState.output
            flushReg := true.B
        }

        is(HuffmanState.output) {
            buffer.io.in.valid := false.B
            when(!buffer.io.out.valid) {
                state := HuffmanState.idle
            }
        }
    }

    // 控制buffer的flush
    buffer.io.flush := flushReg && (state === HuffmanState.output)
}