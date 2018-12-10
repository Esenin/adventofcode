package task8

import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.test.assertEquals

data class Node(val childNodes: ArrayList<Node>, val data: ArrayList<Int>)

fun Node() = Node(ArrayList<Node>(), ArrayList<Int>())

//! returns root node
fun decodeNodes(istream: Scanner): Node {
    val numAdjNodes = istream.nextInt()
    val numData = istream.nextInt()
    val newNode = Node()
    (0 until numAdjNodes).forEach {
        newNode.childNodes.add(decodeNodes(istream))
    }
    (0 until numData).forEach {
        newNode.data.add(istream.nextInt())
    }
    return newNode
}

typealias Visitor = (Node) -> Unit

//! Assumes a tree, does not check against cycles
fun dfs(root: Node, visit: Visitor) {
    visit(root)
    root.childNodes.forEach { dfs(it, visit) }
}

class Tree (val root: Node)

fun Tree(serialized: String): Tree = Tree(decodeNodes(Scanner(serialized)))

fun <T> Tree.fold(initial: T, op: (T, Node) -> T): T {
    var acc = initial
    dfs(this.root) {
        acc = op(acc, it)
    }
    return acc
}

/**
 * The goal is to deserialize a tree and add up all metadata
 */
fun solvePartOne(line: String): Int =
    Tree(line).fold(0) { acc, node ->
        acc + node.data.sum()
    }

fun Node.getValue():Int =
    when {
        this.childNodes.isEmpty() -> this.data.sum()
        else -> this.data.fold(0) { acc, idx -> acc +
            if (idx in 1..this.childNodes.size)
                this.childNodes[idx - 1].getValue()
            else
                0
        }
    }

/**
 * The goal is to compute a "value" of the root node following some specific rules
 */
fun solvePartTwo(line: String): Int = Tree(line).root.getValue()

// Tests ///////////////////////////////////////////////////////////////////////////////////////////

@Test
fun testPartOneProvidedSample() {
    val line = "2 3 0 3 10 11 12 1 1 0 1 99 2 1 1 2"
    assertEquals(138, solvePartOne(line))
}

@Test
fun testPartTwoProvidedSample() {
    val line = "2 3 0 3 10 11 12 1 1 0 1 99 2 1 1 2"
    assertEquals(66, solvePartTwo(line))
}

fun main(args: Array<String>) {
    testPartOneProvidedSample()
    testPartTwoProvidedSample()

    val input = File("input.txt").readLines()[0]

    println("Part-1 answer: ${solvePartOne(input)}")

    println("Part-2 answer: ${solvePartTwo(input)}")
}