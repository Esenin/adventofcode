package task4

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

typealias Interval = Pair<Int, Int>
typealias GuardLog = ArrayList<Interval>
typealias GuardID = Int

enum class State {
    AWAKE, ASLEEP
}

typealias GuardsSleepData = HashMap<GuardID, GuardLog>
typealias ParseState = Pair<GuardID, GuardsSleepData>
val logLineRegex =""".*(\d+):(\d+)\] (\w+) ([#\w]+).*""".toRegex()

fun parseLine(acc: ParseState, line: String): ParseState {
    val match = logLineRegex.find(line)!!
    val (hour, minute, actionToken1, actionToken2) = match.destructured

    return when (actionToken1) {
        "Guard" -> {
            val id: GuardID = actionToken2.drop(1).toInt()
            id to acc.second
        }
        "falls" -> {
            assert(hour.toInt() == 0)
            val logs: GuardLog = acc.second.getOrPut(acc.first) { GuardLog() }
            logs.add(minute.toInt() to -1)
            acc
        }
        "wakes" -> {
            assert(hour.toInt() == 0)
            val logs: GuardLog = acc.second.getOrPut(acc.first) { GuardLog() }
            val startMinute: Int = logs.last().first
            logs[logs.size - 1] = startMinute to minute.toInt()
            acc
        }
        else -> {
            assert(false)
            acc
        }
    }
}

data class Guard (val id: GuardID) {
    val sleepSpace = Array<Int> (60) {0}
}

fun Guard(id: GuardID, log: GuardLog): Guard {
    val guard = Guard(id)
    log.forEach { (from_, to_) ->
        for (i in from_ until to_) {
            guard.sleepSpace[i] += 1
        }
    }
    return guard
}

fun Guard.getTotalSleepDuration(): Int = this.sleepSpace.sum()

fun Guard.getMostFrequentSleepMinute(): Int =
    this.sleepSpace.indices.maxBy { this.sleepSpace[it] } ?: -1

fun Guard.getCountOfMostFrequentAsleepMinute(): Int =
    this.sleepSpace.max() ?: -1

typealias Guards = List<Guard>

fun parse(lines: List<String>): Guards {
    val init: ParseState = -1 to GuardsSleepData()
    val guardsSleepLogs: GuardsSleepData = lines.fold(init, ::parseLine).second
    return guardsSleepLogs.map { Guard(it.key, it.value) }.toList()
}

/**
 * The goal is (according to the Strategy-1) to find a guard which sleeps more than the others
 * and
 * @return a product of GuardId by the minute they sleep most often
 */
fun solvePartOne(guards: Guards): Int {
    val theMostSleepyGuardIdx = guards.indices.maxBy { guards[it].getTotalSleepDuration() } ?: -1
    assert(theMostSleepyGuardIdx >= 0)

    val chosenGuard = guards[theMostSleepyGuardIdx]
    val bestSneakMinute = chosenGuard.getMostFrequentSleepMinute()

    println("guardId: ${chosenGuard.id}, minute: $bestSneakMinute")
    assert(bestSneakMinute >= 0)
    return chosenGuard.id * bestSneakMinute
}

/**
 * Strategy 2: find a guard which is asleep during the same minute the most frequent
 * @return a product of GuardId by the found minute
 */
fun solvePartTwo(guards: Guards): Int {
    val mostStableGuard: Guard = guards.maxBy { it.getCountOfMostFrequentAsleepMinute() }!!
    println("MostStableGuard id=${mostStableGuard.id} has slept " +
        "${mostStableGuard.getCountOfMostFrequentAsleepMinute()} times during " +
        "${mostStableGuard.getMostFrequentSleepMinute()} minute")

    return mostStableGuard.id * mostStableGuard.getMostFrequentSleepMinute()
}

// Tests ///////////////////////////////////////////////////////////////////////////////////////////

@Test
fun testParser() {
    val init: ParseState = -1 to GuardsSleepData()
    var res = parseLine(init, "[1518-04-20 23:58] Guard #353 begins shift")
    assertEquals(353 to GuardsSleepData(), res)
    res = parseLine(res, "[1518-04-21 00:31] falls asleep")
    res = parseLine(res, "[1518-04-21 00:51] wakes up")

    assertEquals(true, res.second[353]?.size == 1)
    assertEquals(true, res.second[353]!![0] == 31 to 51)

    res = parseLine(res, "[1518-04-21 00:01] Guard #7 begins shift")
    res = parseLine(res, "[1518-04-21 00:10] falls asleep")
    res = parseLine(res, "[1518-04-21 00:20] wakes up")
    res = parseLine(res, "[1518-04-21 00:30] falls asleep")
    res = parseLine(res, "[1518-04-21 00:40] wakes up")
    res = parseLine(res, "[1518-05-22 00:02] Guard #353 begins shift")
    res = parseLine(res, "[1518-05-21 00:32] falls asleep")
    res = parseLine(res, "[1518-05-21 00:52] wakes up")

    assertEquals(true, res.second[353]?.size == 2)
    assertEquals(true, res.second[353]!![0] == 31 to 51)
    assertEquals(true, res.second[353]!![1] == 32 to 52)
    assertEquals(true, res.second[7]!![0] == 10 to 20)
    assertEquals(true, res.second[7]!![1] == 30 to 40)
}

@Test
fun testProvidedExample() {
    val lines: List<String> = listOf(
        "[1518-11-01 00:00] Guard #10 begins shift",
        "[1518-11-01 00:05] falls asleep",
        "[1518-11-01 00:25] wakes up",
        "[1518-11-01 00:30] falls asleep",
        "[1518-11-01 00:55] wakes up",
        "[1518-11-01 23:58] Guard #99 begins shift",
        "[1518-11-02 00:40] falls asleep",
        "[1518-11-02 00:50] wakes up",
        "[1518-11-03 00:05] Guard #10 begins shift",
        "[1518-11-03 00:24] falls asleep",
        "[1518-11-03 00:29] wakes up",
        "[1518-11-04 00:02] Guard #99 begins shift",
        "[1518-11-04 00:36] falls asleep",
        "[1518-11-04 00:46] wakes up",
        "[1518-11-05 00:03] Guard #99 begins shift",
        "[1518-11-05 00:45] falls asleep",
        "[1518-11-05 00:55] wakes up"
    )
    val guards = parse(lines)
    assertEquals(10 * 24, solvePartOne(guards))

    assertEquals(99 * 45, solvePartTwo(guards))
}

@Test
fun testLogsSorting() {
    val lines: List<String> = listOf(
        "[1518-06-18 00:02] Guard #2789 begins shift",
        "[1518-08-28 00:45] wakes up",
        "[1518-09-16 23:56] Guard #1117 begins shift",
        "[1518-08-26 00:31] falls asleep",
        "[1518-04-11 00:00] wakes up",
        "[1518-04-10 23:52] Guard #811 begins shift"
    )
    val expected: List<String> = listOf(
        "[1518-04-10 23:52] Guard #811 begins shift",
        "[1518-04-11 00:00] wakes up",
        "[1518-06-18 00:02] Guard #2789 begins shift",
        "[1518-08-26 00:31] falls asleep",
        "[1518-08-28 00:45] wakes up",
        "[1518-09-16 23:56] Guard #1117 begins shift"
    )
    assertEquals(expected, lines.sorted())
}

@Test
fun testGuardsProperties() {
    val log1: GuardLog = arrayOf(
        10 to 20,
        20 to 30,
        40 to 50,
        40 to 50
    ).toCollection(ArrayList())
    val g1: Guard = Guard(1, log1)

    assertEquals(true, g1.getMostFrequentSleepMinute() in 40 .. 49)
    assertEquals(40, g1.getTotalSleepDuration())
}

fun main(args: Array<String>) {
    testParser()
    testLogsSorting()
    testGuardsProperties()
    testProvidedExample()

    val lines = File("input.txt").readLines().sorted()
    val guards = parse(lines)

    val answer1 = solvePartOne(guards)
    println("Strategy-1 ans: $answer1")

    val answer2 = solvePartTwo(guards)
    println("Strategy-2 ans: $answer2")
}