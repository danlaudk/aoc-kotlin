package flows

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.nio.file.Path

import kotlin.io.path.useLines

fun lines(path: Path) = flow {
    path.useLines { lines ->
        lines.forEach { emit(it) }
    }
}.flowOn(Dispatchers.IO)

fun windows(numElements: Int = 2, path: Path) = flow {
    path.useLines { lines ->
        lines.windowed(numElements).forEach { emit(it) }
    }
}.flowOn(Dispatchers.IO)