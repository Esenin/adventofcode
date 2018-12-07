import java.io.File


fun countCharacters(s: String) = s.groupBy { it }.values.map { it.size }

/**
 *  Task is to compute a checksum which is a product of amount of ids which mention some letter 2
 *  times by amount of ids which mention some letter 3 times
 */
fun taskPartOne(boxIds: List<String>) {
    val reduced = boxIds.map { countCharacters(it) }

    val amtTwoChars = reduced.map { it.contains(2) }.count { it }
    val amtThreeChars = reduced.map { it.contains(3) }.count { it }

    println(amtTwoChars * amtThreeChars)
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun pairToCommonLetters(lhs: String, rhs: String) =
    lhs.zip(rhs).fold(String()) { newStr, charPair ->
        if (charPair.first == charPair.second) newStr + charPair.first else newStr
    }

fun hasCloseSerial(lhs: String, rhs: String) = lhs.length == rhs.length &&
        lhs.length - 1 == pairToCommonLetters(lhs, rhs).length

/**
 * The task is to find a pair of ids which differ by a single letter only
 */
fun taskPartTwo(boxIds: List<String>): String {
    val matchedPair = boxIds.sorted().zipWithNext().find { hasCloseSerial(it.first, it.second) }
    return if (matchedPair != null) pairToCommonLetters(
        matchedPair.first,
        matchedPair.second
    ) else "not found"
}

fun main(args: Array<String>) {
    val lines: List<String> = File("input.txt").readLines()
//    taskPartOne(lines)
    println(taskPartTwo(lines))
}