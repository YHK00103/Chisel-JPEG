package jpeg

import chisel3._
import chisel3.util._

// object DecodingState extends ChiselEnum {
//     val idle, load, decode = Value
// }

// class decodeRLE extends Module {
//     val io = IO(new Bundle {
//         val in = Flipped(Decoupled(new Bundle {
//             val data = Vec(128, SInt(8.W))
//             val length = UInt(8.W)
//         }))
//         val out = Valid(Vec(64, SInt(8.W)))
//         val state = Output(DecodingState())
//     })

//     val stateReg = RegInit(DecodingState.idle)

//     // Initialize to all zeros
//     val dataReg = RegInit(VecInit(Seq.fill(128)(0.S(8.W))))
    
//     // Initialize output register
//     val outputReg = RegInit(VecInit(Seq.fill(64)(0.S(8.W))))

//     // to keep track of the values from io.in.bits.data
//     val freq = RegInit(0.U(8.W))
//     val value = RegInit(0.S(8.W))

//     val freqCounter = RegInit(0.U(log2Ceil(64+1).W))
//     val valueCounter = RegInit(1.U(log2Ceil(64+1).W))

//     val consecutiveCounter = RegInit(0.U(log2Ceil(64+1).W))
//     val outputIndexCounter = RegInit(0.U(log2Ceil(64+1).W))

//     switch(stateReg){
//         is(DecodingState.idle){
//             when(io.in.fire){
//                 // current := io.in.bits.data(0)
//                 dataReg := io.in.bits.data
//                 stateReg := DecodingState.load
//             }
//         }

//         is(DecodingState.load){
//             when(freqCounter < 64.U && valueCounter < 64.U){
//                 freq := dataReg(freqCounter)
//                 value := dataReg(valueCounter)
//                 freqCounter := freqCounter + 2.U
//                 valueCounter := valueCounter + 2.U
//                 stateReg := DecodingState.decode
//                 consecutiveCounter := 0.U
//             }
//         }

//         is(DecodingState.decode){
//             when(consecutiveCounter =/= (freq - 1.U)){
//                 outputReg(outputIndexCounter) := value
//                 consecutiveCounter := consecutiveCounter + 1.U
//                 outputIndexCounter := outputIndexCounter + 1.U
//             }
//             .elsewhen(consecutiveCounter === (freq - 1.U) && valueCounter < 64.U){
//                 stateReg := DecodingState.load
//             }
//             .otherwise {
//                 stateReg := DecodingState.idle
//             }
//         }
//     }
// }


object DecodingState extends ChiselEnum {
    val idle, decode = Value
}

class decodeDelta extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new Bundle {
            val data = Vec(64, SInt(8.W))
        }))
        val out = Valid(Vec(64, SInt(8.W)))
        val state = Output(DecodingState())
    })

    val stateReg = RegInit(DecodingState.idle)

    // Initialize to all zeros
    val dataReg = RegInit(VecInit(Seq.fill(64)(0.S(8.W))))
    
    // Initialize output register
    val outputReg = RegInit(VecInit(Seq.fill(64)(0.S(8.W)))) 
    val dataIndex = RegInit(1.U(log2Ceil(64+1).W))

    io.state := stateReg
    io.out.valid := false.B
    io.out.bits := outputReg
    io.in.ready := stateReg === DecodingState.idle

    switch(stateReg){
        is(DecodingState.idle){
            when(io.in.fire){
                dataReg := io.in.bits.data
                outputReg(0) := io.in.bits.data(0)
                stateReg := DecodingState.decode
            }
        }

        is(DecodingState.decode){
            
            when (dataIndex < 64.U) {
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
