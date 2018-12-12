package task10

import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.test.assertEquals

data class Vector (val x: Int, val y: Int)
typealias Point = Vector
typealias Velocity = Vector

// L2 + L1
fun Point.distanceTo(other: Point): Double =
    (abs(this.x - other.x) to abs(this.y - other.y)).let { (dx, dy) ->
        sqrt(dx * dx + dy * dy.toDouble()) + (dx + dy)
    }

data class LightPoint (val pos: Point, val velocity: Velocity)

fun LightPoint.nextPos(speedUp: Int) =
    LightPoint(Point(pos.x + speedUp * velocity.x, pos.y + speedUp * velocity.y), velocity)

val lightPointFormat = """position=< ?(-?\d+),  ?(-?\d+)> velocity=< ?(-?\d+),  ?(-?\d+)>""".toRegex()

fun LightPoint(line: String): LightPoint {
    val matched = lightPointFormat.matchEntire(line)!!
    val (x, y, dx, dy) = matched.destructured
    return LightPoint(Point(x.toInt(), y.toInt()), Velocity(dx.toInt(), dy.toInt()))
}

class Configuration (val pointsOfLight: Collection<LightPoint>) {
    private val defaultResolution = 60

    private fun getVerticalScale(scale: Int) = scale / 5

    private fun getPoints() = pointsOfLight.mapTo(ArrayList()) { it.pos }

    /// shift all points to a different coordinate system
    /// (0, 0) is top left corner
    private fun getPointsMappedToTopLeft(): ArrayList<Point> {
        val points = getPoints()
        val minY = points.minBy { it.y }?.y ?: error("empty configuration")
        val minX = points.minBy { it.x }?.x ?: error("empty configuration")
        val verticalShift = 0 - minY
        val horizontalShift = 0 - minX
        return points.mapTo(ArrayList()) {
            Point(it.x + horizontalShift, it.y + verticalShift)
        }
    }

    /// maps points to limits XY:[0.. [scale])
    private fun getNormalizedPoints(scale: Int = defaultResolution): ArrayList<Point> {
        val points = getPointsMappedToTopLeft()
        assert(points.isNotEmpty())
        val maxX = points.maxBy { it.x }!!.x.let { if (it > 0) it else 1 }
        val maxY = points.maxBy { it.y }!!.y.let { if (it > 0) it else 1 }
        return points.mapTo(ArrayList()) { (x, y) ->
            Point(
                (x / maxX.toFloat() * (scale - 1)).roundToInt(),
                (y / maxY.toFloat() * (getVerticalScale(scale) - 1)).roundToInt()
            )
        }
    }

    val getScore: Double
        get() {
            val points = getPoints()
            var result: Double = 0.0
            for (center in points) {
                points.map { center.distanceTo(it) }.sorted().let { distances ->
                    result += distances.take(distances.size / 5).sum()
                }
            }
            return result
        }

    private fun toMatrix(scale: Int = defaultResolution): ArrayList<String> {
        val emptyLine = "".padEnd(scale, ' ')
        val result = ArrayList<StringBuilder>()
        repeat(getVerticalScale(scale)) {
            result.add(StringBuilder(emptyLine))
        }

        getNormalizedPoints(scale).forEach { p ->
            result[p.y][p.x] = '#'
        }
        return result.mapTo(ArrayList(), StringBuilder::toString)
    }

    fun toString(scale: Int = defaultResolution): String {
        val field = toMatrix(scale)
        val builder = StringBuilder()
        for (line in field) {
            if (builder.isNotEmpty())
                builder.append('\n')
            builder.append(line)
        }
        return builder.toString()
    }

    override fun toString(): String = toString(defaultResolution)

    fun nextConfiguration(speedUp: Int = 1) = Configuration(pointsOfLight.map { it.nextPos(speedUp) })
}

fun parseConfiguration(lines: List<String>): Configuration =
    Configuration(lines.mapTo(ArrayList(), ::LightPoint))


fun solvePartOne(lines: List<String>) {
    val maxSteps = 800
    val bestStates = PriorityQueue<Configuration>(compareBy { it.getScore })
    var configuration = parseConfiguration(lines)
    println(configuration)
    println("".padEnd(120, '|'))
    println("")

    repeat(maxSteps) {
        bestStates.add(configuration)
        if (bestStates.size > 5) {
            bestStates.poll()
        }
        configuration = configuration.nextConfiguration(80)

        if (it == 100) {
            println(configuration)
            println("".padEnd(120, '|'))
            println("")
        }
    }

    println("Best met configurations:")
    while (bestStates.isNotEmpty()) {
        val c = bestStates.poll()
        println(c)
        println("".padEnd(120, '|'))
        println("")
    }
}

fun watchProgress(lines: List<String>) {
    var configuration = parseConfiguration(lines)
    val maxSteps = 1200
    repeat(maxSteps) {
        val sleepTime =
            when {
                it > 1066 -> 100L
                it > 1061 -> 800L
                it > 1059 -> 600L
                it > 1050 -> 100L
                else -> 50L
            }
        if (it < 10 || it > maxSteps * 8 / 10)
        {
            println("Step $it. Sleeptime = $sleepTime ::")
            println(configuration)
//        println("".padEnd(120, '|'))
            println("")
            System.out.flush()
            Thread.sleep(sleepTime)
            print("\u001b[H\u001b[2J")
        }
        configuration = configuration.nextConfiguration(10)
    }

}

// Tests ///////////////////////////////////////////////////////////////////////////////////////////

@Test
fun testParser() {
    val input = listOf(
        "position=<-21091, -10461> velocity=< 2,  1>",
        "position=< 32049,  53316> velocity=<-3, -5>"
    )
    val expected = listOf(
        LightPoint(Point(-21091, -10461), Velocity(2, 1)),
        LightPoint(Point(32049, 53316), Velocity(-3, -5))
    )
    assertEquals(expected, input.map(::LightPoint))
}

@Test
fun testConfigurationAdvance() {
    val c = Configuration(arrayListOf(
        LightPoint(Point(0, 0), Velocity(1, 0)),
        LightPoint(Point(0, 0), Velocity(2, 0))
    ))
    assertEquals(arrayListOf(
        LightPoint(Point(1, 0), Velocity(1, 0)),
        LightPoint(Point(2, 0), Velocity(2, 0))
        ),
        c.nextConfiguration().pointsOfLight)
}

fun main(args: Array<String>) {
    testParser()
    testConfigurationAdvance()

    val input = File("input.txt").readLines()

    watchProgress(input)
//    solvePartOne(input)
}