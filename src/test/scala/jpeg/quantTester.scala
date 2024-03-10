package jpeg

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class QuantizationTest extends AnyFlatSpec with ChiselScalatestTester {
    def doQuantizationTest(data: Seq[Seq[Int]], quantTable: Seq[Seq[Int]]): Unit = {
        test(new Quantization).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.io.in.valid.poke(true.B)
            dut.io.in.ready.expect(true.B)
            dut.io.state.expect(QuantState.idle)

            for (r <- 0 until 8) {
                for (c <- 0 until 8) {
                    dut.io.in.bits.data(r)(c).poke(data(r)(c).S)
                }
            } 

            for (r <- 0 until 8) {
                for (c <- 0 until 8) {
                    dut.io.in.bits.quantTable(r)(c).poke(quantTable(r)(c).S)
                }
            }
            dut.clock.step()
            dut.io.in.ready.expect(false.B)
            dut.io.out.valid.expect(false.B)
            dut.clock.step(64)

            val jpegEncoder = new jpegEncode(false, List.empty, 0)
            val expected = jpegEncoder.quantization(data, quantTable)
            dut.io.state.expect(QuantState.idle)

            /* 
                For Testing purposes, prints out both the expected and actual results
             */
            // println("scala expected:")
            // val expectedArray: Seq[Seq[Int]] = expected
            // for {
            // row <- expectedArray
            // } {
            // val rowString = row.mkString("\t")
            // println(rowString)
            // }

            // println("Chisel actual:")
            // val bitsArray: Vec[Vec[SInt]] = dut.io.out.bits
            // for {
            // row <- bitsArray
            // } {
            // val rowString = row.map(_.peek()).mkString("\t")
            // println(rowString)
            // }


            for (r <- 0 until 8) {
                for (c <- 0 until 8) {
                    dut.io.out.bits(r)(c).expect(expected(r)(c).S)
                }
            }
            dut.io.state.expect(QuantState.idle)
        }
    }

    behavior of "Quantization"

    it should "correctly quantize in1 with qt1" in {
        val data = jpeg.QuantizationData.in1
        val quantTable = jpeg.QuantizationTables.qt1
        doQuantizationTest(data, quantTable)
    }

    it should "correctly quantize in1 with qt2" in {
        val data = jpeg.QuantizationData.in1
        val quantTable = jpeg.QuantizationTables.qt2
        doQuantizationTest(data, quantTable)
    }

    it should "correctly quantize in2 with qt1" in {
        val data = jpeg.QuantizationData.in2
        val quantTable = jpeg.QuantizationTables.qt1
        doQuantizationTest(data, quantTable)
    }

    it should "correctly quantize in2 with qt2" in {
        val data = jpeg.QuantizationData.in2
        val quantTable = jpeg.QuantizationTables.qt2
        doQuantizationTest(data, quantTable)
    }

    it should "correctly quantize in3 with qt1" in {
        // val data = jpeg.QuantizationData.in3
        val in3 = Seq(Seq(120, -40, 80, 60, 90, -50, -30, 45),
                  Seq(10, -30, 45, 25, 35, 15, -20, 30),
                  Seq(-25, 35, 55, -15, 20, 40, 50, -10),
                  Seq(5, -20, 30, 40, 26, 10, -15, 20),
                  Seq(-10, 15, 20, 10, -5, 25, 30, -10),
                  Seq(60, -30, -35, 20, 10, 15, -25, 35),
                  Seq(30, -25, 40, 15, -10, 20, 25, -20),
                  Seq(25, 15, -10, -20, 30, 45, -35, -15))
        val quantTable = jpeg.QuantizationTables.qt1
        doQuantizationTest(in3, quantTable)
    }

    it should "correctly quantize in3 with qt2" in {
        val data = jpeg.QuantizationData.in3
        val quantTable = jpeg.QuantizationTables.qt2
        doQuantizationTest(data, quantTable)
    }
}
