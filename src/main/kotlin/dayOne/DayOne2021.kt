package dayOne

import arrow.fx.coroutines.parMap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Random
import flows.*
import kotlin.io.path.Path

suspend fun strictlyIncreasing(xs: List<String>) = run {
    // Simulate a larger computation
    val delayLength = Random().nextLong(0, 2000)
    println("Starting $xs with delay $delayLength")
    delay(delayLength)

    // The actual computation
    val asIntegers = xs.map { it.toInt() }
    val isIncreasing = asIntegers.zip(asIntegers.drop(1)).fold(true) { acc, pair ->
        acc && pair.first < pair.second
    }
    if (isIncreasing) {
        1
    } else {
        0
    }
}

// TODO: If windows, returned a `MutableStateFlow` we could accumulate inside `strictlyIncreasing`
@FlowPreview
@ExperimentalCoroutinesApi
suspend fun dayOne2021() = coroutineScope {
    val path = Path("inputFiles/dayOne.test.txt")
    val result =
        windows(2, path)
            .parMap { strictlyIncreasing(it) }
            .fold(0) { acc, x -> acc + x }
    println("The final result is $result")
}