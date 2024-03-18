package jpeg

import chisel3._
import chisel3.util._

object QuantState extends ChiselEnum {
    val idle, quant = Value
}

class Quantization(p: JpegParams) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new Bundle{
            val data = Input(Vec(p.numRows, Vec(p.numCols, SInt(32.W))))
            val quantTable = Input(Vec(p.numRows, Vec(p.numCols, SInt(12.W))))
        }))
        val out = Valid(Vec(p.numRows, Vec(p.numCols, SInt(p.w8))))
        val state = Output(QuantState())
    })

    // registers to hold io
    val outputReg = RegInit(VecInit(Seq.fill(p.numRows)(VecInit(Seq.fill(p.numCols)(0.S(12.W))))))
    val dataReg = RegInit(VecInit(Seq.fill(p.numRows)(VecInit(Seq.fill(p.numCols)(0.S(32.W))))))
    val quantTabReg = RegInit(VecInit(Seq.fill(p.numRows)(VecInit(Seq.fill(p.numCols)(0.S(12.W))))))

    val stateReg = RegInit(QuantState.idle)
    io.in.ready := stateReg === QuantState.idle
    io.out.valid := false.B
    io.state := stateReg
    io.out.bits := outputReg

    // row and col counters
    val rCounter = Counter(p.numRows)
    val cCounter = Counter(p.numCols)

    switch(stateReg){
        is(QuantState.idle){
            when(io.in.fire){
                dataReg := VecInit(io.in.bits.data.map(row => VecInit(row.map(_ / 1000000.S))))
                quantTabReg := io.in.bits.quantTable
                stateReg := QuantState.quant
            }
        }

        is(QuantState.quant){
            when(dataReg(rCounter.value)(cCounter.value) < 0.S){
                val remainder = dataReg(rCounter.value)(cCounter.value) % quantTabReg(rCounter.value)(cCounter.value)
                when(rCounter.value === 4.U && cCounter.value === 2.U) {
                    printf("remainder: %d data: %d QT: %d\n", remainder, dataReg(rCounter.value)(cCounter.value), quantTabReg(rCounter.value)(cCounter.value))
                    printf("Test: %d\n", (-55 % 37).S)
                }
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

            when(rCounter.value === (p.numRows - 1).U && cCounter.value === (p.numCols - 1).U) {
                io.out.valid := true.B
                stateReg := QuantState.idle
            }
        }
    }

}

class QuantizationDecode(p: JpegParams) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new Bundle{
            val data = Input(Vec(p.numRows, Vec(p.numCols, SInt(12.W))))
            val quantTable = Input(Vec(p.numRows, Vec(p.numCols, SInt(12.W))))
        }))
        val out = Valid(Vec(p.numRows, Vec(p.numCols, SInt(12.W))))
        val state = Output(QuantState())
    })

    // registers to hold io
    val outputReg = RegInit(VecInit(Seq.fill(p.numRows)(VecInit(Seq.fill(p.numCols)(0.S(12.W))))))
    val dataReg = RegInit(VecInit(Seq.fill(p.numRows)(VecInit(Seq.fill(p.numCols)(0.S(12.W))))))
    val quantTabReg = RegInit(VecInit(Seq.fill(p.numRows)(VecInit(Seq.fill(p.numCols)(0.S(12.W))))))

    val stateReg = RegInit(QuantState.idle)
    io.in.ready := stateReg === QuantState.idle
    io.out.valid := false.B
    io.state := stateReg
    io.out.bits := outputReg

    // row and col counters
    val rCounter = Counter(p.numRows)
    val cCounter = Counter(p.numCols)

    switch(stateReg){
        is(QuantState.idle){
            when(io.in.fire){
                dataReg := io.in.bits.data
                quantTabReg := io.in.bits.quantTable
                stateReg := QuantState.quant
            }
        }

        is(QuantState.quant){
            outputReg(rCounter.value)(cCounter.value) := dataReg(rCounter.value)(cCounter.value) * quantTabReg(rCounter.value)(cCounter.value)
            
            when(cCounter.inc()) {
                rCounter.inc()
            }

            when(rCounter.value === (p.numRows - 1).U && cCounter.value === (p.numCols - 1).U) {
                io.out.valid := true.B
                stateReg := QuantState.idle
            }
        }
    }

}

