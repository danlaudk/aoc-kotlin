package dayThree

import arrow.fx.coroutines.parMapUnordered
import flows.lines
import flows.windows
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.fold
import kotlin.io.path.Path

fun splitIntoHalves(x: String): Pair<String, String> {
    val halfSize: Int = x.length / 2
    val firstHalf = x.take(halfSize)
    val secondHalf = x.drop(halfSize)
    return firstHalf to secondHalf
}

fun determineOverlappingCharacters(p: Pair<String, String>) =
    p.first.toSet().intersect(p.second.toSet())

const val lowerMin = 'a'.code
const val upperMin = 'A'.code

fun scoreCharacter(x: Char) = when (x) {
    in 'a'..'z' -> x.code - lowerMin + 1
    in 'A'..'Z' -> x.code - upperMin + 27
    else -> error("Unrecognized character $x")
}

fun scoreCharacters(xs: Set<Char>) =
    xs.fold(0) { acc, c -> acc + scoreCharacter(c) }

fun elfGroupBadge(xs: List<String>): Set<Char> = run {
    if (xs.size != 3) {
        throw Error("Expected list of size 3, found: $xs")
    }
    val first = xs.first().toSet()
    xs.drop(1).fold(first) { acc, x ->
        acc.intersect(x.toSet())
    }
}

@FlowPreview
suspend fun dayThree() {
    val path = Path("inputFiles/dayThree.txt")
    val resultPartOne =
        lines(path)
            .parMapUnordered { splitIntoHalves(it) }
            .parMapUnordered { determineOverlappingCharacters(it) }
            .parMapUnordered { scoreCharacters(it) }
            .fold(0) { acc, x -> acc + x }
    println("Part 1: $resultPartOne")

    val resultPartTwo =
        windows(3, 3, path)
            .parMapUnordered { elfGroupBadge(it) }
            .parMapUnordered { scoreCharacters(it) }
            .fold(0) { acc, x -> acc + x }

    println("Part 2: $resultPartTwo")
}