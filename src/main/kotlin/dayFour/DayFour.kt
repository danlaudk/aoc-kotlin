package dayFour

import arrow.fx.coroutines.parMapUnordered
import cc.ekblad.konbini.*
import flows.lines
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.fold
import kotlin.io.path.Path

// Parses '1-2', convert (1..2) to a Set to do contains operations
val assignmentParser = parser {
    val first = integer()
    char('-')
    val second = integer()
    (first..second).toSet()
}

// Parses '1-2,2-3' as two assignments, pairs them up
val elvesAssignmentParser = parser {
    val first = assignmentParser()
    char(',')
    val second = assignmentParser()
    first to second
}

// If either Set fully contains the other, they overlap (part one)
fun eitherContainsAll(p: Pair<Set<Long>, Set<Long>>): Boolean =
    p.first.containsAll(p.second) || p.second.containsAll(p.first)

// If either Set contains any elements from the other, they overlap (part two)
fun eitherContains(p: Pair<Set<Long>, Set<Long>>): Boolean =
    p.first.intersect(p.second).isNotEmpty()
// p.first.any { x -> p.second.contains(x) }

@FlowPreview
suspend fun dayFour() {
    val path = Path("inputFiles/dayFour.txt")
    val partOneResult = lines(path)
        .parMapUnordered { line ->
            when (val res = elvesAssignmentParser.parse(line)) {
                is ParserResult.Ok -> res.result
                is ParserResult.Error -> throw Error("Unable to parse line: $line")
            }
        }
        .parMapUnordered { eitherContainsAll(it) }
        .fold(0) { acc, b ->
            if (b) {
                acc + 1
            } else {
                acc
            }
        }
    println("Part One: $partOneResult")

    val partTwoResult = lines(path)
        .parMapUnordered { line ->
            when (val res = elvesAssignmentParser.parse(line)) {
                is ParserResult.Ok -> res.result
                is ParserResult.Error -> throw Error("Unable to parse line: $line")
            }
        }
        .parMapUnordered { eitherContains(it) }
        .fold(0) { acc, b ->
            if (b) {
                acc + 1
            } else {
                acc
            }
        }
    println("Part Two: $partTwoResult")
}