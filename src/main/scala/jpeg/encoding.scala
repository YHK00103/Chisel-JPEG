package jpeg

import chisel3._
import chisel3.util._

object RLE {
    def apply(data: Vec[SInt], result: Vec[SInt]) = {
        val mod = Module(new RLE)
        mod.io.data := data
        result := mod.io.result
        mod
    }
}

class RLE extends Module {
    val io = IO(new Bundle{
        val data = Input(Vec(64, SInt(8.W)))
        val result = Output(Vec(64, SInt(8.W)))
    })

}

object Delta {
    def apply(data: Vec[SInt], result: Vec[SInt]) = {
        val mod = Module(new Delta)
        mod.io.data := data
        result := mod.io.result
        mod
    }
}

class Delta extends Module {
    val io = IO(new Bundle{
        val data = Input(Vec(64, SInt(8.W)))
        val result = Output(Vec(64, SInt(8.W)))
    })

}