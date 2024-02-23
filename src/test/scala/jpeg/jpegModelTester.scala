package jpeg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

object ZigZagParseData {
    val in2x2 = Seq(Seq(1,2),
                    Seq(3,4))

    val in3x3 = Seq(Seq(1,2,6),
                    Seq(3,5,7),
                    Seq(4,8,9))
    
    val in4x4 = Seq(Seq(10,11,12,13),
                    Seq(14,15,16,17),
                    Seq(18,19,20,21),
                    Seq(22,23,24,25))

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