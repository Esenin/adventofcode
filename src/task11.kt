package task11

import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

class PowerGrid(private val serial: Int) {
    private val grid = ArrayList<ArrayList<Int>>()
    val gridSize = 300

    init {
        for (y in 1 .. 300) {
            grid.add(ArrayList<Int>())
            for (x in 1 .. 300) {
                grid.last().add(computeCellPower(x, y))
            }
        }
    }

    fun at(x: Int, y: Int): Int = grid[y - 1][x - 1]

    private fun computeCellPower(x: Int, y: Int): Int {
        val rackID = x + 10
        return (((((rackID * y) + serial) * rackID) / 100) % 10) - 5
    }
}

data class Point(var x:Int, var y:Int)

class RunningSquare(
    private val grid: PowerGrid,
    private val sideSize: Int = 3,
    val pos: Point = Point(1, 1)
) {
    val power: Int
        get() = power_

    private var power_: Int = 0

    init {
        assert(pos.x in 1..grid.gridSize - sideSize + 1)
        assert(pos.y in 1..grid.gridSize - sideSize + 1)

        for (y in pos.y until pos.y + sideSize) {
            for (x in pos.x until pos.x + sideSize) {
                power_ += grid.at(x, y)
            }
        }
    }

    fun canGoRight(): Boolean = pos.x <= grid.gridSize - sideSize
    fun canGoDown(): Boolean = pos.y <= grid.gridSize - sideSize
    fun canGoLeft(): Boolean = pos.x > 1

    fun stepXAxis(goRight: Boolean) {
        val xPrev = if (goRight) pos.x else pos.x + sideSize - 1
        val xNew = if (goRight) pos.x + sideSize else pos.x - 1

        for (yi in pos.y until pos.y + sideSize) {
            power_ -= grid.at(xPrev, yi)
            power_ += grid.at(xNew, yi)
        }
        pos.x += if (goRight) +1 else -1
    }

    fun stepDown() {

        for (x in pos.x until pos.x + sideSize) {
            power_ -= grid.at(x, pos.y)
            power_ += grid.at(x, pos.y + sideSize)
        }
        pos.y += 1
    }

}

/**
 * Find a coordinate where starts a 3x3 square of the most powerful region on a grid with a given
 * serial
 * @return pair top-left position and the resulted power
 */
fun solvePartOne(serial: Int, squareSize: Int = 3): Pair<Point, Int> {
    val grid = PowerGrid(serial)

    val region = RunningSquare(grid, squareSize)
    var maxPower = region.power
    var bestPos = region.pos

    val update = {
        if (region.power > maxPower) {
            maxPower = region.power
            bestPos = region.pos. let { (x, y) -> Point(x, y) }
        }
    }

    var isDirectionRight = true

    while (true) {

        while (isDirectionRight && region.canGoRight()
            || !isDirectionRight && region.canGoLeft()
        ) {
            region.stepXAxis(isDirectionRight)
            update()
        }

        if (region.canGoDown()) {
            region.stepDown()
            update()
            isDirectionRight = !isDirectionRight
        } else {
            break
        }
    }
    return Pair(bestPos, maxPower)
}

/**
 * Find what size of a square give most power
 */
fun solvePartTwo(serial: Int): Pair<Point, Int> {
    var bestPoint = Point(1, 1)
    var bestSize = 1
    var bestPower = 0
    for (size in 1 .. 300) {
        val(pos, power) = solvePartOne(serial, size)
        if (power > bestPower) {
            bestPower = power
            bestSize = size
            bestPoint = Point(pos.x, pos.y)
        }
    }

    return Pair(bestPoint, bestSize)
}

// Tests ///////////////////////////////////////////////////////////////////////////////////////////

@Test
fun testPowerGridLevel() {
    val g = PowerGrid(8)
    assertEquals(4, g.at(3, 5))
    assertEquals(-5, PowerGrid(57).at(122,79))
    assertEquals(0, PowerGrid(39).at(217,196))
    assertEquals(4, PowerGrid(71).at(101,153))
}

@Test
fun testRunningSquareBoundaries() {
    val g = PowerGrid(18)
    var s = RunningSquare(g, pos = Point(297, 200))
    assertEquals(true, s.canGoRight())
    assertEquals(true, s.canGoDown())

    s.stepXAxis(goRight = true)
    assertEquals(false, s.canGoRight())

    s = RunningSquare(g, pos = Point(1, 200))
    assertEquals(false, s.canGoLeft())

    s = RunningSquare(g, pos = Point(1, 298))
    assertEquals(false, s.canGoDown())
}

@Test
fun testRunningSquare() {
    val g = PowerGrid(18)
    var s = RunningSquare(g, pos = Point(33, 45))
    assertEquals(29, s.power)
    assertEquals(true, s.canGoRight())

    s.stepXAxis(goRight = true)

    assertEquals(9, s.power)


    s = RunningSquare(PowerGrid(42), pos = Point(21, 61))
    assertEquals(30, s.power)
    assertEquals(true, s.canGoDown())

    s.stepDown()

    assertEquals(21, s.power)
}

@Test
fun testProvidedSample() {
    assertEquals(Point(33, 45), solvePartOne(18).first)
    assertEquals(Point(21, 61), solvePartOne(42).first)
}

@Test
fun testProvidedSamplePartTwo() {
    assertEquals(Pair(Point(90, 269), 16), solvePartTwo(18))

    assertEquals(Pair(Point(232, 251), 12), solvePartTwo(42))
}

fun main(args: Array<String>) {
    testPowerGridLevel()
    testRunningSquareBoundaries()
    testRunningSquare()

    testProvidedSample()
    testProvidedSamplePartTwo()

    println("Part-1 solution: ${solvePartOne(7511).first}")

    var partTwoResult = Pair(Point(0, 0), 0)

    val elapsed = measureTimeMillis {
        partTwoResult = solvePartTwo(7511)
    }
    println("Part-2 solution: $partTwoResult. Time elapsed: $elapsed [ms]")

}