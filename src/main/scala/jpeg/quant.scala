package jpeg

import chisel3._
import chisel3.util._

object QuantState extends ChiselEnum {
    val idle, quant = Value
}

class Quantization extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new Bundle{
            val data = Input(Vec(8, Vec(8, SInt(12.W))))
            val quantTable = Input(Vec(8, Vec(8, SInt(12.W))))
        }))
        val out = Valid(Vec(8, Vec(8, SInt(8.W))))
        val state = Output(QuantState())
    })

    // registers to hold io
    val outputReg = RegInit(VecInit(Seq.fill(8)(VecInit(Seq.fill(8)(0.S(12.W))))))
    val dataReg = RegInit(VecInit(Seq.fill(8)(VecInit(Seq.fill(8)(0.S(12.W))))))
    val quantTabReg = RegInit(VecInit(Seq.fill(8)(VecInit(Seq.fill(8)(0.S(12.W))))))

    val stateReg = RegInit(QuantState.idle)
    io.in.ready := stateReg === QuantState.idle
    io.out.valid := false.B
    io.state := stateReg
    io.out.bits := outputReg

    // row and col counters
    val rCounter = Counter(8)
    val cCounter = Counter(8)

    switch(stateReg){
        is(QuantState.idle){
            when(io.in.fire){
                dataReg := io.in.bits.data
                quantTabReg := io.in.bits.quantTable
                stateReg := QuantState.quant
            }
        }

        is(QuantState.quant){
            // outputReg(rCounter.value)(cCounter.value) := dataReg(rCounter.value)(cCounter.value) / quantTabReg(rCounter.value)(cCounter.value)
            // val quantResult = dataReg(rCounter.value)(cCounter.value) / quantTabReg(rCounter.value)(cCounter.value)
            when(dataReg(rCounter.value)(cCounter.value) < 0.S){
                val remainder = dataReg(rCounter.value)(cCounter.value) % quantTabReg(rCounter.value)(cCounter.value)
                when(remainder <= (quantTabReg(rCounter.value)(cCounter.value) / -2.S)){
                    outputReg(rCounter.value)(cCounter.value) := (dataReg(rCounter.value)(cCounter.value) / quantTabReg(rCounter.value)(cCounter.value)) - 1.S
                    
                }
                .otherwise{
                    outputReg(rCounter.value)(cCounter.value) := dataReg(rCounter.value)(cCounter.value) / quantTabReg(rCounter.value)(cCounter.value)
                }
            }
            .otherwise{
                val remainder = dataReg(rCounter.value)(cCounter.value) % quantTabReg(rCounter.value)(cCounter.value)
                when(remainder >= (quantTabReg(rCounter.value)(cCounter.value) / 2.S)){
                    outputReg(rCounter.value)(cCounter.value) := (dataReg(rCounter.value)(cCounter.value) / quantTabReg(rCounter.value)(cCounter.value)) + 1.S
                }
                .otherwise{
                    outputReg(rCounter.value)(cCounter.value) := dataReg(rCounter.value)(cCounter.value) / quantTabReg(rCounter.value)(cCounter.value)
                }
            }
            when(cCounter.inc()) {
                rCounter.inc()
            }

            when(rCounter.value === 7.U && cCounter.value === 7.U) {
                io.out.valid := true.B
                stateReg := QuantState.idle
            }
        }
    }

}

