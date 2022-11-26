package flows

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.nio.file.Path

import kotlin.io.path.useLines

fun lines(path: Path) =
    path.useLines { lines -> lines.asFlow().flowOn(Dispatchers.IO) }

fun windows(numElements: Int = 2, path: Path) = flow {
    path.useLines { lines ->
        lines.windowed(numElements).forEach { emit(it) }
    }
}.flowOn(Dispatchers.IO)