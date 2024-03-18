package jpeg

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._
import java.util.zip.ZipFile

/** 
  * Creates states for ZigZag FSM
  */
object ZigZagState extends ChiselEnum {
    val idle, processing = Value
}

/** Persorms ZigZag parsing on 8x8 matrix converting it to 1D array
  *
  * @param p JPEG Paramaters
  * 
  * IO
  * @param matrixIn Input matrix to perform ZigZag parsing on 
  * 
  * @return zigzagOut Resulting 1d array
  * @return state Current state of FSM
  */
class ZigZagChisel(p: JpegParams) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Valid(new Bundle {
            val matrixIn = Vec(p.numRows, Vec(p.numCols, SInt(9.W)))
        }))
        val zigzagOut = Valid(Vec(p.totalElements, SInt(9.W)))
        val state = Output(ZigZagState())
    })

    // Initializes Counters and Registers
    val inMatrix = RegInit(VecInit(Seq.fill(p.numRows)(VecInit(Seq.fill(p.numCols)(0.S(9.W))))))
    val outReg = RegInit(VecInit(Seq.fill(p.totalElements)(0.S(9.W))))
    val count = RegInit(0.U(6.W)) // Keeps track of how many elements are processed
    val row   = RegInit(0.U(3.W)) 
    val col   = RegInit(0.U(3.W))
    val isUp  = RegInit(true.B) // Keeps track of direction
    
    // Regs for valid bits
    val validOut  = RegInit(false.B)
    
    // FSM
    val stateReg = RegInit(ZigZagState.idle)
    io.state := stateReg

    switch(stateReg) {
        is(ZigZagState.idle) {
            when (io.in.valid) {
                stateReg := ZigZagState.processing
                inMatrix := io.in.bits.matrixIn
                validOut := false.B
            }
        } 
        
        is(ZigZagState.processing) {
            count := count + 1.U
            
            // Conditions increment row/col according to ZigZag pattern
            when(count < p.totalElements.U) {
                // For 8x8 to 1D array input is assigned to output array
                outReg(count) := inMatrix(row)(col)
                when(isUp) {
                    when(col === 7.U) {
                        row := row + 1.U
                        isUp := false.B
                    }.elsewhen(row === 0.U) {
                        col := col + 1.U
                        isUp := false.B
                    }.otherwise {
                        row := row - 1.U
                        col := col + 1.U
                    }
                }.otherwise {
                    when(row === 7.U) {
                        col := col + 1.U
                        isUp := true.B
                    }.elsewhen(col === 0.U) {
                        row := row + 1.U
                        isUp := true.B
                    }.otherwise {
                        row := row + 1.U
                        col := col - 1.U
                    }
                }
            } 
            
            // When parsing complete return to idle
            when (count === (p.totalElements.U - 1.U)) {
                stateReg := ZigZagState.idle
                count := 0.U
                validOut := true.B
            }
        }
    }

    // Outputs
    io.zigzagOut.bits := outReg
    io.zigzagOut.valid := validOut
}



/** Persorms Inverse ZigZag parsing on 1D array converting it 
  * back to an 8x8 matrix
  *
  * @param p JPEG Paramaters
  * 
  * IO
  * @param matrixIn Input matrix to perform ZigZag parsing on 
  * 
  * @return zigzagOut Resulting 1d array
  * @return state Current state of FSM
  */
class InverseZigZagChisel(p: JpegParams) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Valid(new Bundle {
            val zigzagIn = Input(Vec(p.totalElements, SInt(9.W)))
        }))
        val matrixOut = Valid(Vec(p.numRows, Vec(p.numCols, SInt(9.W))))
        val state = Output(ZigZagState())
    })

    // Initializes Counters and Registers
    val inData = RegInit(VecInit(Seq.fill(p.totalElements)(0.S(9.W))))
    val outMatrix = RegInit(VecInit(Seq.fill(p.numRows)(VecInit(Seq.fill(p.numCols)(0.S(9.W))))))
    val count = RegInit(0.U(6.W)) // Keeps track of how many elements are processed
    val row   = RegInit(0.U(3.W)) 
    val col   = RegInit(0.U(3.W))
    val isUp  = RegInit(true.B) // Keeps track of direction
     
    // Regs for valid bits
    val validOut  = RegInit(false.B)
    
    // FSM
    val stateReg = RegInit(ZigZagState.idle)
    io.state := stateReg

    switch(stateReg) {
        is(ZigZagState.idle) {
            when (io.in.fire) {
                stateReg := ZigZagState.processing
                inData := io.in.bits.zigzagIn
                validOut := false.B
            }
        } 
        
        is(ZigZagState.processing) {
            count := count + 1.U

            when(count < p.totalElements.U) {
                // For inverse the order is fliped and the matrix is assigned to the array
                outMatrix(row)(col) := inData(count)
                when(isUp) {
                    when(col === 7.U) {
                        row := row + 1.U
                        isUp := false.B
                    }.elsewhen(row === 0.U) {
                        col := col + 1.U
                        isUp := false.B
                    }.otherwise {
                        row := row - 1.U
                        col := col + 1.U
                    }
                }.otherwise {
                    when(row === 7.U) {
                        col := col + 1.U
                        isUp := true.B
                    }.elsewhen(col === 0.U) {
                        row := row + 1.U
                        isUp := true.B
                    }.otherwise {
                        row := row + 1.U
                        col := col - 1.U
                    }
                }
            } 
            
            when (count === (p.totalElements.U - 1.U)) {
                stateReg := ZigZagState.idle
                count := 0.U
                validOut := true.B
            }
        }
    }

    // Outputs
    io.matrixOut.bits := outMatrix
    io.matrixOut.valid := validOut
}
