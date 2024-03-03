package jpeg

import chisel3._
import chisel3.util._

// object RLE {
//     def apply(data: Vec[SInt], result: Vec[SInt]) = {
//         val mod = Module(new RLE)
//         mod.io.data := data
//         result := mod.io.result
//         mod
//     }
// }

// class RLE extends Module {
//     val io = IO(new Bundle{
//         val data = Input(Vec(64, SInt(8.W)))
//         val result = Output(Vec(64, SInt(8.W)))
//     })

// }

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
    
    val dataReg = RegInit(VecInit(Seq.fill(64)(0.S(8.W)))) // Initialize to all zeros
    val outputReg = RegInit(VecInit(Seq.fill(64)(0.S(8.W)))) // Initialize output register
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
            val diff = dataReg(dataIndex) - dataReg(dataIndex - 1.U)
            outputReg(dataIndex) := diff
            when (dataIndex < 64.U) {
                dataIndex := dataIndex + 1.U
            }
            .otherwise{
                io.out.valid := true.B
                stateReg := DeltaState.idle
            }
        }
    }

}

// object Delta {
//     def apply(data: Vec[SInt], result: Vec[SInt]) = {
//         val mod = Module(new Delta)
//         mod.io.data := data
//         result := mod.io.result
//         mod
//     }
// }
// class Delta extends Module {
//     val io = IO(new Bundle{
//         val data = Input(Vec(64, SInt(8.W)))
//         val result = Output(Vec(64, SInt(8.W)))
//     })

//     val result = Wire(Vec(64, SInt(8.W)))
//     val prev = RegInit(0.S(8.W))

//     result(0) := io.data.head
//     for (i <- 1 until 64) {
//         val diff = io.data(i) - prev
//         result(i) := diff
//         prev := io.data(i)
//     }

//     io.result := result

// }
