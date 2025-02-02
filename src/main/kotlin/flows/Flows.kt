package flows

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.yield
import java.nio.file.Path
import kotlin.io.path.useLines

fun lines(path: Path, skipWhitespace: Boolean = false) = flow {
    path.useLines { lines ->
        lines.forEach { line ->
            if (skipWhitespace && line.isEmpty()) {
                yield()
            } else {
                emit(line)
            }
        }
    }
}.flowOn(Dispatchers.IO)

fun windows(path: Path, numElements: Int = 2, step: Int = 1, skipWhitespace: Boolean = false) = flow {
    path.useLines { lines ->
        if (skipWhitespace) {
            lines.filter { it.isNotEmpty() }.windowed(numElements, step).forEach { emit(it) }
        } else {
            lines.windowed(numElements, step).forEach { emit(it) }
        }
    }
}.flowOn(Dispatchers.IO)
