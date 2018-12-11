package task9

import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

/**
 * Although, the class contains "deque" in its name it has nothing to do with queues.
 * The class allows to add/remove elements to/from any place without moving much of the data around.
 */
class RandomAccessDeque <T>() {
    private val MAX_PAGE_SIZE = 512
    private val MIN_LOAD_SIZE = MAX_PAGE_SIZE / 6
    private var opCounter = 0

    private data class Page <T> (var indexShift: Int) {
        val data = ArrayList<T>()
    }

    private val index = ArrayList<Page<T>>()

    init {
        val newPage = Page<T>(indexShift = 0)
        index.add(newPage)
    }

    fun add(absPosition: Int, value: T) {
        val targetPageIdx = getPagePositionInIndex(absPosition)

        val page = index[targetPageIdx]

        val relativePos = absPosition - page.indexShift
        assert(relativePos in 0..page.data.size)

        page.data.add(relativePos, value)
        updateTail(targetPageIdx, +1)

        if (page.data.size > MAX_PAGE_SIZE) {
            split(page, targetPageIdx)
        }
        countOp()
    }

    fun removeAt(absPosition: Int): T {
        val targetPageIdx = getPagePositionInIndex(absPosition)
        val page = index[targetPageIdx]

        val relativePos = absPosition - page.indexShift
        assert(relativePos in 0 until page.data.size)
        val result = page.data.removeAt(relativePos)
        updateTail(targetPageIdx, -1)

        if (page.data.size < MIN_LOAD_SIZE && index.size > 1) {
            merge(page, targetPageIdx)
        }
        countOp()
        return result
    }

    val size
        get() = if (index.isEmpty()) 0 else index.last().let { page ->
            page.indexShift + page.data.size
        }

    fun <DT> fold(initial: DT, op: (DT, T) -> DT): DT {
        var acc = initial
        for (page in index) {
            for (item in page.data) {
                acc = op(acc, item)
            }
        }
        return acc
    }

    private fun getPagePositionInIndex(absPosition: Int): Int {
        val searchResult = index.binarySearch { it.indexShift - absPosition }
        val idx = if (searchResult < 0) {
            val insertionPoint = -searchResult - 1
            insertionPoint - 1
        } else {
            searchResult
        }
        assert(idx in 0 until index.size)
        return idx
    }

    private fun updateTail(headIdx: Int, increment: Int) {
        for (i in headIdx + 1 until index.size) {
            index[i].indexShift += increment
        }
    }

    private fun split(page: Page<T>, pageIndex: Int) {
        val numStays = page.data.size / 2

        val newPage = Page<T>(page.indexShift + numStays)
        for (i in numStays until page.data.size) {
            newPage.data.add(page.data[i])
        }
        for (i in page.data.size - 1 downTo  numStays) {
            page.data.removeAt(i)
        }
        index.add(pageIndex + 1, newPage)
    }

    /**
     * Merge 2 neighbour pages into one. It might have bigger size than MAX_PAGE_SIZE but that's ok.
     * The resulted page is going to either be reduced on the next remove or split on the next add.
     */
    private fun merge(page: Page<T>, pageIndex: Int) {
        var srcPage = page
        var srcIndex = pageIndex
        var destPage = page
        var destIndex = pageIndex

        if (pageIndex + 1 < index.size) {
            // merge page on the right to the current one
            srcIndex = pageIndex + 1
            srcPage = index[srcIndex]
        } else {
            // merge current page to the left one
            assert(pageIndex > 0)
            destIndex = pageIndex - 1
            destPage = index[destIndex]
        }
        srcPage.data.toCollection(destPage.data)
        index.removeAt(srcIndex)
    }

    private fun countOp() {
        opCounter = (opCounter + 1) % 100000
        if (opCounter != 0)
            return

        val loadFactor = size / index.size.toDouble()
        val loadFactorStr = "%.2f".format(loadFactor)
        println("DEBUG: size: $size, num chunks: ${index.size}, load factor: $loadFactorStr.")

        assert(index.size < 5 || loadFactor.toInt() in MIN_LOAD_SIZE .. MAX_PAGE_SIZE)
    }

}

typealias PlayerID = Int
typealias MarbleValue = Long
typealias Position = Int

class MarbleGameEngine  {
    private val circle = RandomAccessDeque<MarbleValue>() //RandomAccessDeque<MarbleValue>()
    private var currentMarble: Position = -1

    init {
        // Game starts with one stone of value zero
        currentMarble = 0
        circle.add(currentMarble, 0)
        assert(circle.size == 1)
        println("New game initialized!")
    }

    //! returns scores
    fun takeTurn(marbleValue: MarbleValue): MarbleValue =
        if (marbleValue % 23L == 0L) {
            marbleValue + extractScores()
        } else {
            assert(circle.size > 0)
            emplaceNextMarble(marbleValue)
            0
        }

    private fun emplaceNextMarble(value: MarbleValue) {
        val circleLength = circle.size
        val newMarblePos = (currentMarble + 2) % circleLength
        circle.add(newMarblePos, value)
        currentMarble = newMarblePos
    }

    private fun extractScores(): MarbleValue {
        val circleLength = circle.size
        val removedMarblePos = (circleLength + currentMarble - 7) % circleLength
        currentMarble = removedMarblePos
        return circle.removeAt(removedMarblePos)
    }
}

val inputFormat = """(\d+) players; last marble is worth (\d+) points""".toRegex()

/**
 * The goal is to determine the winning score in the Marble game
 */
fun solvePartOne(line: String): Long {
    val matched = inputFormat.matchEntire(line)!!
    val (numPlayersStr, numTurnsStr) = matched.destructured
    val (numPlayers, numTurns) = numPlayersStr.toInt() to numTurnsStr.toInt()
    val game = MarbleGameEngine()

    val scoreboard = HashMap<PlayerID, Long>(numPlayers)
    (0 until numPlayers).forEach { scoreboard[it] = 0L }
    var currentPlayer: PlayerID = 0
    for (value in 1L..numTurns) {
        currentPlayer = (currentPlayer + 1) % numPlayers

        scoreboard[currentPlayer] = scoreboard[currentPlayer]!! + game.takeTurn(value)
    }

    return scoreboard.values.max()!!
}

// Tests ///////////////////////////////////////////////////////////////////////////////////////////

@Test
fun testDeque1() {
    val deque = RandomAccessDeque<Int>()

    deque.add(0, 0)
    assertEquals(1, deque.size)

    deque.add(1, 3)
    deque.add(2, 4)
    deque.add(1, 2)
    deque.add(1, 1)
    assertEquals(5, deque.size)
    assertEquals(listOf(0, 1, 2, 3, 4), deque.fold(listOf()) {
        acc, e -> acc.plus(e)
    })

    assertEquals(0, deque.removeAt(0))
    assertEquals(1, deque.removeAt(0))
    assertEquals(2, deque.removeAt(0))
    assertEquals(3, deque.removeAt(0))
    assertEquals(4, deque.removeAt(0))
    assertEquals(0, deque.size)
}

@Test
fun testDeque2() {
    val deque = RandomAccessDeque<Int>()

    val sz = 128 * 5 + 1

    repeat(3) {
        println("testDeque2.iter=$it")

        for (i in 0 until sz) {
            deque.add(0, sz - i - 1)
        }
        assertEquals(sz, deque.size)

        val shift = 100
        for (i in 0 until sz - shift) {
            assertEquals(shift + i, deque.removeAt(shift))
        }
        assertEquals(100, deque.size)
        for (i in 99 downTo 0) {
            assertEquals(i, deque.removeAt(i))
        }
    }

}

@Test
fun taskOneProvidedSample1() {
    val line = "9 players; last marble is worth 25 points"
    assertEquals(32, solvePartOne(line))
}

@Test
fun taskOneProvidedSamplePack1() {
    val line1 = "10 players; last marble is worth 1618 points"
    assertEquals(8317, solvePartOne(line1))
    val line2 = "13 players; last marble is worth 7999 points"
    assertEquals(146373, solvePartOne(line2))
}

@Test
fun taskOneProvidedSamplePack2(){
    val line1 = "17 players; last marble is worth 1104 points"
    assertEquals(2764, solvePartOne(line1))
    val line2 = "21 players; last marble is worth 6111 points"
    assertEquals(54718, solvePartOne(line2))
    val line3 = "30 players; last marble is worth 5807 points"
    assertEquals(37305, solvePartOne(line3))

}

fun main(args: Array<String>) {
    testDeque1()
    testDeque2()
    taskOneProvidedSample1()
    taskOneProvidedSamplePack1()
    taskOneProvidedSamplePack2()

    val input = File("input.txt").readLines()[0]

    var answer1: Long = 0
    val elapsed1 = measureTimeMillis {
        answer1 = solvePartOne(input)
    }
    println("Answer: $answer1 was returned in ${elapsed1.toFloat() / 1000} [sec]")
}