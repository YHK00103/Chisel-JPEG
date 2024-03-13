package jpeg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.beans.beancontext.BeanContextChildSupport


class ZigZagChiselTester extends AnyFlatSpec with ChiselScalatestTester {
    def doZigZagChiselEncodeTest(data: Seq[Seq[Int]]): Unit = {
        val p = JpegParams(8, 8, 0)
        test(new ZigZagChisel(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.io.in.valid.poke(true.B)
            dut.io.in.ready.expect(true.B)
            dut.io.state.expect(ZigZagState.idle)

            for (r <- 0 until p.numRows) {
                for (c <- 0 until p.numCols) {
                    dut.io.in.bits.matrixIn(r)(c).poke(data(r)(c).S)
                }
            }
            dut.clock.step()
            dut.io.state.expect(ZigZagState.processing)
            dut.io.in.ready.expect(false.B)
            dut.clock.step(p.totalElements)

            val jpegEncoder = new jpegEncode(false, List.empty, 0)
            val expected = jpegEncoder.zigzagParse(data)
            for(i <- 0 until expected.length){
                dut.io.zigzagOut.bits(i).expect(expected(i).S)
            }
            dut.io.state.expect(ZigZagState.idle)
        }
    }

    behavior of "ZigZagChisel"
    it should "zig zag encode ZigZagParseData.in8x8" in {
        val test = jpeg.ZigZagParseData.in8x8
        doZigZagChiselEncodeTest(test)
    }

    it should "zig zag encode QuantizationData.in2" in {
        val test = jpeg.QuantizationData.in2
        doZigZagChiselEncodeTest(test)
    }

    it should "zig zag encode QuantizationData.in3" in {
        val test = jpeg.QuantizationData.in3
        doZigZagChiselEncodeTest(test)
    }
}

class ZigZagChiselDecodeTester extends AnyFlatSpec with ChiselScalatestTester {
    def doZigZagChiselDecodeTest(data: Seq[Int]): Unit = {
        val p = JpegParams(8, 8, 0)
        test(new ZigZagDecodeChisel(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.io.in.valid.poke(true.B)
            dut.io.in.ready.expect(true.B)
            dut.io.state.expect(ZigZagState.idle)

            for(i <- 0 until data.length){
                dut.io.in.bits.zigzagIn(i).poke(data(i).S)
            }
            dut.clock.step()
            dut.io.state.expect(ZigZagState.processing)
            dut.io.in.ready.expect(false.B)
            dut.clock.step(p.totalElements)

            val jpegEncoder = new jpegEncode(false, List.empty, 0)
            val expected = jpegEncoder.zigzagDecode(data)
            for (r <- 0 until p.numRows) {
                for (c <- 0 until p.numRows) {
                    dut.io.matrixOut.bits(r)(c).expect(expected(r)(c).S)
                }
            }
            dut.io.state.expect(ZigZagState.idle)
        }
    }
    behavior of "ZigZagDecodeChisel"
    it should "zig zag decode out8x8" in {
        val test = jpeg.ZigZagParseData.out8x8
        doZigZagChiselDecodeTest(test)
    }

    it should "zig zag decode QuantizationData.in2" in {
        val test = jpeg.QuantizationData.in2.flatten
        doZigZagChiselDecodeTest(test)
    }

    it should "zig zag decode QuantizationData.in3" in {
        val test = jpeg.QuantizationData.in3.flatten
        doZigZagChiselDecodeTest(test)
    }
}

