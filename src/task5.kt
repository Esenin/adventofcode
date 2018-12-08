import org.junit.jupiter.api.Test
import java.io.File
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

fun collapsePolymer(p: String): String {
    return p.fold(String()) { acc, c ->
            if (acc.isNotEmpty()
                && acc.last().equals(c, ignoreCase = true)
                && acc.last().isLowerCase() xor c.isLowerCase()
            )
                acc.dropLast(1) else acc.plus(c)

        }
}

fun String.asCollapsedPolymer(): String = collapsePolymer(this)

/**
 * Collapse the given @param polymer following the rules Aa -> {}, aA -> {}, AA / aa -> AA / aa
 * @ return length of a collapsed polymer
 */
fun solvePartOne(polymer: String): Int = collapsePolymer(polymer).length

/**
 * Find a char in polymer which prevents further collapsing
 * @param polymer collapsed polymer
 * @return a new collapsed length after all instances of certain unit type was removed
 */
fun solvePartTwo(polymer: String): Int =
    polymer.toLowerCase().toSet().fold(polymer.length) { minLen, c ->
        minOf(minLen, polymer.filterNot { it.equals(c, ignoreCase = true) }.asCollapsedPolymer().length)
    }


// Tests ///////////////////////////////////////////////////////////////////////////////////////////

@Test
fun testProvidedSamplePartOne() {
    val p: String = "dabAcCaCBAcCcaDA"
    assertEquals("dabCBAcaDA", collapsePolymer(p))
    assertEquals(10, solvePartOne(p))
}

@Test
fun testProvidedSamplePartTwo() {
    val p: String = "dabAcCaCBAcCcaDA"
    assertEquals(4, solvePartTwo(p))
}

fun main(args: Array<String>) {
    testProvidedSamplePartOne()
    testProvidedSamplePartTwo()

    val polymer: String = File("input.txt").readLines()[0]
    var answer1: Int = 0
    val elapsed1 = measureTimeMillis {
        answer1 = solvePartOne(polymer)
    }
    println("Input len: ${polymer.length}, result len: $answer1 in $elapsed1 [ms]")

    var answer2: Int = 0
    val elapsed2 = measureTimeMillis {
        answer2 = solvePartTwo(polymer)
    }
    println("Part2\nResult len: $answer2 in $elapsed2 [ms]")
}