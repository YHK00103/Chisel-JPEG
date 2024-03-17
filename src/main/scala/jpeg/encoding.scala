package jpeg

import chisel3._
import chisel3.util._

/** 
  * Creates states for encoding in RLE and Delta
  */
object EncodingState extends ChiselEnum {
    val idle, encode = Value
}

/** Performs Run Length Encoding
  * 
  * @param p JPEG Paramaters
  * 
  * IO
  * @param data Data to perform RLE on
  * 
  * @return out Result of performing RLE
  * @return length Length of RLE output in out since we cannot determine final width dynamically in hardware
  * @return state Current state of state machine
  */
class RLE(p: JpegParams) extends Module{
    val io = IO(new Bundle {
        val in = Flipped(Valid(new Bundle{
            val data = Vec(p.totalElements, SInt(p.w8))
        }))
        val out = Valid(Vec(p.maxOutRLE, SInt(p.w8)))
        val length = Valid(UInt())
        val state = Output(EncodingState())
    })
    
    val stateReg = RegInit(EncodingState.idle)

    // Initialize to all zeros
    val dataReg = RegInit(VecInit(Seq.fill(p.totalElements)(0.S(p.w8))))
    
    // Initialize output register
    val outputReg = RegInit(VecInit(Seq.fill(128)(0.S(p.w8)))) 
    val dataIndex = RegInit(1.U(log2Ceil(p.totalElements+1).W))

    val consecutiveCounter = RegInit(0.U(log2Ceil(p.totalElements+1).W))
    val valueCounter = RegInit(1.U(log2Ceil(p.totalElements+1).W))
    val consecutive = RegInit(1.S(log2Ceil(p.totalElements+1).W))

    val lenCounter = RegInit(0.U(log2Ceil(p.maxOutRLE+1).W))

    val current = RegInit(0.S(p.w8))

    io.out.valid := false.B
    io.out.bits := outputReg

    // to keep track of how many elements in out are used
    io.length.valid := false.B
    io.length.bits := lenCounter

    io.state := stateReg

    switch(stateReg){
        is(EncodingState.idle){
            when(io.in.fire){
                current := io.in.bits.data(0)
                dataReg := io.in.bits.data
                stateReg := EncodingState.encode
            }
        }

        is(EncodingState.encode){
            when (dataIndex < p.totalElements.U) {
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
                    lenCounter := lenCounter + 2.U
                }
                dataIndex := dataIndex + 1.U
            }
            .otherwise{
                outputReg(consecutiveCounter) := consecutive
                outputReg(valueCounter) := current
                lenCounter := lenCounter + 2.U
                io.out.valid := true.B
                io.length.valid := true.B
                stateReg := EncodingState.idle
            }
        }
    }
}

/** Performs Delta Encoding
  * 
  * @param p JPEG Paramaters
  * 
  * IO
  * @param data Data to perform Delta Encoding on
  * 
  * @return out Result of performing Delta Encoding
  * @return state Current state of state machine
  * 
  */
class Delta(p: JpegParams) extends Module{
    val io = IO(new Bundle {
        val in = Flipped(Valid(new Bundle{
            val data = Vec(p.totalElements, SInt(p.w8))
        }))
        val out = Valid(Vec(p.totalElements, SInt(p.w8)))
        val state = Output(EncodingState())
    })
    
    val stateReg = RegInit(EncodingState.idle)

    // Initialize to all zeros
    val dataReg = RegInit(VecInit(Seq.fill(p.totalElements)(0.S(p.w8))))
    
    // Initialize output register
    val outputReg = RegInit(VecInit(Seq.fill(p.totalElements)(0.S(p.w8)))) 
    val dataIndex = RegInit(1.U(log2Ceil(p.totalElements+1).W))

    io.state := stateReg
    io.out.valid := false.B
    io.out.bits := outputReg

    switch(stateReg){
        is(EncodingState.idle){
            when(io.in.fire){
                dataReg := io.in.bits.data
                stateReg := EncodingState.encode
            }
        }

        is(EncodingState.encode){
            outputReg(0) := dataReg(0)
            when (dataIndex < p.totalElements.U) {
                val diff = dataReg(dataIndex) - dataReg(dataIndex - 1.U)
                outputReg(dataIndex) := diff
                dataIndex := dataIndex + 1.U
            }
            .otherwise{
                io.out.valid := true.B
                stateReg := EncodingState.idle
            }
        }
    }

}