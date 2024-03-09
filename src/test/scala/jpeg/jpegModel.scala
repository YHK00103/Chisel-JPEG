package jpeg
import scala.math.ceil
import scala.math.round
import scala.math.{cos, Pi}
import scala.math._

class jpegEncode(decompress: Boolean, quantTable: List[List[Int]], encoding: Int){
    
    // Helper func for ZigZag to calculate indicies
    def updateIndices(n: Int, isUp: Boolean, i: Int, j: Int): (Int, Int, Boolean) = {
        if (isUp) {
            if (j == n - 1) {
                (i + 1, j, false)
            } else if (i == 0) {
                (i, j + 1, false)
            } else {
                (i - 1, j + 1, isUp)
            }
        } else {
            if (i == n - 1) {
                (i, j + 1, true)
            } else if (j == 0) {
                (i + 1, j, true)
            } else {
                (i + 1, j - 1, isUp)
            }
        }
    }

    def zigzagParse(matrix: Seq[Seq[Int]]): Seq[Int] = {
        var result: Seq[Int] = Seq.empty
        var i = 0
        var j = 0
        var isUp = true
        val matSize = matrix.length

        for (num <- 0 until matSize * matSize) {
            result = result :+ matrix(i)(j)
            val (newI, newJ, newIsUp) = updateIndices(matSize, isUp, i, j)
            i = newI
            j = newJ
            isUp = newIsUp
        }
        result
    }


    def zigzagDecode(data: Seq[Int]): Seq[Seq[Int]] = {
        var i = 0
        var j = 0
        var isUp = true
        val matSize = sqrt(data.length).toInt
        var result = Array.ofDim[Int](matSize, matSize)

        for (elem <- 0 until matSize * matSize) {
            result(i)(j) = data(elem)
            val (newI, newJ, newIsUp) = updateIndices(matSize, isUp, i, j)
            i = newI
            j = newJ
            isUp = newIsUp
        }
        result.map(_.toSeq).toSeq
    }


    def DCT(matrix: Seq[Seq[Int]]): Seq[Seq[Double]] = {
        // Implement Discrete Cosine Transform algorithm here
        val dctMatrix = matrix.indices.map { u =>
            matrix.indices.map { v =>
            val sum = matrix.indices.foldLeft(0.0) { (accI, i) =>
                matrix.indices.foldLeft(accI) { (accJ, j) =>
                val pixelValue = matrix(i)(j).toDouble
                val tempSum = accJ + pixelValue * cos((2 * i + 1) * u * Pi / 16) * cos((2 * j + 1) * v * Pi / 16)
                tempSum
                }
            }
            val alphaU = if (u == 0) 1 else math.sqrt(2) / 2
            val alphaV = if (v == 0) 1 else math.sqrt(2) / 2
            (alphaU * alphaV * sum / 4).toDouble
            }
        }
        dctMatrix
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

    def decodeDelta(data: Seq[Int]): Seq[Int] = {
        if (data.isEmpty) {
            Seq.empty[Int]
        } else {
            var result = Seq(data.head)
            var prev = data.head

            for (i <- 1 until data.length) {
                val original = data(i) + prev
                result :+= original
                prev = original
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