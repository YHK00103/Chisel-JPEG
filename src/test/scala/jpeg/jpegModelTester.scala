package jpeg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.beans.beancontext.BeanContextChildSupport

object ZigZagParseData {
    val in2x2 = Seq(Seq(1, 2),
                    Seq(3, 4))

    val in3x3 = Seq(Seq(1, 2, 6),
                    Seq(3, 5, 7),
                    Seq(4, 8, 9))
    
    val in4x4 = Seq(Seq(10, 11, 12, 13),
                    Seq(14, 15, 16, 17),
                    Seq(18, 19, 20, 21),
                    Seq(22, 23, 24, 25))

    val in8x8 = Seq(Seq(10, 11, 12, 13, 14, 15, 16, 17),
                    Seq(18, 19, 20, 21, 22, 23, 24, 25),
                    Seq(26, 27, 28, 29, 30, 31, 32, 33),
                    Seq(34, 35, 36, 37, 38, 39, 40, 41),
                    Seq(42, 43, 44, 45, 46, 47, 48, 49),
                    Seq(50, 51, 52, 53, 54, 55, 56, 57),
                    Seq(58, 59, 60, 61, 62, 63, 64, 65),
                    Seq(66, 67, 68, 69, 70, 71, 72, 73))

    val out2x2 = Seq(1, 2, 3, 4)
    val out3x3 = Seq(1, 2, 3, 4, 5, 6, 7, 8, 9)
    val out4x4 = Seq(10, 11, 14, 18, 15, 12, 13, 16, 19, 22, 23, 20, 17, 21, 24, 25)
    val out8x8 = Seq(10, 11, 18, 26, 19, 12, 13, 20, 27, 34, 42, 35, 28, 21, 14, 15, 22, 29, 36, 43, 50, 58, 51, 44, 37, 30, 23, 16, 17, 24, 31, 38, 45, 52, 59, 66, 67, 60, 53, 46, 39, 32, 25, 33, 40, 47, 54, 61, 68, 69, 62, 55, 48, 41, 49, 56, 63, 70, 71, 64, 57, 65, 72, 73)
}

object RLEData {
    val in1 = Seq(1, 1, 1, 2, 2, 3, 3, 3, 3)
    val in2 = Seq(5, 5, 5, 5, 3, 3, 1, 1, 1)
    val in3 = Seq(4, 4, 4, 4, 4)
    val in4 = Seq(1, 2, 3, 4 ,5)
    
    val out1 = Seq(3, 1, 2, 2, 4, 3)
    val out2 = Seq(4, 5, 2, 3, 3, 1)
    val out3 = Seq(5, 4)
    val out4 = Seq(1, 1, 1, 2, 1, 3, 1, 4, 1, 5)
}

object deltaData {
    val in1 = Seq(1, 3, 6, 10)
    val in2 = Seq(10, 7, 4, 2)
    val in3 = Seq(5, 5, 5, 5)
    val in4 = Seq.empty[Int]
    val in5 = Seq(100)

    val out1 = Seq(1, 2, 3, 4)
    val out2 = Seq(10, -3, -3, -2)
    val out3 = Seq(5, 0, 0, 0)
    val out4 = Seq.empty[Int]
    val out5 = Seq(100)

}

object QuantizationTables {
    val qEmpty = Seq(Seq(0, 0, 0, 0, 0, 0, 0, 0),
                  Seq(0, 0, 0, 0, 0, 0, 0, 0),
                  Seq(0, 0, 0, 0, 0, 0, 0, 0),
                  Seq(0, 0, 0, 0, 0, 0, 0, 0),
                  Seq(0, 0, 0, 0, 0, 0, 0, 0),
                  Seq(0, 0, 0, 0, 0, 0, 0, 0),
                  Seq(0, 0, 0, 0, 0, 0, 0, 0),
                  Seq(0, 0, 0, 0, 0, 0, 0, 0))

    val qt1 = Seq(Seq(16, 11, 10, 16, 24, 40, 51, 61),
                  Seq(12, 12, 14, 19, 26, 58, 60, 55),
                  Seq(14, 13, 16, 24, 40, 57, 69, 56),
                  Seq(14, 17, 22, 29, 51, 87, 80, 62),
                  Seq(18, 22, 37, 56, 68, 109, 103, 77),
                  Seq(24, 35, 55, 64, 81, 104, 113, 92),
                  Seq(49, 64, 78, 87, 103, 121, 120, 101),
                  Seq(72, 992, 95, 98, 112, 100, 103, 99))

    val qt2 = Seq(Seq(17, 18, 24, 47, 99, 99, 99, 99),
                  Seq(18, 21, 26, 66, 99, 99, 99, 99),
                  Seq(24, 26, 56, 99, 99, 99, 99, 99),
                  Seq(47, 66, 99, 99, 99, 99, 99, 99),
                  Seq(99, 99, 99, 99, 99, 99, 99, 99),
                  Seq(99, 99, 99, 99, 99, 99, 99, 99),
                  Seq(99, 99, 99, 99, 99, 99, 99, 99),
                  Seq(99, 99, 99, 99, 99, 99, 99, 99))
}

object QuantizationData {
    val in1 = Seq(Seq(-415, -33, -58, 35, 58, -51, -15, -12),
                  Seq(5, -34, 49, 18, 27, 1, -5, 3),
                  Seq(-46, 14, 80, -35, -50, 19, 7, -18),
                  Seq(-53, 21, 34, -20, 2, 34, 36, 12),
                  Seq(9, -2, 9, -5, -32, -15, 45, 37),
                  Seq(-8, 15, -16, 7, -8, 11, 4, 7),
                  Seq(19, -28, -2, -26, -2, 7, -44, -21),
                  Seq(18, 25, -12, -44, 35, 48, -37, -3))

    val in2 = Seq(Seq(100, -33, -58, 35, 58, -51, -15, -12),
                  Seq(5, -34, 49, 18, 27, 1, -5, 3),
                  Seq(-46, 14, 80, -35, -50, 19, 7, -18),
                  Seq(-53, 21, 34, -20, 2, 34, 36, 12),
                  Seq(9, -2, 9, -5, -32, -15, 45, 37),
                  Seq(-8, 15, -16, 7, -8, 11, 4, 7),
                  Seq(19, -28, -2, -26, -2, 7, -44, -21),
                  Seq(18, 25, -12, -44, 35, 48, -37, -3))

    val in3 = Seq(Seq(120, -40, 80, 60, 90, -50, -30, 45),
                  Seq(10, -30, 45, 25, 35, 15, -20, 30),
                  Seq(-25, 35, 55, -15, 20, 40, 50, -10),
                  Seq(5, -20, 30, 40, 25, 10, -15, 20),
                  Seq(-10, 15, 20, 10, -5, 25, 30, -10),
                  Seq(60, -30, -35, 20, 10, 15, -25, 35),
                  Seq(30, -25, 40, 15, -10, 20, 25, -20),
                  Seq(25, 15, -10, -20, 30, 45, -35, -15))
                  
    val out1qt1 = Seq(Seq(-26, -3, -6, 2, 2, -1, 0, 0),
                      Seq(0, -3, 4, 1, 1, 0, 0, 0),
                      Seq(-3, 1, 5, -1, -1, 0, 0, 0),
                      Seq(-4, 1, 2, -1, 0, 0, 0, 0),
                      Seq(1, 0, 0, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0))

    val out1qt2 = Seq(Seq(-24, -2, -2, 1, 1, -1, 0, 0),
                      Seq(0, -2, 2, 0, 0, 0, 0, 0),
                      Seq(-2, 1, 1, 0, -1, 0, 0, 0),
                      Seq(-1, 0, 0, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0))

    val out2qt1 = Seq(Seq(6, -3, -6, 2, 2, -1, 0, 0),
                     Seq(0, -3, 4, 1, 1, 0, 0, 0),
                     Seq(-3, 1, 5, -1, -1, 0, 0, 0),
                     Seq(-4, 1, 2, -1, 0, 0, 0, 0),
                     Seq(1, 0, 0, 0, 0, 0, 0, 0),
                     Seq(0, 0, 0, 0, 0, 0, 0, 0),
                     Seq(0, 0, 0, 0, 0, 0, 0, 0),
                     Seq(0, 0, 0, 0, 0, 0, 0, 0))

    val out2qt2 = Seq(Seq(6, -2, -2, 1, 1, -1, 0, 0),
                      Seq(0, -2, 2, 0, 0, 0, 0, 0),
                      Seq(-2, 1, 1, 0, -1, 0, 0, 0),
                      Seq(-1, 0, 0, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0))

    val out3qt1 = Seq(Seq(8, -4, 8, 4, 4, -1, -1, 1),
                      Seq(1, -3, 3, 1, 1, 0, 0, 1),
                      Seq(-2, 3, 3, -1, 1, 1, 1, 0),
                      Seq(0, -1, 1, 1, 0, 0, 0, 0),
                      Seq(-1, 1, 1, 0, 0, 0, 0, 0),
                      Seq(3, -1, -1, 0, 0, 0, 0, 0),
                      Seq(1, 0, 1, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0))

    val out3qt2 = Seq(Seq(7, -2, 3, 1, 1, -1, 0, 0),
                      Seq(1, -1, 2, 0, 0, 0, 0, 0),
                      Seq(-1, 1, 1, 0, 0, 0, 1, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0),
                      Seq(1, 0, 0, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0),
                      Seq(0, 0, 0, 0, 0, 0, 0, 0))
}

object DCTData {
    val in1 = Seq(Seq(231, 32, 233, 161, 24, 71, 140, 245),
                Seq(247, 40, 248, 245, 124, 204, 36, 107),
                Seq(234, 202, 245, 167, 9, 217, 239, 173),
                Seq(193, 190, 100, 167, 43, 180, 8, 70),
                Seq(11, 24, 210, 177, 81, 243, 8, 112),
                Seq(97, 195, 203, 47, 125, 114, 165, 181),
                Seq(193, 70, 174, 167, 41, 30, 127, 245),
                Seq(87, 149, 57, 192, 65, 129, 178, 228))
    
    val out1 = Seq(Seq(2237.50, 44.02, 75.92, -138.57, 3.50, 122.08, 195.04, -101.60),
                Seq(77.19, 57.43, -10.90, 20.68, 4.39, 49.54, 69.09, 5.45),
                Seq(44.84, -31.38, 55.81, -38.19, 62.21, 47.80, -19.91, 29.26),
                Seq(-69.98, -20.12, -11.75, -38.37, 13.32, -18.42, 33.09, 62.71),
                Seq(-109.00, -21.67, -27.77, 4.09, 15.12, -14.33, 1.22, -47.07),
                Seq(-5.39, 28.32, 86.51, -17.71, 16.19, 16.73, -29.06, 9.51),
                Seq(78.84, -32.30, 59.34, -7.55, -68.66, -15.31, -52.56, 19.91),
                Seq(19.79, -39.09, 0.49, -36.17, -10.79, 40.65, 31.86, 2.95))
}

class ZigZagParseTester extends AnyFlatSpec with ChiselScalatestTester {
    it should "Zig Zag 2x2" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.zigzagParse(ZigZagParseData.in2x2) == ZigZagParseData.out2x2)
    }

    it should "Zig Zag 3x3" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.zigzagParse(ZigZagParseData.in3x3) == ZigZagParseData.out3x3)
    }

    it should "Zig Zag 4x4" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.zigzagParse(ZigZagParseData.in4x4) == ZigZagParseData.out4x4)
    }

    it should "Zig Zag 8x8" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.zigzagParse(ZigZagParseData.in8x8) == ZigZagParseData.out8x8)
    }
}

class RLETester extends AnyFlatSpec with ChiselScalatestTester {
    it should "RLE test 1" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.RLE(RLEData.in1) == RLEData.out1)
    }

    it should "RLE test 2" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.RLE(RLEData.in2) == RLEData.out2)
    }

    it should "RLE test 3" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.RLE(RLEData.in3) == RLEData.out3)
    }

    it should "RLE test no dupes" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.RLE(RLEData.in4) == RLEData.out4)
    }

}

class deltaTester extends AnyFlatSpec with ChiselScalatestTester {
    it should "delta test 1" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.delta(deltaData.in1) == deltaData.out1)
    }

    it should "delta test 2" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.delta(deltaData.in2) == deltaData.out2)
    }

    it should "delta test 3" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.delta(deltaData.in3) == deltaData.out3)
    }

    it should "delta test empty" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.delta(deltaData.in4) == deltaData.out4)
    }

    it should "delta test single elem" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.delta(deltaData.in5) == deltaData.out5)
    }

}

class quantizationTester extends AnyFlatSpec with ChiselScalatestTester {
    it should "in1 / quant table 1" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.quantization(QuantizationData.in1, QuantizationTables.qt1) == QuantizationData.out1qt1)
    }
  
    it should "in1 / quant table 2" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.quantization(QuantizationData.in1, QuantizationTables.qt2) == QuantizationData.out1qt2)
    }

    it should "in2 / quant table 1" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.quantization(QuantizationData.in2, QuantizationTables.qt1) == QuantizationData.out2qt1)
    }

    it should "in2 / quant table 2" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.quantization(QuantizationData.in2, QuantizationTables.qt2) == QuantizationData.out2qt2)
    }

    it should "in3 / quant table 1" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.quantization(QuantizationData.in3, QuantizationTables.qt1) == QuantizationData.out3qt1)
    }

    it should "in3 / quant table 2" in {
        val jpegEncoder = new jpegEncode(false, List.empty, 0)
        assert(jpegEncoder.quantization(QuantizationData.in3, QuantizationTables.qt2) == QuantizationData.out3qt2)
    }
}


class dctTester extends AnyFlatSpec with ChiselScalatestTester {
    it should "dct test 1" in {
        // val zerosTable: Seq[Seq[Int]] = Seq.fill(8)(Seq.fill(8)(0))
        val jpegEncode = new jpegEncode(false, List.empty, 0)
        //assert(jpegEncode.dct())
        val dctResult = jpegEncode.DCT(DCTData.in1)
        val rounded = jpegEncode.roundToTwoDecimalPlaces(dctResult)
        jpegEncode.printMatrix(dctResult)
        jpegEncode.printMatrix(rounded)
        assert(rounded == DCTData.out1)

    }
}

