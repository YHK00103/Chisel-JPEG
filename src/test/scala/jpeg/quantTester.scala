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
            val expectedArray: Seq[Seq[Int]] = expected
            for {
            row <- expectedArray
            } {
            val rowString = row.mkString("\t")
            println(rowString)
            }

            val bitsArray: Vec[Vec[SInt]] = dut.io.out.bits
            for {
            row <- bitsArray
            } {
            val rowString = row.map(_.peek()).mkString("\t")
            println(rowString)
            }
            
        }
    }

    behavior of "Quantization"

    it should "correctly quantize data" in {
        val data = jpeg.QuantizationData.in1
        val quantTable = jpeg.QuantizationTables.qt1
        doQuantizationTest(data, quantTable)
    }
}
