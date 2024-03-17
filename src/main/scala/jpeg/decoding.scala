package jpeg

import chisel3._
import chisel3.util._

object RLEDecodingState extends ChiselEnum {
    val idle, decode = Value
}

class decodeRLE extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Valid(new Bundle {
            val data = Vec(12, SInt(8.W))
            val length = UInt(8.W)
        }))
        val out = Valid(Vec(25, SInt(8.W)))
        val state = Output(RLEDecodingState())
    })

    val stateReg = RegInit(RLEDecodingState.idle)

    // Initialize to all zeros
    val dataReg = RegInit(VecInit(Seq.fill(12)(0.S(8.W))))
    
    // Initialize output register
    val outputReg = RegInit(VecInit(Seq.fill(25)(0.S(8.W))))
    val outputIndexCounter = RegInit(0.U(log2Ceil(25+1).W))

    // to keep track of the values from io.in.bits.data
    val freq = RegInit(0.S(8.W))
    val value = RegInit(0.S(8.W))

    // counters for indexing dataReg
    val freqIndex = RegInit(0.U(log2Ceil(12+1).W))
    val valueIndex = RegInit(1.U(log2Ceil(12+1).W))

    val freqCounter = RegInit(0.S(log2Ceil(25+1).W))

    io.state := stateReg
    io.out.valid := false.B
    io.out.bits := outputReg

    val pair = RegInit(0.U(log2Ceil(12+1).W))
    val numPairs = RegInit(6.U(log2Ceil(12+1).W))
    switch(stateReg){
        is(RLEDecodingState.idle){
            when(io.in.fire){
                dataReg := io.in.bits.data
                freq := io.in.bits.data(freqIndex)
                value := io.in.bits.data(valueIndex)
                stateReg := RLEDecodingState.decode
            }
        }

        is(RLEDecodingState.decode){
            when(freqCounter < freq){
                outputReg(outputIndexCounter) := value
                outputIndexCounter := outputIndexCounter + 1.U
                freqCounter := freqCounter + 1.S
            }
            .elsewhen(freqCounter === freq){
                pair := pair + 1.U
                freqCounter := 0.S
                freqIndex := freqIndex + 2.U
                valueIndex := valueIndex + 2.U
                freq := dataReg(freqIndex + 2.U)
                value := dataReg(valueIndex + 2.U)
            }
            when(pair === numPairs) {
                stateReg := RLEDecodingState.idle
            }
        }
    }
}


object DecodingState extends ChiselEnum {
    val idle, decode = Value
}

class decodeDelta(p: JpegParams) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Valid(new Bundle {
            val data = Vec(p.totalElements, SInt(p.w8))
        }))
        val out = Valid(Vec(p.totalElements, SInt(p.w16))) // had to change bit width to 16 to pass tests
        val state = Output(DecodingState())
    })

    val stateReg = RegInit(DecodingState.idle)

    // Initialize to all zeros
    val dataReg = RegInit(VecInit(Seq.fill(p.totalElements)(0.S(p.w8))))
    
    // Initialize output register
    val outputReg = RegInit(VecInit(Seq.fill(p.totalElements)(0.S(p.w16)))) 
    val dataIndex = RegInit(1.U(log2Ceil(p.totalElements+1).W))

    io.state := stateReg
    io.out.valid := false.B
    io.out.bits := outputReg

    switch(stateReg){
        is(DecodingState.idle){
            when(io.in.fire){
                dataReg := io.in.bits.data
                outputReg(0) := io.in.bits.data(0)
                stateReg := DecodingState.decode
            }
        }

        is(DecodingState.decode){
            
            when (dataIndex < p.totalElements.U) {
                outputReg(dataIndex) := outputReg(dataIndex - 1.U) + dataReg(dataIndex)
                dataIndex := dataIndex + 1.U
            }
            .otherwise{
                io.out.valid := true.B
                stateReg := DecodingState.idle
            }
             
        }
    }
}