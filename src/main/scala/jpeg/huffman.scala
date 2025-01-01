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

    val buffer = RegInit(0.U(24.W))  // store bits in buffer
    val count = RegInit(0.U(5.W))    // Track valid bits

    io.out.valid := false.B
    io.out.bits := 0.U

    when(count >= 8.U) {
        io.out.valid := true.B
        io.out.bits := buffer(7, 0)
        buffer := buffer >> 8
        count := count - 8.U
    }.elsewhen(io.flush && count > 0.U) {
        io.out.valid := true.B
        io.out.bits := buffer(7, 0)
        buffer := 0.U
        count := 0.U
    }

    when(io.in.valid) {
        buffer := Cat(buffer, io.in.bits.bits(15, 16.U - io.in.bits.length))
        count := count + io.in.bits.length
    }
}

/**
  * Performs Huffman encoding for JPEG
  */
class HuffmanEncoder(p: JPEGParams) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Valid(new Bundle {
            val data = SInt(16.W)
            val runLength = UInt(4.W)    // RLE run length
            val isRLE = Bool()           
            val isDC = Bool()            
            val isLuminance = Bool()     
            val isEOB = Bool()           
            val isZRL = Bool()           
        }))
        val out = Valid(UInt(8.W))
        val state = Output(HuffmanState())
    })

    // FSM
    val state = RegInit(HuffmanState.idle)
    io.state := state

    // Huffman buffer
    val buffer = Module(new HuffmanBuffer)
    io.out := buffer.io.out

    // Define Huffman tables
    val dcLumTable = VecInit(Seq(
        (2.U, "b00".U),
        (3.U, "b010".U),
        (3.U, "b011".U),
        (3.U, "b100".U),
        (3.U, "b101".U),
        (3.U, "b110".U),
        (4.U, "b1110".U),
        (5.U, "b11110".U),
        (6.U, "b111110".U),
        (7.U, "b1111110".U),
        (8.U, "b11111110".U),
        (9.U, "b111111110".U)
    ))

    val dcChromTable = VecInit(Seq(
        (2.U, "b00".U),
        (2.U, "b01".U),
        (2.U, "b10".U),
        (3.U, "b110".U),
        (4.U, "b1110".U),
        (5.U, "b11110".U),
        (6.U, "b111110".U),
        (7.U, "b1111110".U),
        (8.U, "b11111110".U),
        (9.U, "b111111110".U),
        (10.U, "b1111111110".U)
    ))

    val acLumTable = VecInit(Seq(
        (4.U, "b1010".U),         // EOB (End of Block)
        (11.U, "b11111111001".U), // ZRL (Zero Run Length)
        (2.U, "b00".U),           // 0/1
        (2.U, "b01".U),           // 0/2
        (3.U, "b100".U),          // 0/3
        (4.U, "b1011".U),         // 0/4
        (5.U, "b11010".U),        // 0/5
        (7.U, "b11110000".U),     // 0/6
        (8.U, "b11111000".U),     // 0/7
        (10.U, "b1111110110".U),  // 0/8
        (16.U, "b1111111110000010".U),     // 0/9
        (16.U, "b1111111110000011".U),     // 0/10
        // Additional entries for full table
        (4.U, "b1100".U),         // 1/1
        (5.U, "b11011".U),        // 1/2
        (7.U, "b1111001".U),       // 1/3
        (9.U, "b111110110".U),       // 1/4
        (11.U, "b11111110110".U),    // 1/5
        (16.U, "b1111111110000100".U),    // 1/6
        (16.U, "b1111111110000101".U),    // 1/7
        (16.U, "b1111111110000110".U),    // 1/8
        (16.U, "b1111111110000111 ".U),   // 1/9
        (16.U, "b1111111110001000".U)    // 1/10
        // 2
        (5.U, "b11100".U),         // 1/1
        (6.U, "b11111001".U),        // 1/2
        (10.U, "b1111110111".U),       // 1/3
        (12.U, "b111111110100".U),       // 1/4
        (16.U, "b1111111110001001".U),    // 1/5
        (16.U, "b1111111110001010".U),    // 1/6
        (16.U, "b1111111110001011".U),    // 1/7
        (16.U, "b1111111110001100".U),    // 1/8
        (16.U, "b1111111110001101 ".U),   // 1/9
        (16.U, "b1111111110001110".U)    // 1/10
        // 3
        (6.U, "b111010".U),         // 1/1
        (9.U, "b111110111".U),        // 1/2
        (12.U, "b111111110101".U),       // 1/3
        (16.U, "b1111111110001111".U),       // 1/4
        (16.U, "b1111111110010000".U),    // 1/5
        (16.U, "b1111111110010001".U),    // 1/6
        (16.U, "b1111111110010010".U),    // 1/7
        (16.U, "b1111111110010011".U),    // 1/8
        (16.U, "b1111111110010100".U),   // 1/9
        (16.U, "b1111111110010101".U)    // 1/10
        // 4
        (6.U, "b111011".U),         // 1/1
        (10.U, "b1111111000".U),        // 1/2
        (16.U, "b1111111110010110".U),       // 1/3
        (16.U, "b1111111110010111".U),       // 1/4
        (10.U, "b1111111110011000".U),    // 1/5
        (16.U, "b1111111110011001".U),    // 1/6
        (16.U, "b1111111110011010".U),    // 1/7
        (16.U, "b1111111110011011".U),    // 1/8
        (16.U, "b1111111110011100".U),   // 1/9
        (16.U, "b1111111110011101".U)    // 1/10
        // 5
        (7.U, "b1111010".U),         // 1/1
        (11.U, "b11111110111".U),        // 1/2
        (16.U, "b1111111110011110".U),       // 1/3
        (16.U, "b1111111110011111".U),       // 1/4
        (10.U, "b1111111110100000".U),    // 1/5
        (16.U, "b1111111110100001".U),    // 1/6
        (16.U, "b1111111110100010".U),    // 1/7
        (16.U, "b1111111110100011".U),    // 1/8
        (16.U, "b1111111110100100".U),   // 1/9
        (16.U, "b1111111110100101".U)    // 1/10
        // 6
        (7.U, "b1111011".U),         // 1/1
        (12.U, "b111111110110".U),        // 1/2
        (16.U, "b1111111110100110".U),       // 1/3
        (16.U, "b1111111110100111".U),       // 1/4
        (16.U, "b1111111110101000".U),    // 1/5
        (16.U, "b1111111110101001".U),    // 1/6
        (16.U, "b1111111110101010".U),    // 1/7
        (16.U, "b1111111110101011".U),    // 1/8
        (16.U, "b1111111110101100".U),   // 1/9
        (16.U, "b1111111110101101".U)    // 1/10
        // 7
        (8.U, "b11111010".U),         // 1/1
        (12.U, "b111111110111".U),        // 1/2
        (16.U, "b1111111110101110".U),       // 1/3
        (16.U, "b1111111110101111".U),       // 1/4
        (16.U, "b1111111110110000".U),    // 1/5
        (16.U, "b1111111110110001".U),    // 1/6
        (16.U, "b1111111110110010".U),    // 1/7
        (16.U, "b1111111110110011".U),    // 1/8
        (16.U, "b1111111110110100".U),   // 1/9
        (16.U, "b1111111110110101".U)    // 1/10
        // 8
        (9.U, "b111111000".U),         // 1/1
        (15.U, "b111111111000000".U),        // 1/2
        (16.U, "b1111111110110110".U),       // 1/3
        (16.U, "b1111111110110111".U),       // 1/4
        (16.U, "b1111111110111000".U),    // 1/5
        (16.U, "b1111111110111001".U),    // 1/6
        (16.U, "b1111111110111010".U),    // 1/7
        (16.U, "b1111111110111011".U),    // 1/8
        (16.U, "b1111111110111100".U),   // 1/9
        (16.U, "b1111111110111101".U)    // 1/10
        // 9
        (9.U, "b111111001".U),         // 1/1
        (16.U, "b1111111110111110".U),        // 1/2
        (16.U, "b1111111110111111".U),       // 1/3
        (16.U, "b1111111111000000".U),       // 1/4
        (16.U, "b1111111111000001".U),    // 1/5
        (16.U, "b1111111111000010".U),    // 1/6
        (16.U, "b1111111111000011".U),    // 1/7
        (16.U, "b1111111111000100".U),    // 1/8
        (16.U, "b1111111111000101".U),   // 1/9
        (16.U, "b1111111111000110".U)    // 1/10
        // A
        (9.U, "b111111010".U),         // 1/1
        (16.U, "b1111111111000111".U),        // 1/2
        (16.U, "b1111111111001000".U),       // 1/3
        (16.U, "b1111111111001001".U),       // 1/4
        (16.U, "b1111111111001010".U),    // 1/5
        (16.U, "b1111111111001011".U),    // 1/6
        (16.U, "b1111111111001100".U),    // 1/7
        (16.U, "b1111111111001101".U),    // 1/8
        (16.U, "b1111111111001110".U),   // 1/9
        (16.U, "b1111111111001111".U)    // 1/10
        // B
        (10.U, "b1111111001".U),         // 1/1
        (16.U, "b1111111111010000".U),        // 1/2
        (16.U, "b1111111111010001".U),       // 1/3
        (16.U, "b1111111111010010".U),       // 1/4
        (16.U, "b1111111111010011".U),    // 1/5
        (16.U, "b1111111111010100".U),    // 1/6
        (16.U, "b1111111111010101".U),    // 1/7
        (16.U, "b1111111111010110".U),    // 1/8
        (16.U, "b1111111111010111".U),   // 1/9
        (16.U, "b1111111111011000".U)    // 1/10
        // C
        (10.U, "b1111111010".U),         // 1/1
        (16.U, "b1111111111011001".U),        // 1/2
        (16.U, "b1111111111011010".U),       // 1/3
        (16.U, "b1111111111011011".U),       // 1/4
        (16.U, "b1111111111011100".U),    // 1/5
        (16.U, "b1111111111011101".U),    // 1/6
        (16.U, "b1111111111011110".U),    // 1/7
        (16.U, "b1111111111011111".U),    // 1/8
        (16.U, "b1111111111100000".U),   // 1/9
        (16.U, "b1111111111100001".U)    // 1/10
        // D
        (11.U, "b11111111000".U),         // 1/1
        (16.U, "b1111111111100010".U),        // 1/2
        (16.U, "b1111111111100011".U),       // 1/3
        (16.U, "b1111111111100100".U),       // 1/4
        (16.U, "b1111111111100101".U),    // 1/5
        (16.U, "b1111111111100110".U),    // 1/6
        (16.U, "b1111111111100111".U),    // 1/7
        (16.U, "b1111111111101000".U),    // 1/8
        (16.U, "b1111111111101001".U),   // 1/9
        (16.U, "b1111111111101010".U)    // 1/10
        // E
        (16.U, "b1111111111101011".U),         // 1/1
        (16.U, "b1111111111101100".U),        // 1/2
        (16.U, "b1111111111101101".U),       // 1/3
        (16.U, "b1111111111101110".U),       // 1/4
        (16.U, "b1111111111101111".U),    // 1/5
        (16.U, "b1111111111110000".U),    // 1/6
        (16.U, "b1111111111110001".U),    // 1/7
        (16.U, "b1111111111110010".U),    // 1/8
        (16.U, "b1111111111110011".U),   // 1/9
        (16.U, "b1111111111110100".U)    // 1/10
        // F
        (11.U, "b11111111001".U),         // 1/0
        (16.U, "b1111111111110101".U),        // 1/1
        (16.U, "b1111111111110110".U),       // 1/2
        (16.U, "b1111111111110111".U),       // 1/3
        (16.U, "b1111111111111000".U),    // 1/4
        (16.U, "b1111111111111001".U),    // 1/5
        (16.U, "b1111111111111010".U),    // 1/6
        (16.U, "b1111111111111011".U),    // 1/7
        (16.U, "b1111111111111100".U),   // 1/8
        (16.U, "b1111111111111101".U)    // 1/9
        (16.U, "b1111111111111110".U)    // 1/10
    ))

    val acChromTable = VecInit(Seq(
        (2.U, "b00".U),      // EOB
        (10.U, "b1111111010".U), // ZRL
        (2.U, "b01".U),      // 0/1
        (3.U, "b100".U),     // 0/2
        (4.U, "b1010".U),    // 0/3
        (5.U, "b11000".U),   // 0/4
        (5.U, "b11001".U),   // 0/5
        // Additional entries for full table
        (6.U, "b111000".U),  // 0/6
        (7.U, "b111001".U),  // 0/7
        (8.U, "b111010".U),  // 0/8
        (9.U, "b111011".U),  // 0/9
        (10.U, "b111100".U), // 0/10
        (3.U, "b101".U),     // 1/1
        (4.U, "b1100".U),    // 1/2
        (5.U, "b11100".U),   // 1/3
        (6.U, "b111010".U),  // 1/4
        (7.U, "b1110110".U)  // 1/5
    ))

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

    def getAdditionalBits(value: SInt, length: UInt): UInt = {
        val absValue = value.abs
        Mux(value >= 0.S,
            absValue.asUInt,
            (~absValue.asUInt + 1.U)(length - 1, 0))
    }

    val inputReg = RegInit(0.S(16.W))
    val categoryReg = RegInit(0.U(4.W))
    val runLengthReg = RegInit(0.U(4.W))

    switch(state) {
        is(HuffmanState.idle) {
            when(io.in.valid) {
                inputReg := io.in.bits.data
                categoryReg := getBitsNeeded(io.in.bits.data)
                runLengthReg := io.in.bits.runLength
                state := HuffmanState.encode
            }
        }

        is(HuffmanState.encode) {
            val (length, code) = Wire(new Bundle {
                val length = UInt(4.W)
                val code = UInt(16.W)
            })

            when(io.in.bits.isEOB) {
                when(io.in.bits.isLuminance) {
                    length := acLumTable(0)._1
                    code := acLumTable(0)._2
                }.otherwise {
                    length := acChromTable(0)._1
                    code := acChromTable(0)._2
                }
            }.elsewhen(io.in.bits.isZRL) {
                when(io.in.bits.isLuminance) {
                    length := acLumTable(1)._1
                    code := acLumTable(1)._2
                }.otherwise {
                    length := acChromTable(1)._1
                    code := acChromTable(1)._2
                }
            }.elsewhen(io.in.bits.isDC) {
                when(io.in.bits.isLuminance) {
                    val (l, c) = dcLumTable(categoryReg)
                    length := l
                    code := c
                }.otherwise {
                    val (l, c) = dcChromTable(categoryReg)
                    length := l
                    code := c
                }
            }.otherwise {
                when(io.in.bits.isLuminance) {
                    val (l, c) = acLumTable(2 + runLengthReg)
                    length := l
                    code := c
                }.otherwise {
                    val (l, c) = acChromTable(2 + runLengthReg)
                    length := l
                    code := c
                }
            }

            val additionalBits = getAdditionalBits(inputReg, categoryReg)
            val fullCode = Mux(io.in.bits.isEOB || io.in.bits.isZRL, code, Cat(code, additionalBits))
            val fullLength = Mux(io.in.bits.isEOB || io.in.bits.isZRL, length, length + categoryReg)

            buffer.io.in.valid := true.B
            buffer.io.in.bits.bits := fullCode
            buffer.io.in.bits.length := fullLength
            state := HuffmanState.output
        }

        is(HuffmanState.output) {
            buffer.io.in.valid := false.B
            when(!buffer.io.out.valid) {
                state := HuffmanState.idle
            }
        }
    }

    buffer.io.flush := state === HuffmanState.output
}
