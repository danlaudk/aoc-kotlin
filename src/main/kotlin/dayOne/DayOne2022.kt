package dayOne

import arrow.fx.coroutines.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.useLines

fun elfItems(path: Path) = flow {
    path.useLines { lines ->
        var builder = emptyList<String>()
        for (line in lines) {
            builder = if (line == "") {
                emit(builder.toList())
                emptyList()
            } else {
                builder + listOf(line)
            }
        }
        emit(builder)
    }
}.flowOn(Dispatchers.IO)

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun dayOne2022() = coroutineScope {
    val path = Path("inputFiles/dayOne2022.txt")
    val maximumCalories = elfItems(path)
        .parMapUnordered { xs -> xs.map { it.toInt() } }
        .parMapUnordered { it.sum() }
        .fold(emptyList<Int>()) { acc, x ->
            if (acc.size == 3) {
                (acc + listOf(x)).sortedDescending().take(3)
            } else {
                acc + listOf(x)
            }
        }.sum()
    println("The maximum calories is $maximumCalories")
}