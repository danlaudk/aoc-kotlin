package dayFive

import arrow.fx.coroutines.parMap
import cc.ekblad.konbini.*
import flows.lines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.fold
import kotlin.io.path.Path

sealed class InputLineOrAction
data class InputLine(val value: List<Char?>) : InputLineOrAction()
data class Action(val amount: Int, val fromColumn: Int, val toColumn: Int) : InputLineOrAction()
object Empty : InputLineOrAction()

// '[A]'
val parseCrate: Parser<Char?> = parser {
    char('[')
    val inner = char()
    char(']')
    inner
}

// '   '
val parseSlot: Parser<Char?> = parser {
    string("   ")
    null
}

val parseCrateOrSlot = parser {
    oneOf(parseCrate, parseSlot)
}

val parseInputLine: Parser<InputLine> = parser {
    val result = chain1(parseCrateOrSlot, parser { char(' ') })
    InputLine(result.terms)
}

val parseAction: Parser<Action> = parser {
    string("move")
    whitespace()
    val amount = integer()
    whitespace()
    string("from")
    whitespace()
    val fromColumn = integer()
    whitespace()
    string("to")
    whitespace()
    val toColumn = integer()
    Action(amount.toInt(), fromColumn.toInt() - 1, toColumn.toInt() - 1)
}

val parseNumberLine: Parser<InputLineOrAction> = parser {
    chain1(integer, whitespace)
    Empty
}

val parseNumberLineOrEmptyLine: Parser<InputLineOrAction> = parser {
    oneOf(parseNumberLine, parser { whitespace(); Empty })
    Empty
}

// TODO: Technically this parses the lines `1 2 3 ...` and fully blank lines as `InputLine`
val parseInputLineOrAction: Parser<InputLineOrAction> = parser {
    oneOf(parseAction, parseInputLine, parseNumberLineOrEmptyLine)
}

typealias Stacks = Map<Int, List<Char>>

fun applyMovement(map: Stacks, from: Int, to: Int): Stacks = run {
    map[from]?.last()?.let { crate ->
        val fromStack = map[from]?.dropLast(1).orEmpty()
        val toStack = map.getOrDefault(to, emptyList()) + listOf(crate)
        return map + mapOf(from to fromStack, to to toStack)
    }
    map
}

fun applyAction(map: Stacks, action: Action): Stacks = run {
    (0 until action.amount).fold(map) { acc, _ ->
        println("Begin apply movement with: $acc")
        val result = applyMovement(acc, action.fromColumn, action.toColumn)
        println("End apply movement got: $result")
        result
    }
}

fun runActionsPartOne(p: Pair<Stacks, List<Action>>): Stacks = run {
    p.second.fold(p.first) { acc, action ->
        applyAction(acc, action)
    }
}

fun applyActionPartTwo(map: Stacks, action: Action): Stacks =
    map[action.fromColumn]?.takeLast(action.amount)?.let { crates ->
        val fromStack = map[action.fromColumn]?.dropLast(action.amount).orEmpty()
        val toStack = map.getOrDefault(action.toColumn, emptyList()) + crates
        map + mapOf(action.fromColumn to fromStack, action.toColumn to toStack)
    } ?: map

fun runActionsPartTwo(p: Pair<Stacks, List<Action>>): Stacks =
    p.second.fold(p.first) { acc, action -> applyActionPartTwo(acc, action) }

fun printTopOfStacks(stack: Stacks) =
    stack
        .toList()
        .sortedBy { (idx, _) -> idx }
        .map { (_, xs) -> xs.last() }
        .joinToString(separator = "")

@ExperimentalCoroutinesApi
@FlowPreview
suspend fun dayFive() = coroutineScope {
    val path = Path("inputFiles/dayFive.txt")
    val inputs =
        lines(path)
            .parMap { line ->
                when (val r = parseInputLineOrAction.parse(line)) {
                    is ParserResult.Ok -> r.result
                    is ParserResult.Error -> throw Error("Unable to parse $line")
                }
            }
            .fold(mutableMapOf<Int, List<Char>>() to emptyList<Action>()) { acc, x ->
                when (x) {
                    is InputLine -> run {
                        x.value.mapIndexed {
                            // if the character exists, prepend it in the location
                                idx, mChar ->
                            mChar?.let { c ->
                                when (val xs = acc.first[idx]) {
                                    is List -> acc.first.put(idx, listOf(c) + xs)
                                    else -> acc.first.put(idx, listOf(c))
                                }
                            }
                        }
                        acc
                    }

                    is Action -> acc.first to acc.second + x
                    is Empty -> run {
                        println("Skipping empty line")
                        acc
                    }
                }
            }
    val partOneResult = printTopOfStacks(runActionsPartOne(inputs))
    println("Part 1: $partOneResult")

    val partTwoResult = printTopOfStacks(runActionsPartTwo(inputs))
    println("Part 2: $partTwoResult")
}