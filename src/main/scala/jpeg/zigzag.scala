package jpeg

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._
import java.util.zip.ZipFile

// Defines the stateRegs
object ZigZagState extends ChiselEnum {
    val idle, processing = Value
}

class ZigZagChisel extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new Bundle {
            val matrixIn = Vec(8, Vec(8, SInt(9.W)))
        }))
        val zigzagOut = Valid(Vec(64, SInt(9.W)))
        val state = Output(ZigZagState())
    })

    val inMatrix = Reg(Vec(8, Vec(8, SInt(9.W))))
    val outReg = Reg(Vec(64, SInt(9.W)))
    val count = RegInit(0.U(6.W)) // Keeps track of how many elements are processed
    val row   = RegInit(0.U(3.W)) 
    val col   = RegInit(0.U(3.W))
    val isUp  = RegInit(true.B) // Keeps track of direction
    
    // Regs for ready/valid bits
    val readyIn   = RegInit(true.B) 
    val validOut  = RegInit(false.B)
    
    // FSM
    val stateReg = RegInit(ZigZagState.idle)
    io.state := stateReg

    switch(stateReg) {
        is(ZigZagState.idle) {
            when (io.in.fire) {
                stateReg := ZigZagState.processing
                inMatrix := io.in.bits.matrixIn
                validOut := false.B
                readyIn := false.B
            }
        } 
        
        is(ZigZagState.processing) {
            count := count + 1.U

            when(count < 64.U) {
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
            
            when (count === 63.U) {
                stateReg := ZigZagState.idle
                count := 0.U
                validOut := true.B
                readyIn := true.B
            }
        }
    }

    // Outputs
    io.in.ready := readyIn
    io.zigzagOut.bits := outReg
    io.zigzagOut.valid := validOut
}




class ZigZagDecodeChisel extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new Bundle {
            val zigzagIn = Input(Vec(64, SInt(9.W)))
        }))
        val matrixIn = Vaild(Vec(8, Vec(8, SInt(9.W))))
    })

    val inData = Reg(Vec(64, SInt(9.W)))
    val outMatrix = Reg(Vec(8, Vec(8, SInt(9.W))))
    val count = RegInit(0.U(6.W)) // Keeps track of how many elements are processed
    val row   = RegInit(0.U(3.W)) 
    val col   = RegInit(0.U(3.W))
    val isUp  = RegInit(true.B) // Keeps track of direction
     
    // Regs for ready/valid bits
    val readyIn   = RegInit(true.B) 
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
                readyIn := false.B
            }
        } 
        
        is(ZigZagState.processing) {
            count := count + 1.U

            when(count < 64.U) {
                outMatrix(row)(col) :=  inData(count)
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
            
            when (count === 63.U) {
                stateReg := ZigZagState.idle
                count := 0.U
                validOut := true.B
                readyIn := true.B
            }
        }
    }

    // Outputs
    io.in.ready := readyIn
    io.zigzagOut.bits := outReg
    io.zigzagOut.valid := validOut
}
