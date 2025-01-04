import scala.collection.immutable._
    // Define Huffman tables
    val dcLumTable = VecInit(Seq(
        (2.U, "b00".U),
        (3.U, "b010".U),
        (3.U, "b011".U),
        (3.U, "b100".U),
        (3.U, "b101".U),
        (3.U, "b110".U),
        (4.U, "b1110".U),
        (5.U, "b11110".U),
        (6.U, "b111110".U),
        (7.U, "b1111110".U),
        (8.U, "b11111110".U),
        (9.U, "b111111110".U)
    ))
    val dcChromTable = VecInit(Seq(
        (2.U, "b00".U),
        (2.U, "b01".U),
        (2.U, "b10".U),
        (3.U, "b110".U),
        (4.U, "b1110".U),
        (5.U, "b11110".U),
        (6.U, "b111110".U),
        (7.U, "b1111110".U),
        (8.U, "b11111110".U),
        (9.U, "b111111110".U),
        (10.U, "b1111111110".U)
    ))
    val acLumTable = VecInit(Seq(
        (4.U, "b1010".U),         // EOB (End of Block)
        (11.U, "b11111111001".U), // ZRL (Zero Run Length)
        (2.U, "b00".U),           // 0/1
        (2.U, "b01".U),           // 0/2
        (3.U, "b100".U),          // 0/3
        (4.U, "b1011".U),         // 0/4
        (5.U, "b11010".U),        // 0/5
        (7.U, "b11110000".U),     // 0/6
        (8.U, "b11111000".U),     // 0/7
        (10.U, "b1111110110".U),  // 0/8
        (16.U, "b1111111110000010".U),     // 0/9
        (16.U, "b1111111110000011".U),     // 0/10
        // Additional entries for full table
        (4.U, "b1100".U),         // 1/1
        (5.U, "b11011".U),        // 1/2
        (7.U, "b1111001".U),       // 1/3
        (9.U, "b111110110".U),       // 1/4
        (11.U, "b11111110110".U),    // 1/5
        (16.U, "b1111111110000100".U),    // 1/6
        (16.U, "b1111111110000101".U),    // 1/7
        (16.U, "b1111111110000110".U),    // 1/8
        (16.U, "b1111111110000111 ".U),   // 1/9
        (16.U, "b1111111110001000".U)    // 1/10
        // 2
        (5.U, "b11100".U),         // 1/1
        (6.U, "b11111001".U),        // 1/2
        (10.U, "b1111110111".U),       // 1/3
        (12.U, "b111111110100".U),       // 1/4
        (16.U, "b1111111110001001".U),    // 1/5
        (16.U, "b1111111110001010".U),    // 1/6
        (16.U, "b1111111110001011".U),    // 1/7
        (16.U, "b1111111110001100".U),    // 1/8
        (16.U, "b1111111110001101 ".U),   // 1/9
        (16.U, "b1111111110001110".U)    // 1/10
        // 3
        (6.U, "b111010".U),         // 1/1
        (9.U, "b111110111".U),        // 1/2
        (12.U, "b111111110101".U),       // 1/3
        (16.U, "b1111111110001111".U),       // 1/4
        (16.U, "b1111111110010000".U),    // 1/5
        (16.U, "b1111111110010001".U),    // 1/6
        (16.U, "b1111111110010010".U),    // 1/7
        (16.U, "b1111111110010011".U),    // 1/8
        (16.U, "b1111111110010100".U),   // 1/9
        (16.U, "b1111111110010101".U)    // 1/10
        // 4
        (6.U, "b111011".U),         // 1/1
        (10.U, "b1111111000".U),        // 1/2
        (16.U, "b1111111110010110".U),       // 1/3
        (16.U, "b1111111110010111".U),       // 1/4
        (10.U, "b1111111110011000".U),    // 1/5
        (16.U, "b1111111110011001".U),    // 1/6
        (16.U, "b1111111110011010".U),    // 1/7
        (16.U, "b1111111110011011".U),    // 1/8
        (16.U, "b1111111110011100".U),   // 1/9
        (16.U, "b1111111110011101".U)    // 1/10
        // 5
        (7.U, "b1111010".U),         // 1/1
        (11.U, "b11111110111".U),        // 1/2
        (16.U, "b1111111110011110".U),       // 1/3
        (16.U, "b1111111110011111".U),       // 1/4
        (10.U, "b1111111110100000".U),    // 1/5
        (16.U, "b1111111110100001".U),    // 1/6
        (16.U, "b1111111110100010".U),    // 1/7
        (16.U, "b1111111110100011".U),    // 1/8
        (16.U, "b1111111110100100".U),   // 1/9
        (16.U, "b1111111110100101".U)    // 1/10
        // 6
        (7.U, "b1111011".U),         // 1/1
        (12.U, "b111111110110".U),        // 1/2
        (16.U, "b1111111110100110".U),       // 1/3
        (16.U, "b1111111110100111".U),       // 1/4
        (16.U, "b1111111110101000".U),    // 1/5
        (16.U, "b1111111110101001".U),    // 1/6
        (16.U, "b1111111110101010".U),    // 1/7
        (16.U, "b1111111110101011".U),    // 1/8
        (16.U, "b1111111110101100".U),   // 1/9
        (16.U, "b1111111110101101".U)    // 1/10
        // 7
        (8.U, "b11111010".U),         // 1/1
        (12.U, "b111111110111".U),        // 1/2
        (16.U, "b1111111110101110".U),       // 1/3
        (16.U, "b1111111110101111".U),       // 1/4
        (16.U, "b1111111110110000".U),    // 1/5
        (16.U, "b1111111110110001".U),    // 1/6
        (16.U, "b1111111110110010".U),    // 1/7
        (16.U, "b1111111110110011".U),    // 1/8
        (16.U, "b1111111110110100".U),   // 1/9
        (16.U, "b1111111110110101".U)    // 1/10
        // 8
        (9.U, "b111111000".U),         // 1/1
        (15.U, "b111111111000000".U),        // 1/2
        (16.U, "b1111111110110110".U),       // 1/3
        (16.U, "b1111111110110111".U),       // 1/4
        (16.U, "b1111111110111000".U),    // 1/5
        (16.U, "b1111111110111001".U),    // 1/6
        (16.U, "b1111111110111010".U),    // 1/7
        (16.U, "b1111111110111011".U),    // 1/8
        (16.U, "b1111111110111100".U),   // 1/9
        (16.U, "b1111111110111101".U)    // 1/10
        // 9
        (9.U, "b111111001".U),         // 1/1
        (16.U, "b1111111110111110".U),        // 1/2
        (16.U, "b1111111110111111".U),       // 1/3
        (16.U, "b1111111111000000".U),       // 1/4
        (16.U, "b1111111111000001".U),    // 1/5
        (16.U, "b1111111111000010".U),    // 1/6
        (16.U, "b1111111111000011".U),    // 1/7
        (16.U, "b1111111111000100".U),    // 1/8
        (16.U, "b1111111111000101".U),   // 1/9
        (16.U, "b1111111111000110".U)    // 1/10
        // A
        (9.U, "b111111010".U),         // 1/1
        (16.U, "b1111111111000111".U),        // 1/2
        (16.U, "b1111111111001000".U),       // 1/3
        (16.U, "b1111111111001001".U),       // 1/4
        (16.U, "b1111111111001010".U),    // 1/5
        (16.U, "b1111111111001011".U),    // 1/6
        (16.U, "b1111111111001100".U),    // 1/7
        (16.U, "b1111111111001101".U),    // 1/8
        (16.U, "b1111111111001110".U),   // 1/9
        (16.U, "b1111111111001111".U)    // 1/10
        // B
        (10.U, "b1111111001".U),         // 1/1
        (16.U, "b1111111111010000".U),        // 1/2
        (16.U, "b1111111111010001".U),       // 1/3
        (16.U, "b1111111111010010".U),       // 1/4
        (16.U, "b1111111111010011".U),    // 1/5
        (16.U, "b1111111111010100".U),    // 1/6
        (16.U, "b1111111111010101".U),    // 1/7
        (16.U, "b1111111111010110".U),    // 1/8
        (16.U, "b1111111111010111".U),   // 1/9
        (16.U, "b1111111111011000".U)    // 1/10
        // C
        (10.U, "b1111111010".U),         // 1/1
        (16.U, "b1111111111011001".U),        // 1/2
        (16.U, "b1111111111011010".U),       // 1/3
        (16.U, "b1111111111011011".U),       // 1/4
        (16.U, "b1111111111011100".U),    // 1/5
        (16.U, "b1111111111011101".U),    // 1/6
        (16.U, "b1111111111011110".U),    // 1/7
        (16.U, "b1111111111011111".U),    // 1/8
        (16.U, "b1111111111100000".U),   // 1/9
        (16.U, "b1111111111100001".U)    // 1/10
        // D
        (11.U, "b11111111000".U),         // 1/1
        (16.U, "b1111111111100010".U),        // 1/2
        (16.U, "b1111111111100011".U),       // 1/3
        (16.U, "b1111111111100100".U),       // 1/4
        (16.U, "b1111111111100101".U),    // 1/5
        (16.U, "b1111111111100110".U),    // 1/6
        (16.U, "b1111111111100111".U),    // 1/7
        (16.U, "b1111111111101000".U),    // 1/8
        (16.U, "b1111111111101001".U),   // 1/9
        (16.U, "b1111111111101010".U)    // 1/10
        // E
        (16.U, "b1111111111101011".U),         // 1/1
        (16.U, "b1111111111101100".U),        // 1/2
        (16.U, "b1111111111101101".U),       // 1/3
        (16.U, "b1111111111101110".U),       // 1/4
        (16.U, "b1111111111101111".U),    // 1/5
        (16.U, "b1111111111110000".U),    // 1/6
        (16.U, "b1111111111110001".U),    // 1/7
        (16.U, "b1111111111110010".U),    // 1/8
        (16.U, "b1111111111110011".U),   // 1/9
        (16.U, "b1111111111110100".U)    // 1/10
        // F
        (11.U, "b11111111001".U),         // 1/0
        (16.U, "b1111111111110101".U),        // 1/1
        (16.U, "b1111111111110110".U),       // 1/2
        (16.U, "b1111111111110111".U),       // 1/3
        (16.U, "b1111111111111000".U),    // 1/4
        (16.U, "b1111111111111001".U),    // 1/5
        (16.U, "b1111111111111010".U),    // 1/6
        (16.U, "b1111111111111011".U),    // 1/7
        (16.U, "b1111111111111100".U),   // 1/8
        (16.U, "b1111111111111101".U)    // 1/9
        (16.U, "b1111111111111110".U)    // 1/10
    ))
    val acChromTable = VecInit(Seq(
        (2.U, "b00".U),      // EOB
        (10.U, "b1111111010".U), // ZRL
        (2.U, "b01".U),      // 0/1
        (3.U, "b100".U),     // 0/2
        (4.U, "b1010".U),    // 0/3
        (5.U, "b11000".U),   // 0/4
        (5.U, "b11001".U),   // 0/5
        // Additional entries for full table
        (6.U, "b111000".U),  // 0/6
        (7.U, "b111001".U),  // 0/7
        (8.U, "b111010".U),  // 0/8
        (9.U, "b111011".U),  // 0/9
        (10.U, "b111100".U), // 0/10
        (3.U, "b101".U),     // 1/1
        (4.U, "b1100".U),    // 1/2
        (5.U, "b11100".U),   // 1/3
        (6.U, "b111010".U),  // 1/4
        (7.U, "b1110110".U)  // 1/5
    ))
/**
 * Huffman coding for JPEG compression.
 */
object Huffman {

  /**
   * A Huffman code is represented by a binary tree.
   *
   * `Leaf` nodes represent single symbols (e.g., integers from DPCM or RLE outputs).
   * `Fork` nodes represent a combination of symbols, with their weights being the sum of the weights of the leaves below them.
   */
  abstract class CodeTree
  case class Fork(left: CodeTree, right: CodeTree, symbols: List[Int], weight: Int) extends CodeTree
  case class Leaf(symbol: Int, weight: Int) extends CodeTree

  // Part 1: Basics

  def weight(tree: CodeTree): Int = tree match {
    case Fork(_, _, _, w) => w
    case Leaf(_, w)       => w
  }

  def symbols(tree: CodeTree): List[Int] = tree match {
    case Fork(_, _, s, _) => s
    case Leaf(s, _)       => List(s)
  }

  def makeCodeTree(left: CodeTree, right: CodeTree): Fork =
    Fork(left, right, symbols(left) ::: symbols(right), weight(left) + weight(right))

  // Part 2: Generating Huffman trees

  /**
   * Computes the frequency of each symbol in the input list.
   */
  def times(symbols: List[Int]): List[(Int, Int)] = {
    symbols.groupBy(identity).map { case (s, occurrences) => (s, occurrences.length) }.toList
  }

  /**
   * Creates a list of `Leaf` nodes for a given frequency table, ordered by ascending weight.
   */
  def makeOrderedLeafList(freqs: List[(Int, Int)]): List[Leaf] = {
    freqs.sortBy(_._2).map { case (symbol, weight) => Leaf(symbol, weight) }
  }

  /**
   * Checks whether the list contains only one tree.
   */
  def singleton(trees: List[CodeTree]): Boolean = trees.size == 1

  /**
   * Combines the first two elements of the list into a single `Fork` node and reorders the list.
   */
  def combine(trees: List[CodeTree]): List[CodeTree] = trees match {
    case left :: right :: rest => (makeCodeTree(left, right) :: rest).sortBy(weight)
    case _                     => trees
  }

  /**
   * Repeatedly combines trees until a single tree remains.
   */
  def until(p: List[CodeTree] => Boolean, f: List[CodeTree] => List[CodeTree])(trees: List[CodeTree]): List[CodeTree] =
    if (p(trees)) trees else until(p, f)(f(trees))

  /**
   * Creates a Huffman tree from a list of symbols.
   */
  def createCodeTree(symbols: List[Int]): CodeTree = {
    val freqs = times(symbols)
    val initialTrees = makeOrderedLeafList(freqs)
    until(singleton, combine)(initialTrees).head
  }

  // Part 3: Encoding and Decoding

  type Bit = Int

  /**
   * Encodes a list of symbols using the Huffman tree.
   */
  def encode(tree: CodeTree)(symbols: List[Int]): List[Bit] = {
    def encodeSymbol(tree: CodeTree, symbol: Int): List[Bit] = tree match {
      case Leaf(s, _) if s == symbol => List()
      case Fork(left, right, _, _) if Huffman.symbols(left).contains(symbol) => 0 :: encodeSymbol(left, symbol)
      case Fork(left, right, _, _)                                    => 1 :: encodeSymbol(right, symbol)
      case _                                                          => throw new Error("Symbol not found in the tree.")
    }

    symbols.flatMap(symbol => encodeSymbol(tree, symbol))
  }

  /**
   * Decodes a list of bits into the original symbols using the Huffman tree.
   */
  def decode(tree: CodeTree, bits: List[Bit]): List[Int] = {
    def decodeBits(currentTree: CodeTree, remainingBits: List[Bit]): (Int, List[Bit]) = currentTree match {
      case Leaf(symbol, _)             => (symbol, remainingBits)
      case Fork(left, right, _, _) if remainingBits.head == 0 => decodeBits(left, remainingBits.tail)
      case Fork(left, right, _, _)     => decodeBits(right, remainingBits.tail)
    }

    def traverse(bits: List[Bit], acc: List[Int]): List[Int] =
      if (bits.isEmpty) acc
      else {
        val (symbol, remainingBits) = decodeBits(tree, bits)
        traverse(remainingBits, acc :+ symbol)
      }

    traverse(bits, List())
  }

  // Part 4: Integration with JPEG (DPCM + RLE)

  /**
   * Encodes a block of symbols (DPCM + RLE output) into Huffman-encoded bits.
   */
  def encodeBlock(block: List[Int]): List[Bit] = {
    val tree = createCodeTree(block)
    encode(tree)(block)
  }

  /**
   * Decodes Huffman-encoded bits back into the original block (DPCM + RLE symbols).
   */
  def decodeBlock(tree: CodeTree, encodedBits: List[Bit]): List[Int] = decode(tree, encodedBits)
}

object Main extends App {
  import Huffman._

  // Example: Simulated DPCM + RLE output
  val dpcmRLEOutput = List(-26, -3, 0, 0, -6, 0, 0, 0, 0, 0)

  // Huffman encoding
  val tree = createCodeTree(dpcmRLEOutput)
  val encodedBits = encode(tree)(dpcmRLEOutput)
  println(s"Encoded Bits: $encodedBits")

  // Huffman decoding
  val decodedOutput = decode(tree, encodedBits)
  println(s"Decoded Output: $decodedOutput")

  // Verify the output matches the original
  assert(decodedOutput == dpcmRLEOutput, "Decoded output does not match the original!")
}