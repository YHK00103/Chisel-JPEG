package jpeg

import chisel3._
import chisel3.util._
import scala.math.cos
import scala.math.Pi

/**
  * Object for IDCT
  */
object IDCTChisel {
    def apply(matrixIn: Valid[Vec[Vec[SInt]]], idctOut: Valid[Vec[Vec[SInt]]]) = {
        val mod = Module(new IDCTChisel)
        mod.io.in := matrixIn
        mod.io.idctOut := idctOut
        mod
    }
}

/**
  * Creates FSM states for IDCT
  */
object IDCTState extends ChiselEnum {
    val waiting, calculating = Value
}

/** Performs Inverse DCT on 8x8 Matrix with scaling
  *
  * IO
  * @param matrixIn Input matrix to perform IDCT on
  *
  * @return idctOut Resulting scaled output of IDCT
  */
class IDCTChisel extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Valid(new Bundle {
            val matrixIn = Input(Vec(8, Vec(8, SInt(32.W))))
        }))
        val idctOut = Valid(Vec(8, Vec(8, SInt(9.W))))
    })

    // Initializes registers for matrix and valid bit
    val matrixInput  = Reg(Vec(8, Vec(8, SInt(32.W))))
    val matrixOutput = Reg(Vec(8, Vec(8, SInt(9.W))))
    val validOut = RegInit(false.B)

    // Assigns outputs
    io.idctOut.valid := validOut
    io.idctOut.bits := matrixOutput

    // Function to compute IDCT values for each element of the input matrix
    def IDCT(matrix: Vec[Vec[SInt]]): Vec[Vec[SInt]] = {
        val idctMatrix = Wire(Vec(8, Vec(8, SInt(9.W))))

        // Compute IDCT
        for (i <- 0 until 8) {
            for (j <- 0 until 8) {
                var sum = 0.S
                for (u <- 0 until 8) {
                    for (v <- 0 until 8) {
                        // Scale alphaU/V to preserve precision
                        val alphaU = if (u == 0) (1.0 / math.sqrt(2)) * 100 else 100
                        val alphaV = if (v == 0) (1.0 / math.sqrt(2)) * 100 else 100
                        val coefValue = matrix(u)(v)

                        // Scale the cosine values to preserve precision
                        val cosVal = (math.cos((2 * i + 1) * u * Pi / 16) * math.cos((2 * j + 1) * v * Pi / 16) * 100).toInt.S
                        val mulSum = alphaU.toInt.S * alphaV.toInt.S * coefValue * cosVal
                        sum = sum +& mulSum
                    }
                }

                val scaledSum = sum / 4.S
                idctMatrix(i)(j) := scaledSum
            }
        }

        idctMatrix
    }

    // Initializes state and defines FSM
    val state = RegInit(IDCTState.waiting)
    switch(state) {
        is(IDCTState.waiting) {
            when(io.in.valid) {
                matrixInput := io.in.bits.matrixIn
                state := IDCTState.calculating
                validOut := false.B
            }
        }
        is(IDCTState.calculating) {
            // Assigns output matrix to calculated IDCT values
            matrixOutput := IDCT(matrixInput)
            state := IDCTState.waiting
            validOut := true.B
        }
    }
}
