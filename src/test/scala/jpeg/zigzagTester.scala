package jpeg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.beans.beancontext.BeanContextChildSupport


class ZigZagChiselTester extends AnyFlatSpec with ChiselScalatestTester {
    def doZigZagChiselEncodeTest(data: Seq[Seq[Int]]): Unit = {
        test(new ZigZagChisel).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.io.in.valid.poke(true.B)
            dut.io.in.ready.expect(true.B)
            dut.io.state.expect(ZigZagState.idle)

            for (r <- 0 until 8) {
                for (c <- 0 until 8) {
                    dut.io.in.bits.matrixIn(r)(c).poke(data(r)(c).S)
                }
            }
            dut.clock.step()
            dut.io.state.expect(ZigZagState.processing)
            dut.io.in.ready.expect(false.B)
            dut.clock.step(64)

            val jpegEncoder = new jpegEncode(false, List.empty, 0)
            val expected = jpegEncoder.zigzagParse(data)
            for(i <- 0 until expected.length){
                dut.io.zigzagOut.bits(i).expect(expected(i).S)
            }
            dut.io.state.expect(ZigZagState.idle)
        }
    }


    def doZigZagChiselDecodeTest(data: Seq[Int]): Unit = {
        test(new ZigZagDecodeChisel).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.io.in.valid.poke(true.B)
            dut.io.in.ready.expect(true.B)
            dut.io.state.expect(ZigZagState.idle)

            for(i <- 0 until data.length){
                dut.io.in.bits.zigzagIn(i).poke(data(i).S)
            }
            dut.clock.step()
            dut.io.state.expect(ZigZagState.processing)
            dut.io.in.ready.expect(false.B)
            dut.clock.step(64)

            val jpegEncoder = new jpegEncode(false, List.empty, 0)
            val expected = jpegEncoder.zigzagDecode(data)
            for (r <- 0 until 8) {
                for (c <- 0 until 8) {
                    dut.io.matrixOut.bits(r)(c).expect(expected(r)(c).S)
                }
            }
            dut.io.state.expect(ZigZagState.idle)
        }
    }


    behavior of "ZigZagChisel"
    it should "zig zag 8x8 test 1" in {
        val test = jpeg.ZigZagParseData.in8x8
        doZigZagChiselEncodeTest(test)
    }

    it should "zig zag 8x8 test 2" in {
        val test = jpeg.QuantizationData.in2
        doZigZagChiselEncodeTest(test)
    }

    it should "zig zag 8x8 test 3" in {
        val test = jpeg.QuantizationData.in3
        doZigZagChiselEncodeTest(test)
    }


    behavior of "ZigZagChisel"
    it should "zig zag out8x8" in {
        val test = jpeg.ZigZagParseData.out8x8
        doZigZagChiselDecodeTest(test)
    }

    it should "zig zag out2x2" in {
        val test = jpeg.ZigZagParseData.out2x2
        doZigZagChiselDecodeTest(test)
    }

    it should "zig zag out3x3" in {
        val test = jpeg.ZigZagParseData.out3x3
        doZigZagChiselDecodeTest(test)
    }


}

