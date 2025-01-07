package jpeg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
  * 測試數據，用於驗證 Chisel IDCT 的實現
  */
object IDCTDataChisel {
  // DCT 輸出矩陣，作為 IDCT 的輸入
  val dctInput = Seq(
    Seq(-369, -30, -3, -2, -1, -4, -1, 0),
    Seq(-231, 45, 24, 0, 9, 4, 4, -1),
    Seq(63, 9, -8, -3, 0, -1, 1, -1),
    Seq(12, -15, -4, -3, 2, -1, 3, 0),
    Seq(-5, -4, 1, 4, 0, 5, 1, 0),
    Seq(0, 3, -1, 0, -1, -1, -1, 1),
    Seq(4, 2, -2, -2, 1, -3, 1, -1),
    Seq(-10, -2, 6, 0, 0, 0, -1, 0)
  )
  

  // IDCT 的預期輸出（重建的原始數據矩陣）
  val expectedOutput = Seq(
    Seq(-66, -73, -73, -74, -79, -80, -81, -73),
    Seq(-66, -71, -74, -76, -80, -81, -80, -75),
    Seq(-67, -68, -76, -79, -80, -81, -79, -74),
    Seq(-65, -67, -68, -68, -65, -63, -60, -63),
    Seq(-61, -61, -58, -54, -49, -43, -37, -36),
    Seq(-46, -33, -27, -22, -14, -13, -16, -11),
    Seq(-32, -17, -13, -9, 0, 0, 2, -1),
    Seq(-19, -7, -1, 5, 11, 13, 12, 5)
  )
}

/**
  * 用於測試 IDCT 的類別
  */
class IDCTChiselTest extends AnyFlatSpec with ChiselScalatestTester {

  /**
    * 測試 IDCT 的函數
    *
    * @param inputMatrix 測試用的 DCT 輸入矩陣
    * @param expectedOutput 預期的重建數據矩陣
    */
  def doIDCTChiselTest(inputMatrix: Seq[Seq[Int]], expectedOutput: Seq[Seq[Int]]): Unit = {
    test(new IDCTChisel) { dut =>
      // 設置輸入數據
      dut.io.in.valid.poke(true.B)
      for (i <- 0 until 8) {
        for (j <- 0 until 8) {
          dut.io.in.bits.matrixIn(i)(j).poke(inputMatrix(i)(j).S)
        }
      }

      // 進行計算
      dut.clock.step(3)

      // 驗證輸出
      dut.io.idctOut.valid.expect(true.B)
      for (i <- 0 until 8) {
        for (j <- 0 until 8) {
          dut.io.idctOut.bits(i)(j).expect(expectedOutput(i)(j).S)
        }
      }
    }
  }

  behavior of "IDCTChisel"

  it should "重建數據正確 (測試 IDCT)" in {
    val dctInput = IDCTDataChisel.dctInput
    val expectedOutput = IDCTDataChisel.expectedOutput
    doIDCTChiselTest(dctInput, expectedOutput)
  }
}
