import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File
import org.junit.jupiter.api.Test
import java.awt.CardLayout


data class Claim(val id: Int, val x: Int, val w: Int, val y: Int, val h: Int)

val claimRegex = """#(?<id>\d+) @ (?<xpos>\d+),(?<ypos>\d+): (?<w>\d+)x(?<h>\d+)""".toRegex()
/**
 * input example: "#1 @ 1,3: 4x4"
 */
fun Claim(line: String): Claim {
    val match = claimRegex.matchEntire(line)

    val x = match!!.groups["xpos"]!!.value.toInt()
    val w = match.groups["w"]!!.value.toInt()
    val y = match.groups["ypos"]!!.value.toInt()
    val h = match.groups["h"]!!.value.toInt()
    val id = match.groups["id"]!!.value.toInt()
    return Claim(id, x, w, y, h)
}

typealias Field = Array<Array<Int>>
const val fieldSize = 1001
val zeroField: Field = Array<Array<Int>>(fieldSize) { Array<Int>(fieldSize) {0}}

fun Field.deepcopy(): Field = this.map { it.clone() }.toTypedArray()

fun merge(lhs: Field, rhs: Field): Field =
    lhs.zip(rhs).map { columnPair
        ->
        columnPair.first.zip(columnPair.second).map { it.first + it.second }.toTypedArray()
    }.toTypedArray()

fun Claim.toField(): Field {
    val field = zeroField.deepcopy()
    for (i in this.x until this.x + this.w) {
        for (j in this.y until this.y + this.h) {
            field[i][j] = 1
        }
    }
    return field
}

/**
 * The goal is to find how many rectangles do intersect with other one
 */
fun solve_part1(lines: List<String>): Int {
    val claims = lines.map(::Claim)

    val result: Int = claims.fold(zeroField) { acc, rect ->
        merge(acc, rect.toField())
    }.sumBy { it.fold(0) {acc, x -> if (x > 1) acc + 1 else acc} }

    println(result)
    return result
}

fun Claim.square(): Int = this.w * this.h

fun Claim.isGoodOn(field: Field): Boolean =
    this.square() ==
            field.sliceArray(this.x..this.x + this.w)
                .map { it.sliceArray(this.y..this.y + this.h) }
                .map { it.sum() }.sum()


/**
 * The goal is to fine the one claim(rectangle) which does not intersect with any other
 * @return id of the good claim
 */
fun solve_part2(lines: List<String>): Int {
    val claims = lines.map(::Claim)

    val mergedField: Field = claims.fold(zeroField) { acc, claim ->
        merge(acc, claim.toField())
    }

    val goodClaim: Claim = claims.find { it.isGoodOn(mergedField) }!!

    println(goodClaim.id)
    return goodClaim.id
}

// Tests ///////////////////////////////////////////////////////////////////////////////////////////

fun Field.isSame(other: Field): Boolean = this.zip(other).all { it.first contentEquals it.second }

@Test
fun testMergeFields_1() {
    assertEquals(true, zeroField.isSame(merge(zeroField, zeroField)))

    val f1: Field = Array(4) { arrayOf(1, 2, 3, 4) }
    val f2: Field = f1.deepcopy()
    val expected: Field = Array(4) { arrayOf(2, 4, 6, 8) }
    assertEquals(true, expected.isSame(merge(f1, f2)))
}

@Test
fun testMergeFields_2() {
    val f1: Field = arrayOf(
        arrayOf(0, 1, 1, 0),
        arrayOf(0, 1, 1, 0),
        arrayOf(1, 0, 0, 1),
        arrayOf(0, 1, 0, 0)
    )
    val f2: Field = arrayOf(
        arrayOf(1, 0, 0, 1),
        arrayOf(1, 0, 0, 1),
        arrayOf(0, 1, 1, 0),
        arrayOf(1, 0, 1, 1)
    )
    val expected: Field = arrayOf(
        arrayOf(1, 1, 1, 1),
        arrayOf(1, 1, 1, 1),
        arrayOf(1, 1, 1, 1),
        arrayOf(1, 1, 1, 1)
    )
    assertEquals(true, expected.isSame(merge(f1, f2)))
}

@Test
fun testRectToField() {
    val r: Claim = Claim("""#1 @ 1,2: 3x2""")
    val expected = zeroField.deepcopy()
    expected[1][2] = 1
    expected[2][2] = 1
    expected[3][2] = 1
    expected[1][3] = 1
    expected[2][3] = 1
    expected[3][3] = 1

    assertEquals(true, expected.isSame(r.toField()))
}

@Test
fun testCheckZeroState() {
    val brandNewZeroField = Array<Array<Int>>(fieldSize) { Array<Int>(fieldSize) {0}}
    assertEquals(true, zeroField.isSame(brandNewZeroField))
}

fun testOverlappingModule() {
    val lines_1: List<String> = listOf(
        "#101 @ 455,194: 13x20",
        "#103 @ 0,0: 25x25"
    )
    assertEquals(0, solve_part1(lines_1))

    val lines_2: List<String> = listOf(
        "#101 @ 0,0: 10x10",
        "#103 @ 10,10: 25x25"
    )
    assertEquals(0, solve_part1(lines_2))

    val lines_3: List<String> = listOf(
        "#101 @ 0,0: 10x10",
        "#103 @ 9,9: 25x25"
    )
    assertEquals(1, solve_part1(lines_3))
}

@Test
fun testGetGoodClaim() {
    val lines_1: List<String> = listOf(
        "#101 @ 455,194: 13x20",
        "#102 @ 1,1: 25x25",
        "#103 @ 450,190: 50x50"
    )
    assertEquals(102, solve_part2(lines_1))
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun main(args: Array<String>) {
    testMergeFields_1()
    testMergeFields_2()
    testRectToField()
    testCheckZeroState()
    testOverlappingModule()
    testGetGoodClaim()

    val lines = File("input.txt").readLines()
//    solve_part1(lines)

    solve_part2(lines)
}

