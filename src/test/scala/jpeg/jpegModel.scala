package jpeg
import scala.math.ceil
import scala.math.round
import scala.math.{cos, Pi}
import scala.math

class jpegEncode(decompress: Boolean, quantTable: List[List[Int]], encoding: Int){
    
    def zigzagParse(matrix: Seq[Seq[Int]]): Seq[Int] = {
        var result: Seq[Int] = Seq.empty
        var i = 0
        var j = 0
        var isUp = true

        for (_ <- 0 until matrix.length * matrix.length) {
            result = result :+ matrix(i)(j)
            if (isUp) {
                if (j == matrix.length - 1) {
                    i += 1
                    isUp = false
                } 
                else if (i == 0) {
                    j += 1
                    isUp = false
                } 
                else {
                    i -= 1
                    j += 1
                }
            } 
            else {
                if (i == matrix.length - 1) {
                    j += 1
                    isUp = true
                } 
                else if (j == 0) {
                    i += 1
                    isUp = true
                } 
                else {
                    i += 1
                    j -= 1
                }
            }
        }
        result
    }


    // def DCT(matrix: Seq[Seq[Int]]): Seq[Seq[Double]] = {
    //     val dctMatrix = matrix.indices.map { u =>
    //         matrix.indices.map { v =>
    //         val sum = matrix.indices.foldLeft(0.0) { (accI, i) =>
    //             matrix.indices.foldLeft(accI) { (accJ, j) =>
    //             val pixelValue = matrix(i)(j).toDouble
    //             if (i == 2 && j == 2 && u == 1 && v == 2) {
    //                 println("cos:", cos((2 * i + 1) * u * Pi / 16) * cos((2 * j + 1) * v * Pi / 16) * 100)

    //             }
                
    //             val tempSum = accJ + pixelValue * ((cos((2 * i + 1) * u * Pi / 16) * cos((2 * j + 1) * v * Pi / 16)))
    //             if (u == 1 && v == 2) {
    //                 // println("alphaU: V:", alphaU, alphaV)
    //                 println("sum:", tempSum, pixelValue)
    //             }


    //             tempSum

    //             }

    //         }
    //         val alphaU = if (u == 0) 1 else math.sqrt(2) / 2
    //         val alphaV = if (v == 0) 1 else math.sqrt(2) / 2
    //         if (u == 1 && v == 2) {
    //             println("alphaU: V:", alphaU, alphaV)
    //             println("sum:", sum)
    //         }
                
    //         (alphaU * alphaV * sum / 4).toDouble
    //         }
    //     }
    //     println("DCT Matrix:")
    //     dctMatrix.foreach(row => println(row.map(_.formatted("%.2f")).mkString(" ")))
        
    //     dctMatrix
    // }


    def DCT(matrix: Seq[Seq[Int]]): Seq[Seq[Double]] = {
        val rows = matrix.length
        val cols = matrix.headOption.map(_.length).getOrElse(0)

        require(rows == 8 && cols == 8, "Input matrix must be 8x8")

        val dctMatrix = Array.ofDim[Double](8, 8)

        for (u <- 0 until 8) {
            for (v <- 0 until 8) {
                var sum = 0.0
                for (i <- 0 until 8) {
                    for (j <- 0 until 8) {
                        val pixelValue = matrix(i)(j)
                        val cosVal = cos((2 * i + 1) * u * Pi / 16) * cos((2 * j + 1) * v * Pi / 16)
                        sum += pixelValue * cosVal
                    }
                }
                val alphaU = if (u == 0) 1.0 / math.sqrt(2) else 1.0
                val alphaV = if (v == 0) 1.0 / math.sqrt(2) else 1.0
                dctMatrix(u)(v) = alphaU * alphaV * sum / 4
            }
        }

        println("DCT Matrix:")
        dctMatrix.foreach(row => println(row.map(_.formatted("%.2f")).mkString(" ")))
        dctMatrix.map(_.toSeq).toSeq
    }

    def printMatrix(matrix: Seq[Seq[Double]]): Unit = {
        for (row <- matrix) {
            println(row.mkString(" "))
        }
    }

    def roundToTwoDecimalPlaces(matrix: Seq[Seq[Double]]): Seq[Seq[Double]] = {
        matrix.map { row =>
            row.map { element =>
             BigDecimal(element).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
            }
        }
    }

    def roundToInt(matrix: Seq[Seq[Double]]): Seq[Seq[Double]] = {
        matrix.map { row =>
            row.map { element =>
                Math.round(element)
            }
        }
    }    

    def RLE(data: Seq[Int]): Seq[Int] = {
        var result = Seq[Int]()
        var current = data.head
        var count = 1
        
        for (i <- 1 until data.length) {
            if (data(i) == current) {
                count += 1
            } 
            else {
                result :+= count
                result :+= current
                current = data(i)
                count = 1
            }
        }
        
        result :+= count
        result :+= current
        
        result
    }

    def delta(data: Seq[Int]): Seq[Int] = {
        if (data.isEmpty) {
            Seq.empty[Int] 
        } 
        else {
            var result = Seq(data.head)
            var prev = data.head 

            for (i <- 1 until data.length) {
                val diff = data(i) - prev
                result :+= diff
                prev = data(i)
            }

            result
        }
    }

    def quantization(data: Seq[Seq[Int]], quantTable: Seq[Seq[Int]]): Seq[Seq[Int]] = {
        data.zip(quantTable).map { case (dataRow, quantRow) =>
                dataRow.zip(quantRow).map { case (d, q) =>
                val result = d.toDouble / q.toDouble
                if (result < 0) (round(-result) * -1).toInt else round(result).toInt
            }
        }
    }


}