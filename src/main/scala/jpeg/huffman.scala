import scala.collection.immutable._

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