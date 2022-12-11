package dayEleven

import arrow.fx.coroutines.parMap
import arrow.optics.optics
import arrow.optics.typeclasses.Index
import cc.ekblad.konbini.*
import flows.windows
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.toList
import java.math.BigInteger
import kotlin.io.path.Path

const val debug = false

sealed class Operation
object Add : Operation()
object Multiply : Operation()

sealed class Part
object PartOne : Part()
data class PartTwo(val reducer: (BigInteger) -> BigInteger) : Part()

@optics
data class Monkey(
    // Which monkey am I?
    val number: Int,

    // what items to I have?
    val items: List<BigInteger>,

    // inspect an item, change it with the operands and operation
    val operands: Pair<BigInteger?, BigInteger?>,
    val operation: Operation,

    // when new value is divisibleBy, throw to a new monkey
    val divisibleBy: BigInteger,
    val throwToTrue: Int,
    val throwToFalse: Int,

    val inspectionCount: BigInteger = 0.toBigInteger(),
) {
    companion object
}

val parseMonkeyNumber = parser {
    string("Monkey")
    whitespace()
    integer().toInt()
}

val parserMonkeyItems = parser {
    whitespace()
    string("Starting items:")
    whitespace()
    chain1(parser { integer().toBigInteger() }, parser { string(", ") }).terms
}

val parseOperand = parser {
    oneOf(parser { string("old"); null }, parser { integer().toBigInteger() })
}

val parseOperation = parser {
    when (val c = char()) {
        '*' -> Multiply
        '+' -> Add
        else -> fail("Unable to parse $c as operation")
    }
}

val parseOperationAndOperands = parser {
    whitespace()
    string("Operation: new =")
    whitespace()
    val operandOne = parseOperand()
    whitespace()
    val operation = parseOperation()
    whitespace()
    val operandTwo = parseOperand()
    operation to (operandOne to operandTwo)
}

val parseDivisibleBy = parser {
    whitespace()
    string("Test: divisible by")
    whitespace()
    integer().toInt()
}

val parseThrowTo = parser {
    whitespace()
    oneOf(
        parser { string("If true: throw to monkey") },
        parser { string("If false: throw to monkey") }
    )
    whitespace()
    integer().toInt()
}

fun <T> ParserResult<T>.getOrThrow() = when (this) {
    is ParserResult.Ok -> result
    is ParserResult.Error -> throw Error("$this")
}

fun initializeMonkey(input: List<String>): Monkey = run {
    val number = parseMonkeyNumber.parse(input[0]).getOrThrow()
    val items = parserMonkeyItems.parse(input[1]).getOrThrow()
    val operationAndOperand = parseOperationAndOperands.parse(input[2]).getOrThrow()
    val divisibleBy = parseDivisibleBy.parse(input[3]).getOrThrow()
    val throwToTrue = parseThrowTo.parse(input[4]).getOrThrow()
    val throwToFalse = parseThrowTo.parse(input[5]).getOrThrow()

    Monkey(
        number,
        items,
        operationAndOperand.second,
        operationAndOperand.first,
        divisibleBy.toBigInteger(),
        throwToTrue,
        throwToFalse,
    )
}

fun getFirstItem(monkey: Monkey): Pair<Monkey, BigInteger> = run {
    val item = monkey.items.first()
    val restItems = monkey.items.drop(1)
    val updatedItems = Monkey.items.modify(monkey) { restItems }
    Monkey.inspectionCount.modify(updatedItems) { it + 1.toBigInteger() } to item
}

fun computeWorryLevel(monkey: Monkey, old: BigInteger): BigInteger = run {
    when (monkey.operation) {
        Multiply -> (monkey.operands.first ?: old) * (monkey.operands.second ?: old)
        Add -> (monkey.operands.first ?: old) + (monkey.operands.second ?: old)
    }
}

fun simulate(monkeys: List<Monkey>, part: Part): List<Monkey> =
    monkeys.indices.fold(monkeys) { acc, idx ->
        acc[idx].items.indices.fold(acc) { inner, _ ->
            // Separate the item from the Monkey
            val fromIndex = Index.list<Monkey>().index(idx)
            val monkeyThrows = getFirstItem(inner[idx])
            if (debug) {
                println("[$idx]: Inspecting item: ${monkeyThrows.second}")
            }
            val newMonkeys = fromIndex.modify(inner) { monkeyThrows.first }

            // Throw the item
            val newWorryLevel = when (part) {
                PartOne -> computeWorryLevel(monkeyThrows.first, monkeyThrows.second).div(3.toBigInteger())
                is PartTwo -> part.reducer(computeWorryLevel(monkeyThrows.first, monkeyThrows.second))
            }
            if (debug) {
                println("[$idx]: new worry level $newWorryLevel")
            }
            if (newWorryLevel.mod(monkeyThrows.first.divisibleBy) == 0.toBigInteger()) {
                if (debug) {
                    println("[$idx]: divisible, throw to ${monkeyThrows.first.throwToTrue}")
                }
                val toIndex = Index.list<Monkey>().index(monkeyThrows.first.throwToTrue)
                toIndex.modify(newMonkeys) { m -> Monkey.items.modify(m) { it + listOf(newWorryLevel) } }
            } else {
                if (debug) {
                    println("[$idx]: not divisible, throw to ${monkeyThrows.first.throwToFalse}")
                }
                val toIndex = Index.list<Monkey>().index(monkeyThrows.first.throwToFalse)
                toIndex.modify(newMonkeys) { m -> Monkey.items.modify(m) { it + listOf(newWorryLevel) } }
            }
        }
    }

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun dayEleven() {
    val path = Path("inputFiles/dayEleven.txt")
    val monkeys =
        windows(path, numElements = 6, step = 6, skipWhitespace = true)
            .parMap { initializeMonkey(it) }
            .toList()

    if (debug) {
        monkeys.forEach { println(it) }
    }

    val monkeysAfterPartOne = (0 until 20).fold(monkeys) { acc, _ ->
        simulate(acc, PartOne)
    }
    if (debug) {
        println()
        monkeysAfterPartOne.forEach { println(it) }
    }
    monkeysAfterPartOne
        .map { it.inspectionCount }
        .sortedDescending()
        .take(2)
        .product()
        .also { println("Part One: $it") }

    val totalRounds = 10000
    // I looked this up,
    // The basic idea is you are normalizing your worry by the largest number
    // divisible by all `worry.mod(divisibleBy) == 0`. Really clever.
    val maxDivisibleBy = monkeys.map { it.divisibleBy }.reduce { x, y -> x * y }
    val monkeysAfterPartTwo = (0 until totalRounds).fold(monkeys) { acc, _ ->
        simulate(acc, PartTwo { it % maxDivisibleBy })
    }
    if (debug) {
        println()
        monkeysAfterPartTwo.forEach { println(it) }
    }
    monkeysAfterPartTwo
        .map { it.inspectionCount }
        .sortedDescending()
        .take(2)
        .product()
        .also { println("Part Two: $it") }
}

fun List<BigInteger>.product() =
    this.fold(1.toBigInteger()) { acc, x -> acc * x }