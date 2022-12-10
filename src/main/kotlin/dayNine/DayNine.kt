package dayNine

import arrow.fx.coroutines.parMap
import arrow.optics.optics
import cc.ekblad.konbini.*
import flows.lines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.toList
import kotlin.io.path.Path
import kotlin.math.pow
import kotlin.math.sqrt

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT,
}

data class Movement(val magnitude: Int, val direction: Direction)

val parseDirection = parser {
    val direction = char()
    whitespace()
    val magnitude = integer()
    when (direction) {
        'U' -> Movement(magnitude.toInt(), Direction.UP)
        'D' -> Movement(magnitude.toInt(), Direction.DOWN)
        'L' -> Movement(magnitude.toInt(), Direction.LEFT)
        'R' -> Movement(magnitude.toInt(), Direction.RIGHT)
        else -> fail("Unable to parse $direction as direction")
    }
}

@optics
data class Point(val row: Int, val col: Int) {
    companion object
}

fun Point.isBottomRow() = this.row == 0
fun Point.isLeftCol() = this.col == 0

fun DayNineState.distance(): Double = run {
    val row = (this.tail.row - this.head.row).toDouble().pow(2)
    val col = (this.tail.col - this.head.col).toDouble().pow(2)
    sqrt(row + col)
}

@optics
data class DayNineState(
    val head: Point, // current head position
    val tail: Point, // current tail position
    val seenTail: Set<Pair<Int, Int>> // all unique tail positions
) {
    companion object
}

fun pickMovement(
    before: Double,
    after: Double,
    nextState: DayNineState,
    alsoMoveTail: DayNineState,
    snapTail: DayNineState
) =
    when {
        before == 0.toDouble() && after == 1.toDouble() -> nextState // overlapping
        before == 1.toDouble() && after == 0.toDouble() -> nextState // overlapping

        before == 1.toDouble() && after == 2.toDouble() -> alsoMoveTail // vertical or horizontal

        before == 1.toDouble() && after < 2 -> nextState // creating a diagonal
        before < 2 && after == 1.toDouble() -> nextState // removing a diagonal

        else -> snapTail // everything else should snap...
    }

fun moveRight(dayNineState: DayNineState): DayNineState = run {
    val nextState = DayNineState.head.col.modify(dayNineState) { it + 1 }
    val head = DayNineState.head.get(nextState)
    val alsoMoveTail = DayNineState.tail.col.modify(nextState) { it + 1 }
    val snapTail = DayNineState.tail.modify(nextState) { Point.col.modify(head) { col -> col - 1 } }
    pickMovement(dayNineState.distance(), nextState.distance(), nextState, alsoMoveTail, snapTail)
}

fun moveLeft(dayNineState: DayNineState): DayNineState = run {
    val nextState = DayNineState.head.col.modify(dayNineState) { it - 1 }
    val head = DayNineState.head.get(nextState)
    val alsoMoveTail = DayNineState.tail.col.modify(nextState) { it - 1 }
    val snapTail = DayNineState.tail.modify(nextState) { Point.col.modify(head) { col -> col + 1 } }
    if (dayNineState.head.isLeftCol()) {
        dayNineState
    } else {
        pickMovement(dayNineState.distance(), nextState.distance(), nextState, alsoMoveTail, snapTail)
    }
}

fun moveUp(dayNineState: DayNineState): DayNineState = run {
    val nextState = DayNineState.head.row.modify(dayNineState) { it + 1 }
    val head = DayNineState.head.get(nextState)
    val alsoMoveTail = DayNineState.tail.row.modify(nextState) { it + 1 }
    val snapTail = DayNineState.tail.modify(nextState) { Point.row.modify(head) { row -> row - 1 } }
    pickMovement(dayNineState.distance(), nextState.distance(), nextState, alsoMoveTail, snapTail)
}

fun moveDown(dayNineState: DayNineState): DayNineState = run {
    val nextState = DayNineState.head.row.modify(dayNineState) { it - 1 }
    val head = DayNineState.head.get(nextState)
    val alsoMoveTail = DayNineState.tail.row.modify(nextState) { it - 1 }
    val snapTail = DayNineState.tail.modify(nextState) { Point.row.modify(head) { row -> row + 1 } }
    if (dayNineState.head.isBottomRow()) {
        dayNineState
    } else {
        pickMovement(dayNineState.distance(), nextState.distance(), nextState, alsoMoveTail, snapTail)
    }
}

fun applyMovementTo(dayNineState: DayNineState, movement: (DayNineState) -> DayNineState): DayNineState {
    val nextState = movement(dayNineState)
    val seen = nextState.tail.row to nextState.tail.col
    return DayNineState.seenTail.modify(nextState) { s -> s + setOf(seen) }
}

fun printSeenTail(seenTail: Set<Pair<Int, Int>>) = run {
    val maxRow = seenTail.maxOf { it.first } + 1
    val maxCol = seenTail.maxOf { it.second } + 1
    // Initialize the character grid
    val charGrid: MutableList<MutableList<Char>> = mutableListOf()
    (0 until maxRow).forEach { row ->
        val buildList: MutableList<Char> = mutableListOf()
        (0 until maxCol).forEach { col ->
            buildList.add(col, '.')
        }
        charGrid.add(row, buildList)
    }
    seenTail.forEach { p ->
        charGrid[p.first][p.second] = '#'
    }
    charGrid.forEach { row ->
        row.forEach { col ->
            print(col)
        }
        println()
    }
}

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun dayNine() {
    val path = Path("inputFiles/dayNine.txt")
    val movements: List<Movement> =
        lines(path)
            .parMap { input ->
                when (val r = parseDirection.parse(input)) {
                    is ParserResult.Ok -> r.result
                    is ParserResult.Error -> throw Error("Unable to parse $input")
                }
            }
            .toList()

    val initialState = DayNineState(
        Point(0, 0),
        Point(0, 0),
        setOf(0 to 0)
    )

    val finalState =
        movements.fold(initialState) { acc, movement ->
            val indices = 0 until movement.magnitude
            when (movement.direction) {
                Direction.UP ->
                    indices.fold(acc) { x, _ -> applyMovementTo(x) { moveUp(it) } }

                Direction.DOWN ->
                    indices.fold(acc) { x, _ -> applyMovementTo(x) { moveDown(it) } }

                Direction.LEFT ->
                    indices.fold(acc) { x, _ -> applyMovementTo(x) { moveLeft(it) } }

                Direction.RIGHT ->
                    indices.fold(acc) { x, _ -> applyMovementTo(x) { moveRight(it) } }
            }
        }

    // println(printSeenTail(finalState.seenTail))
    println(finalState.seenTail) // works for the test input, but not the test input :shrug:
    println(finalState.seenTail.size)
}