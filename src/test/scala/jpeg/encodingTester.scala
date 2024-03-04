package jpeg

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.language.experimental

class RLETest extends AnyFlatSpec with ChiselScalatestTester {
    def doRLETest(data: Seq[Int]): Unit = {
        test(new RLE).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.io.in.valid.poke(true.B)
            dut.io.in.ready.expect(true.B)
            dut.io.state.expect(RLEState.idle)
            for (i <- 0 until 64) {
                dut.io.in.bits.data(i).poke(data(i).S)
            }
            dut.clock.step()
            dut.io.state.expect(RLEState.encode)
            dut.io.in.ready.expect(false.B)
            dut.clock.step(64)
            
            val jpegEncoder = new jpegEncode(false, List.empty, 0)
            val expected = jpegEncoder.RLE(data)
            dut.io.length.bits.expect(expected.length)
            for(i <- 0 until expected.length){
                dut.io.out.bits(i).expect(expected(i).S)
            }
            dut.io.state.expect(RLEState.idle)

            // // Printing each element of the array
            // val bitsArray: Vec[SInt] = dut.io.out.bits
            // for (element <- bitsArray) {
            //     println(element.peek())
            // }
        }
    }

    behavior of "RLEChisel"
    it should "correctly encode (1, 3, 6, 10)" in {
        val test = Seq(5, 5, 5, 6, 6, 7, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 10, 10, 11, 11, 11, 12, 12, 12, 12, 12, 13, 13, 13, 13, 14, 14, 14, 14, 15, 15, 15, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 20, 20, 20)
        doRLETest(test)
    }

}

class DeltaTest extends AnyFlatSpec with ChiselScalatestTester {
    def doDeltaTest(data: Seq[Int]): Unit = {
        test(new Delta).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.io.in.valid.poke(true.B)
            dut.io.in.ready.expect(true.B)
            dut.io.state.expect(DeltaState.idle)
            for (i <- 0 until 64) {
                dut.io.in.bits.data(i).poke(data(i).S)
            }
            dut.clock.step()

            dut.io.state.expect(DeltaState.encode)
            dut.io.in.ready.expect(false.B)
            dut.clock.step(64)
            val jpegEncoder = new jpegEncode(false, List.empty, 0)
            val expected = jpegEncoder.delta(data)
            for( i <- 0 until 64){
                dut.io.out.bits(i).expect(expected(i).S)
            }
            dut.io.state.expect(DeltaState.idle)

        }
    }

    behavior of "DeltaChisel"
    it should "correctly encode (1, 3, 6, 10)" in {
        val test = Seq(58, 21, 5, 3, 19, 27, 47, 34, 44, 56, 63, 61, 26, 59, 8, 51, 11, 12, 62, 14, 25, 16, 64, 22, 60, 33, 29, 2, 45, 54, 28, 32, 43, 6, 57, 46, 48, 9, 39, 10, 41, 50, 4, 30, 49, 37, 36, 55, 13, 7, 52, 53, 15, 31, 23, 38, 35, 18, 40, 42, 1, 24, 17, 20)
        doDeltaTest(test)
    }

}