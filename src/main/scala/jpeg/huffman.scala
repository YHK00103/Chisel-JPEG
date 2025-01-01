package jpeg

import chisel3._
import chisel3.util._

/**
  * Creates states for Huffman encoding
  */
object HuffmanState extends ChiselEnum {
    val idle, encoding, done = Value
}

/**
  * Stores Huffman tables - can be DC and AC tables for luminance/chrominance
  */
object HuffmanTables {
    // DC Luminance
    val dcLumCodes = VecInit(Seq(
        "b00".U, "b010".U, "b011".U, "b100".U, "b101".U, "b110".U,
        "b1110".U, "b11110".U, "b111110".U, "b1111110".U, "b11111110".U, "b111111110".U
    ))
    val dcLumLengths = VecInit(Seq(2.U, 3.U, 3.U, 3.U, 3.U, 3.U, 4.U, 5.U, 6.U, 7.U, 8.U, 9.U))

    // DC Chrominance
    val dcChromCodes = VecInit(Seq(
        "b00".U, "b01".U, "b10".U, "b110".U, "b1110".U, "b11110".U,
        "b111110".U, "b1111110".U, "b11111110".U, "b111111110".U, "b1111111110".U, "b11111111110".U
    ))
    val dcChromLengths = VecInit(Seq(2.U, 2.U, 2.U, 3.U, 4.U, 5.U, 6.U, 7.U, 8.U, 9.U, 10.U, 11.U))
}

/**
  * Performs Huffman encoding on input data
  * 
  * @param p JPEG Parameters
  * 
  * IO
  * @param data Input data to encode
  * @param tableSelect Select between DC/AC and luminance/chrominance tables
  * @param out Encoded output bit stream
  * @param outLength Length of valid encoded output bits
  * @param state Current state of encoding FSM
  */
class HuffmanEncoder(p: JPEGParams) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Valid(new Bundle {
            val data = Input(SInt(p.w16))  // Input value to encode
            val tableSelect = Input(UInt(2.W))  // 0: DC lum, 1: DC chrom, 2: AC lum, 3: AC chrom
        }))
        val out = Valid(new Bundle {
            val bits = Output(UInt(16.W))   // Encoded bits
            val length = Output(UInt(5.W))  // Number of valid bits
        })
        val state = Output(HuffmanState())
    })

    val stateReg = RegInit(HuffmanState.idle)
    val outBitsReg = RegInit(0.U(16.W))
    val outLengthReg = RegInit(0.U(5.W))
    val validReg = RegInit(false.B)

    // Calculate category (size) for input value
    def getCategory(value: SInt): UInt = {
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

    // Generate magnitude encoding for a value given its category
    def getMagnitude(value: SInt, category: UInt): UInt = {
        val absValue = value.abs
        Mux(value >= 0.S,
            absValue.asUInt,
            (~absValue.asUInt + 1.U)(category - 1, 0)
        )
    }

    // FSM for Huffman encoding
    switch(stateReg) {
        is(HuffmanState.idle) {
            when(io.in.valid) {
                stateReg := HuffmanState.encoding
                validReg := false.B
                
                // Get category and select appropriate Huffman table
                val category = getCategory(io.in.bits.data)
                val (code, length) = io.in.bits.tableSelect(0) match {
                    case false => (HuffmanTables.dcLumCodes(category), 
                                 HuffmanTables.dcLumLengths(category))
                    case true => (HuffmanTables.dcChromCodes(category), 
                                HuffmanTables.dcChromLengths(category))
                }

                // Generate magnitude bits
                val magnitude = getMagnitude(io.in.bits.data, category)
                
                // Combine Huffman code and magnitude bits
                outBitsReg := (code ## magnitude).asUInt
                outLengthReg := length + category
            }
        }

        is(HuffmanState.encoding) {
            validReg := true.B
            stateReg := HuffmanState.done
        }

        is(HuffmanState.done) {
            validReg := false.B
            stateReg := HuffmanState.idle
        }
    }

    // Assign outputs
    io.state := stateReg
    io.out.valid := validReg
    io.out.bits.bits := outBitsReg
    io.out.bits.length := outLengthReg
}