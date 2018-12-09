package task7
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.test.assertEquals


typealias Vertex = Char
typealias Visitor = (Vertex, SortedSet<Vertex>?) -> Collection<Vertex>
//! Oriented graph
class Graph {
    private val adjacencyList = HashMap<Vertex, TreeSet<Vertex>>()

    fun addEdge(from: Vertex, to: Vertex) {
        val destinations = adjacencyList.getOrPut(from) { TreeSet<Vertex>() }
        destinations.add(to)
    }

    fun getAmountOfRequirementsPerNode(): Map<Vertex, Int> {
        val counter = HashMap<Vertex, Int>()
        adjacencyList.keys.forEach {
            counter[it] = 0
        }
        adjacencyList.values.forEach { destinations ->
            destinations.forEach {
                counter[it] = (counter[it] ?: 0) + 1
            }
        }
        return counter
    }

    //! Vertices which has no incoming edges
    fun getComponentRoots(): Set<Vertex> =
        getAmountOfRequirementsPerNode().filter { it.value == 0 }.keys.toSet()

    /**
     * @param visit handles a node and returns list of nodes to be queued
     */
    fun prioritizedBFS(initial: Collection<Vertex>, visit: Visitor) {
        val queue = initial.toCollection(PriorityQueue<Vertex>())
        val used = HashSet<Vertex>()

        while (queue.isNotEmpty()) {
            val node = queue.poll()
            used.add(node)
            visit(node, adjacencyList[node]).toCollection(queue)
        }
    }


}

val dependencyRegex = """Step (\w) must be finished before step (\w) can begin.""".toRegex()

fun Graph(lines: List<String>): Graph {
    val g = Graph()
    lines.forEach {
        val matched = dependencyRegex.matchEntire(it)!!
        val (from, to) = matched.destructured
        g.addEdge(from.first(), to.first())
    }
    return g
}

/**
 * The goal is determine in which order the assembling steps must be taken.
 * Ties are resolved alphabetically
 * @return string representing an order
 */
fun solvePartOne(lines: List<String>): String {
    val g = Graph(lines)
    val requirements = g.getAmountOfRequirementsPerNode()
    val requirementsDone = HashMap<Vertex, Int>()
    val roots = g.getComponentRoots()
    val result = StringBuilder()
    g.prioritizedBFS(roots) { node, destinations ->
        result.append(node)
        val requeue = ArrayList<Vertex>()

        destinations?.forEach { dest ->
            val timesReached = 1 + (requirementsDone[dest] ?: 0)
            requirementsDone[dest] = timesReached
            if (timesReached == requirements[dest] ?: 0) {
                requeue.add(dest)
            }
        }
        requeue
    }

    return result.toString()
}


data class TaskFinishedEvent (val time: Int, val node: Vertex)

fun takeTask(currentTime: Int, node: Vertex) =
    TaskFinishedEvent(currentTime + node.toInt() - 'A'.toInt() + 1  +60 , node)


/**
 * The goal is to find out how much time would it take to accomplish the whole job if
 * @p numWorkers workers are available
 */
fun solvePartTwo(numWorkers: Int, lines: List<String>): Int {
    val g = Graph(lines)
    val requirements = g.getAmountOfRequirementsPerNode()
    val requirementsDone = HashMap<Vertex, Int>()
    val availableTasks = g.getComponentRoots().toCollection(TreeSet<Vertex>())

    // event loop
    val events = PriorityQueue<TaskFinishedEvent>(compareBy({ it.time }, { it.node }))
    var currentTime = 0
    var availableWorkers = numWorkers

    while(availableTasks.isNotEmpty() && availableWorkers > 0) {
        events.add(takeTask(currentTime, availableTasks.pollFirst()))
        availableWorkers -= 1
    }

    while(events.isNotEmpty()) {
        val finishedTask = events.poll()
        availableWorkers += 1
        currentTime = maxOf(currentTime, finishedTask.time)
        g.prioritizedBFS(listOf(finishedTask.node)) { _ , destinations->
            destinations?.forEach { dest ->
                val timesReached = 1 + (requirementsDone[dest] ?: 0)
                requirementsDone[dest] = timesReached
                if (timesReached == requirements[dest] ?: 0) {
                    availableTasks.add(dest)
                }
            }
            listOf()
        }

        while(availableTasks.isNotEmpty() && availableWorkers > 0) {
            events.add(takeTask(currentTime, availableTasks.pollFirst()))
            availableWorkers -= 1
        }
    }
    return currentTime
}


// Tests ///////////////////////////////////////////////////////////////////////////////////////////

@Test
fun testGraphComponentRootsScan() {
    val lines = listOf(
        "Step A must be finished before step B can begin.",
        "Step C must be finished before step B can begin."
    )
    val g = Graph(lines)
    val roots = setOf<Vertex>(
        "A".first(), "C".first()
    )
    assertEquals(roots, g.getComponentRoots())
}

@Test
fun testFirstPartProvidedSample() {
    val lines = listOf(
        "Step C must be finished before step A can begin.",
        "Step C must be finished before step F can begin.",
        "Step A must be finished before step B can begin.",
        "Step A must be finished before step D can begin.",
        "Step B must be finished before step E can begin.",
        "Step D must be finished before step E can begin.",
        "Step F must be finished before step E can begin."
    )
    assertEquals("CABDFE", solvePartOne(lines))
}

@Test
fun testSecondPartProvidedSample() {
    val lines = listOf(
        "Step C must be finished before step A can begin.",
        "Step C must be finished before step F can begin.",
        "Step A must be finished before step B can begin.",
        "Step A must be finished before step D can begin.",
        "Step B must be finished before step E can begin.",
        "Step D must be finished before step E can begin.",
        "Step F must be finished before step E can begin."
    )
    assertEquals(15, solvePartTwo(2, lines))
}

fun main(args: Array<String>) {
    testGraphComponentRootsScan()
    testFirstPartProvidedSample()
//    testSecondPartProvidedSample()

    val lines = File("input.txt").readLines()
    println("Part-1 answer: '${solvePartOne(lines)}'")

    println("Part-2 answer: ${solvePartTwo(5, lines)}")
}