package task6

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.math.abs
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

data class Point (val x: Int, val y: Int)

typealias Points = ArrayList<Point>

fun String.toPoint(): Point =
    this.split(", ").let { Point(it.first().toInt(), it.last().toInt()) }

fun Point.distanceTo(other: Point) = abs(this.x - other.x) + abs(this.y - other.y)

// Jarvis's scan to find convex hull of a set of points ////////////////////////////////////////////

class JCompare {
    companion object : Comparator<Point> {
        override fun compare(lhs: Point, rhs: Point): Int = when {
            lhs.x != rhs.x -> lhs.x - rhs.x
            else -> lhs.y - rhs.y
        }
    }
}

fun areClockWise(a: Point, b: Point, c: Point) =
    a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y) < 0

fun  Collection<Point>.getConvexHull(): Points {
    if (this.size <= 1)
        return this.toCollection(Points())
    var pointOnHull: Point = this.minWith(JCompare)!!
    var i = 0
    val result = ArrayList<Point>()
    while (true) {
        result.add(i, pointOnHull)
        var endPoint: Point = this.first()
        for (curPoint: Point in this.drop(1)) {
            if (endPoint == pointOnHull || areClockWise(pointOnHull, curPoint, endPoint)) {
                endPoint = curPoint
            }
        }
        i++
        pointOnHull = endPoint

        if (endPoint == result.first()) {
            break
        }
    }
    return result
}

////////////////////////////////////////////////////////////////////////////////////////////////////

data class Rectangle (val leftUp: Point, val rightDown: Point)

fun Collection<Point>.toMarginRectangle(): Rectangle =
    (this.map { it.x } to this.map { it.y }).let {
        Rectangle(
            Point(it.first.min()!!, it.second.min()!!),
            Point(it.first.max()!!, it.second.max()!!)
        )
    }


/**
 * Find a coordinate with the largest area limited by other points in Manhattan distance
 * @return area which for which the chosen point is the closest one
 */
fun solvePartOne(lines: List<String>): Int {
    val allPoints = lines.map { it.toPoint() }
    val convexHull = allPoints.getConvexHull()
    val boundaries = convexHull.toMarginRectangle()

    val counter = HashMap<Point, Int>()
    for (x in boundaries.leftUp.x..boundaries.rightDown.x) {
        for (y in boundaries.leftUp.y..boundaries.rightDown.y) {
            val curPoint = Point(x, y)
            var minDist: Int? = null
            var minPoint: Point? = null
            allPoints.forEach {
                val dist = curPoint.distanceTo(it)
                if (minDist == null || dist < minDist!!) {
                    minDist = dist
                    minPoint = it
                }
                else if (minDist == dist) {
                    minPoint = null
                }
            }
            if (minPoint != null) {
                counter[minPoint!!] = (counter[minPoint!!] ?: 0) + 1
            }
        }
    }
    return counter.minus(convexHull).maxBy { it.value }?.value ?: -1
}

/**
 * Find a size of an area which has sum of all distances to the point in @p lines less than
 * @p maxTotalDistance
 */
fun solvePartTwo(maxTotalDistance: Int, lines: List<String>): Int {
    val allPoints = lines.map { it.toPoint() }
    val boundaries = allPoints.getConvexHull().toMarginRectangle()
    return (boundaries.leftUp.x..boundaries.rightDown.x).fold(0) { accLine, x ->
        (boundaries.leftUp.y..boundaries.rightDown.y).fold(accLine) near@{ areaSize, y ->
            val curPoint = Point(x, y)
            val totalDistance = allPoints.map { curPoint.distanceTo(it) }.sum()
            return@near if (totalDistance < maxTotalDistance) areaSize + 1 else areaSize
        }
    }
}

// Tests ///////////////////////////////////////////////////////////////////////////////////////////
@Test
fun testParsing() {
    val input: List<String> = listOf(
        "1, -1",
        "100, 500",
        "-8, +10"
    )
    assertEquals(
        listOf(
            Point(1, -1), Point(100, 500), Point(-8, 10)
        ),
        input.map { it.toPoint() }
    )
}

@Test
fun testConvexHullFilter() {
    val expectedConvexHull = setOf(
        Point(-10, -10),
        Point(-10, 10),
        Point(10, 10),
        Point(10, -10)
    )
    val points = expectedConvexHull.union(
        setOf(
            Point(0, 0),
            Point(5, 5),
            Point(-5, 5),
            Point(-9, -9)
        )
    )
    val result = points.getConvexHull()
    assertEquals(result.size, result.toSet().size)
    assertEquals(expectedConvexHull, points.getConvexHull().toSet())
}

@Test
fun testConvexHullFilter2() {
    val convexHull = setOf(
        Point(1, 1),
        Point(1, 6),
        Point(8, 3),
        Point(8, 9)
    )
    val points = convexHull.union(setOf(
            Point(3, 4),
            Point(5, 5)
    ))
    assertEquals(convexHull, points.getConvexHull().toSet())
}

fun testConvexHullIsMinimal() {
    // we don't want extra points to lie on the same line within a convex hull for purposes of our
    // task
    val expectedConvexHull = setOf(
        Point(0, 0),
        Point(8, 12),
        Point(12, 8)
    )
    val points = expectedConvexHull.union(
        setOf(
            Point(10, 10)
        )
    )
    assertEquals(expectedConvexHull, points.getConvexHull().toSet())
}

@Test
fun testProvidedSample() {
    val input: List<String> = listOf(
        "1, 1",
        "1, 6",
        "8, 3",
        "3, 4",
        "5, 5",
        "8, 9"
    )
    assertEquals(17, solvePartOne(input))
}

@Test
fun testTaskSecondPartSample() {
    val input: List<String> = listOf(
        "1, 1",
        "1, 6",
        "8, 3",
        "3, 4",
        "5, 5",
        "8, 9"
    )
    assertEquals(16, solvePartTwo(32, input))
}

fun main(args: Array<String>) {
    testParsing()
    testConvexHullFilter()
    testConvexHullFilter2()
    testConvexHullIsMinimal()
    testProvidedSample()
    testTaskSecondPartSample()

    val lines = File("input.txt").readLines()
    var result1 = 0
    val elapsed1 = measureTimeMillis {
        result1 = solvePartOne(lines)
    }
    println("Part-1: max enclosed area: $result1. Time elapsed: $elapsed1 [ms]")

    var result2 = 0
    val elapsed2 = measureTimeMillis {
        result2 = solvePartTwo(10000, lines)
    }
    println("Part-2: max enclosed area: $result2. Time elapsed: $elapsed2 [ms]")
}