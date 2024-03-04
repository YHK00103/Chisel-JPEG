package jpeg

import chisel3._
import chisel3.util._


object RLEState extends ChiselEnum {
    val idle, encode = Value
}

class RLE extends Module{
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new Bundle{
            val data = Vec(64, SInt(8.W))
        }))
        val out = Valid(Vec(64, SInt(8.W)))
        val length = Valid(SInt())
        val state = Output(RLEState())
    })
    
    val stateReg = RegInit(RLEState.idle)

    // Initialize to all zeros
    val dataReg = RegInit(VecInit(Seq.fill(64)(0.S(8.W))))
    
    // Initialize output register
    val outputReg = RegInit(VecInit(Seq.fill(64)(0.S(8.W)))) 
    val dataIndex = RegInit(1.U(log2Ceil(64+1).W))

    // val consecutiveReg = RegInit(VecInit(Seq.fill(64)(0.S(8.W))))
    // val valueReg = RegInit(VecInit(Seq.fill(64)(0.S(8.W))))

    val consecutiveCounter = RegInit(0.U(log2Ceil(64+1).W))
    val valueCounter = RegInit(1.U(log2Ceil(64+1).W))
    val consecutive = RegInit(1.S(log2Ceil(64+1).W))

    val lenCounter = RegInit(0.S(log2Ceil(64+1).W))

    val current = RegInit(0.S(8.W))

    io.out.valid := false.B
    io.out.bits := outputReg

    // to keep track of how many elements in out are used
    io.length.valid := false.B
    io.length.bits := lenCounter

    io.state := stateReg
    io.in.ready := stateReg === RLEState.idle

    switch(stateReg){
        is(RLEState.idle){
            when(io.in.fire){
                current := io.in.bits.data(0)
                // valueReg(0) := io.in.bits.data(0)
                dataReg := io.in.bits.data
                stateReg := RLEState.encode
            }
        }

        is(RLEState.encode){
            when (dataIndex < 64.U) {
                when(dataReg(dataIndex) === current){
                    consecutive := consecutive + 1.S
                }
                .otherwise {
                    outputReg(consecutiveCounter) := consecutive
                    outputReg(valueCounter) := current
                    consecutiveCounter := consecutiveCounter + 2.U
                    valueCounter := valueCounter + 2.U
                    current := dataReg(dataIndex)
                    consecutive := 1.S
                    lenCounter := lenCounter + 2.S
                }
                dataIndex := dataIndex + 1.U
            }
            .otherwise{
                outputReg(consecutiveCounter) := consecutive
                outputReg(valueCounter) := current
                lenCounter := lenCounter + 2.S
                io.out.valid := true.B
                io.length.valid := true.B
                stateReg := RLEState.idle
            }
        }
    }
}




object DeltaState extends ChiselEnum {
    val idle, encode = Value
}

class Delta extends Module{
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new Bundle{
            val data = Vec(64, SInt(8.W))
        }))
        val out = Valid(Vec(64, SInt(8.W)))
        val state = Output(DeltaState())
    })
    
    val stateReg = RegInit(DeltaState.idle)

    // Initialize to all zeros
    val dataReg = RegInit(VecInit(Seq.fill(64)(0.S(8.W))))
    
    // Initialize output register
    val outputReg = RegInit(VecInit(Seq.fill(64)(0.S(8.W)))) 
    val dataIndex = RegInit(1.U(log2Ceil(64+1).W))

    io.state := stateReg
    io.out.valid := false.B
    io.out.bits := outputReg
    io.in.ready := stateReg === DeltaState.idle

    switch(stateReg){
        is(DeltaState.idle){
            when(io.in.fire){
                dataReg := io.in.bits.data
                stateReg := DeltaState.encode
            }
        }

        is(DeltaState.encode){
            outputReg(0) := dataReg(0)
            when (dataIndex < 64.U) {
                val diff = dataReg(dataIndex) - dataReg(dataIndex - 1.U)
                outputReg(dataIndex) := diff
                dataIndex := dataIndex + 1.U
            }
            .otherwise{
                io.out.valid := true.B
                stateReg := DeltaState.idle
            }
        }
    }

}