package dayOne

import arrow.fx.coroutines.parMap
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.*

@ExperimentalCoroutinesApi
fun CoroutineScope.windows(numElements: Int = 2) =
    produce {
        File("inputFiles/dayOne.test.txt").inputStream().bufferedReader().useLines { lines ->
            // `window` is a rolling window over the lines in the file
            // when we have enough elements, `send` the window, and prepare the next window
            var window = emptyList<String>()
            for (line in lines) {
                window = window + listOf(line)
                if (window.size == numElements) {
                    send(window)
                    window = window.drop(1)
                }
            }
        }
    }

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

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun dayOne() = coroutineScope {
    val result = windows(3).consumeAsFlow().parMap { strictlyIncreasing(it) }.fold(0) { acc, x -> acc + x }
    println("The final result is $result")
}