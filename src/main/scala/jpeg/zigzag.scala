package jpeg

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._
import java.util.zip.ZipFile

class ZigZagChisel extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new Bundle {
            val matrixIn = Input(Vec(8, Vec(8, SInt(9.W))))
        }))
        val zigzagOut = Valid(Vec(64, SInt(9.W)))
    })

    val inMatrix = Reg(Vec(8, Vec(8, SInt(9.W))))
    val outReg = Reg(Vec(64, SInt(9.W)))
    val count = RegInit(0.U(6.W)) // Keeps track of how many elements are processed
    val row   = RegInit(0.U(3.W)) 
    val col   = RegInit(0.U(3.W))
    val isUp  = RegInit(true.B) // Keeps track of direction
    
    val readyIn   = RegInit(true.B) 
    val validOut  = RegInit(false.B)
    
    object ZigZagState extends ChiselEnum {
        val idle, processing = Value
    }
    val state = RegInit(ZigZagState.idle)

    when (state === ZigZagState.idle) {
        printf("Output Matrix:\n")
        for (i <- 0 until 64) {
                printf("%d ", outReg(i))
        }
        printf("\n")
        when (io.in.fire) {
            printf("Input matrix:\n")
            for (i <- 0 until 8) {
                for (j <- 0 until 8) {
                    printf("%d ", io.in.bits.matrixIn(i)(j))
                }
                printf("\n")
            }
            state := ZigZagState.processing
            inMatrix := io.in.bits.matrixIn
            validOut := false.B
            readyIn := false.B
        }
    } .elsewhen (state === ZigZagState.processing) {
        count := count + 1.U
        // printf("count: %d\n", count)
        printf("count: %d\n", count)
        when(count < 64.U) {
            // printf("in count %d\n", inMatrix(row)(col))
            printf("Input Matrix val: %d\n",inMatrix(row)(col) )
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
            state := ZigZagState.idle
            count := 0.U
            validOut := true.B
            readyIn := true.B
        }
    }

    io.in.ready := readyIn
    io.zigzagOut.bits := outReg
    io.zigzagOut.valid := validOut
}