package jpeg

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._
import java.rmi.dgc.DGC
import scala.math.round
import chisel3.experimental._
import scala.math.cos
import scala.math.Pi 
//import fixedpoint._

object DCTState extends ChiselEnum { 
    val loading, shifting, calculating, waiting = Value 
}

class DCTChisel extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new Bundle {
            val matrixIn = Input(Vec(8, Vec(8, SInt(9.W))))
        }))
        val shiftedOut = Output(Vec(8, Vec(8, SInt(9.W)))) // Test output to check shiftedblock
        val dctOut = Valid(Vec(8, Vec(8, SInt(16.W))))//FixedPoint(16.W, 8.BP))))
    })

    val matrixInput  = Reg(Vec(8, Vec(8, SInt(9.W))))
    val shiftedBlock = Reg(Vec(8, Vec(8, SInt(9.W))))
    val matrixOutput = Reg(Vec(8, Vec(8, SInt(16.W))))//FixedPoint(16.W, 8.BP)))) //SInt(9.W))))//
    val readyIn   = RegInit(true.B) 
    val validOut  = RegInit(false.B)


    io.in.ready  := readyIn
    io.dctOut.valid := validOut

    io.dctOut.bits := matrixOutput
    io.shiftedOut := DontCare

    // def DCT(matrix: Vec[Vec[SInt]]): Vec[Vec[SInt]] = {
    //     val dctMatrix = Wire(Vec(8, Vec(8, SInt(9.W)))) //FixedPoint(16.W, 8.BP))))
    //     // val Pi1 = scala.math.Pi.SInt//.toDouble //F(16.BP)

    //     // val cosLUT = Module(new CosineLUT(16, 1, 30))
    //     // val test = FixedPoint.fromDouble(2.35, 16.W, 8.BP)
  
    //     for (u <- 0 until 8) {
    //         for (v <- 0 until 8) {
    //             // var sum: FixedPoint = 0.F(8.BP)
    //             var sum = 0.S
    //             for (i <- 0 until 8) {
    //                 for (j <- 0 until 8) {
    //                     val pixelValue = matrix(i)(j)//.asFixedPoint(8.BP)
    //                     // println(s"pixelValue: $pixelValue\n")
    //                     printf("Pixel value at (%d, %d): %d\n", i.U, j.U, matrix(i)(j))
    //                     // printf("pixelValue: %d\n", pixelValue)
    //                     //val cosI = cos((2 * i + 1).toDouble * u.toDouble * Pi1 / 16.0)
    //                     //val cosJ = cos((2 * j + 1).toDouble * v.toDouble * Pi1 / 16.0)
                       
    //                     // var lutIndexI = round((2 * i + 1) * u * (3 / 16)) //.toInt
    //                     // var lutIndexJ = round((2 * j + 1) * v * (3 / 16)) //.toInt
                       
    //                     // printf("indexI: %d, indexJ: %d\n", lutIndexI.asUInt, lutIndexJ.asUInt)
    //                     // val cosI = cosLUT(lutIndexI.toInt).asFixedPoint(8.BP)
    //                     // val cosJ = cosLUT(lutIndexJ.toInt).asFixedPoint(8.BP)
                       
    //                     // val cosI = rom(lutIndexI.asUInt)
    //                     // val cosJ = rom(lutIndexJ.asUInt)
    //                     // printf("cosI: %d, cosJ: $%d\n", cosI, cosJ)
                        
    //                     // sum = sum.asSInt + pixelValue.asSInt * cosI.asSInt * cosJ.asSInt//.toInt
    //                     // printf("sum: %d\n", sum)
    //                     // println(s"sum: $sum\n")
    //                 }
    //             }
    //             // val alphaU = if (u == 0) 1.S else 1.S/ FixedPoint.fromDouble(math.sqrt(2), 16.W, 8.BP).asSInt
    //             // val alphaV = if (v == 0) 1.S else 1.S / FixedPoint.fromDouble(math.sqrt(2), 16.W, 8.BP).asSInt
    //             // var calc = (alphaU * alphaV * sum) 
    //             // printf("calc: %d\n", calc)

    //             dctMatrix(u)(v) := 1.S//calc >> 2 /// 4.U)
    //         }
    //     }
    //     dctMatrix
    // }
    // def DCT(matrix: Vec[Vec[SInt]]): Vec[Vec[SInt]] = {
    //     val dctMatrix = Wire(Vec(8, Vec(8, SInt(8.W)))) //FixedPoint(16.W, 8.BP))))

    //     val C = Array.tabulate(8, 8) { (u, v) =>
    //         val cu = if (u == 0) math.sqrt(1.0 / 8.0) else 0.5 
    //         val cv = if (v == 0) math.sqrt(1.0 / 8.0) else 0.5 
    //         (cu * cv * math.cos(((2 * v + 1) * u * math.Pi) / 16.0) * 256).toInt 
    //     }

    //     for (u <- 0 until 8) {
    //         for (v <- 0 until 8) {
    //             var sum = 0.S
    //             for (i <- 0 until 8) {
    //                 for (j <- 0 until 8) {
    //                     val pixelValue = matrix(i)(j)
    //                     val cosVal = C(i)(u).S * C(j)(v).S
    //                     sum += pixelValue * cosVal
    //                 }
    //             }
    //             dctMatrix(u)(v) := sum >> 8
    //         }
    //     }

    //     dctMatrix
    // }

// def DCT(matrix: Vec[Vec[SInt]]): Vec[Vec[SInt]] = {
//   val dctMatrix = Wire(Vec(8, Vec(8, SInt(16.W))))

//   // Compute DCT
//   for (u <- 0 until 8) {
//     for (v <- 0 until 8) {
//       var sum = 0.S
//       for (i <- 0 until 8) {
//         for (j <- 0 until 8) {
//           val pixelValue = matrix(i)(j)
//           //printf("pixel val: %d i: %d j: %d", pixelValue, i.S, j.S)
//           val cosVal = (math.cos((2 * i + 1) * u * Pi / 16) * math.cos((2 * j + 1) * v * Pi / 16)).toInt.S
//           sum += pixelValue * cosVal
//         }
//       }
//       val alphaU = if (u == 0) 1 else math.sqrt(2) / 2
//       val alphaV = if (v == 0) 1 else math.sqrt(2) / 2
//       printf("alphaU: %d V: %d sum: %d\n", alphaU.toInt.S, alphaV.toInt.S, sum)
//       val scaledSum = (alphaU.toInt.S * alphaV.toInt.S * sum / 4.S)//.toInt.S
//       printf("added val: %d at u: %d v: %d\n",scaledSum, u.S, v.S )
//       dctMatrix(u)(v) := scaledSum

//     }
//   }
// //    // Print dctMatrix
// //     printf("dctMatrix:\n")
// //     for (u <- 0 until 8) {
// //       for (v <- 0 until 8) {
// //         printf("%d ", dctMatrix(u)(v))
// //       }
// //       printf("\n")
// //     }

//   dctMatrix
// }


def DCT(matrix: Vec[Vec[SInt]]): Vec[Vec[SInt]] = {
  val dctMatrix = Wire(Vec(8, Vec(8, SInt(16.W))))

  // Compute DCT
  for (u <- 0 until 8) {
    for (v <- 0 until 8) {
      var sum = 0.S
      for (i <- 0 until 8) {
        for (j <- 0 until 8) {
          val pixelValue = matrix(i)(j)
          // Scale the cosine values to preserve precision
          val cosVal = (math.cos((2 * i + 1) * u * Pi / 16) * math.cos((2 * j + 1) * v * Pi / 16) * 100).toInt.S
          if ((i == 2) && (j == 2) && (u == 1) && (v == 2)) {
            //printf("cosval: %f", (math.cos((2 * i + 1) * u * Pi / 16) * math.cos((2 * j + 1) * v * Pi / 16) * 100))//.toInt.S
            //printf("cosval: %d.%02d", (math.cos((2 * i + 1) * u * Pi / 16) * math.cos((2 * j + 1) * v * Pi / 16) * 100).toInt, ((math.cos((2 * i + 1) * u * Pi / 16) * math.cos((2 * j + 1) * v * Pi / 16) * 100) * 100).toInt % 100.U)
            //printf("cosval: ")
            println("cosval: ", cosVal, i, j, u, v)
          }

          sum += pixelValue * cosVal
        }
      }
      val alphaU = if (u == 0) 1 else math.sqrt(2) / 2
      val alphaV = if (v == 0) 1 else math.sqrt(2) / 2
      val scaledSum = (alphaU.toInt.S * alphaV.toInt.S * sum / 4.S)
      dctMatrix(u)(v) := scaledSum

    }
  }

  dctMatrix
}



    val state = RegInit(DCTState.waiting)
    when(state === DCTState.waiting) {

        // Print content of matrixOutput when it's in the waiting state
        printf("Content of matrixOutput in waiting state:\n")
        for (i <- 0 until 8) {
            for (j <- 0 until 8) {
                printf("%d ", matrixOutput(i)(j))
            }
            printf("\n")
        }
        printf("\n")

        when (io.in.fire) {
            matrixInput := io.in.bits.matrixIn
            state := DCTState.shifting
            validOut := false.B
            readyIn := false.B
        }
    } .elsewhen (state === DCTState.shifting) {
        for (i <- 0 until 8) {
            for (j <- 0 until 8) {
                shiftedBlock(i)(j) := io.in.bits.matrixIn(i)(j) -& 128.S
            }
        }
        io.shiftedOut := shiftedBlock
        state := DCTState.calculating
    } .elsewhen (state === DCTState.calculating) {

        matrixOutput := DCT(shiftedBlock)
        state := DCTState.waiting

    } //.elsewhen (state === DCTState.waiting)

}

