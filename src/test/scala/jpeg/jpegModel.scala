package jpeg

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
                } else if (i == 0) {
                    j += 1
                    isUp = false
                } else {
                    i -= 1
                    j += 1
                }
            } else {
                if (i == matrix.length - 1) {
                    j += 1
                    isUp = true
                } else if (j == 0) {
                    i += 1
                    isUp = true
                } else {
                    i += 1
                    j -= 1
                }
            }
        }
        result
    }


    def DCT(p: jpegParams, matrix: List[List[Int]]): List[List[Int]] = {
        // Implement Discrete Cosine Transform algorithm here
        ???
    }

}