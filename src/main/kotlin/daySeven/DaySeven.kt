package daySeven

import arrow.fx.coroutines.*
import cc.ekblad.konbini.*
import flows.lines
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import kotlin.io.path.Path

sealed class CommandLineAction
object MoveToRoot : CommandLineAction()
data class ChangeDirectory(val value: String) : CommandLineAction()
object MoveUpDirectory : CommandLineAction()
object ListDirectory : CommandLineAction()
data class Directory(val directoryName: String) : CommandLineAction()
data class File(val fileName: String, val fileSize: Int) : CommandLineAction()

val parseFile = parser {
    val fileSize = integer()
    whitespace()
    val fileName = many1(char)
    File(fileName.toString(), fileSize.toInt())
}

val parseDirectory = parser {
    string("dir")
    whitespace()
    val directoryName = many1(char)
    Directory(directoryName.toString())
}

val parseListDirectory = parser {
    char('$')
    whitespace()
    string("ls")
    ListDirectory
}

val parseDirectoryMovement = parser {
    char('$')
    whitespace()
    string("cd")
    whitespace()
    when (val movement = many1(char)) {
        listOf('/') -> MoveToRoot
        listOf('.', '.') -> MoveUpDirectory
        else -> ChangeDirectory(movement.toString())
    }
}

val parseCommandLineOutput = parser {
    oneOf(parseFile, parseDirectory, parseListDirectory, parseDirectoryMovement)
}

sealed class RoseTree<out T : Any>
data class RoseNode<out T : Any>(val leaf: T, val node: List<RoseTree<T>>) : RoseTree<T>()

data class RoseZipper<out T: Any>(
    val focus: RoseNode<T>,
    val depth: Int,
    val aboveFocus: RoseTree<T>?
)

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun daySeven() = coroutineScope {
    val path = Path("inputFiles/daySeven.test.txt")
    val commandLineOutput = lines(path)
        .parMap { parseCommandLineOutput.parse(it) }
        .toList()
    println(commandLineOutput)

    val roseZipper = RoseZipper(
        RoseNode("/", emptyList()), // initial empty root directory
        0, // no subdirectories
        null, // nothing above the root directory
    )

    // ChangeDirectory('hello')
    val roseZipperAfterChangeDirectory =
        RoseZipper(
            RoseNode("hello", emptyList()),
            1,
            RoseNode("/", emptyList())
        )

    // MoveUpDirectory && ChangeDirectory("world")
    val roseZipperAfterActions =
        RoseZipper(
            RoseNode("world", emptyList()),
            1,
            RoseNode("/", listOf(RoseNode("hello", emptyList())))
        )

    commandLineOutput.fold(roseZipper) { acc, p -> when (p) {
        is ParserResult.Ok -> when (p.result) {
            MoveToRoot -> acc
            is ChangeDirectory -> acc
            MoveUpDirectory -> acc
            ListDirectory -> acc
            is Directory -> acc
            is File -> acc
        }
        is ParserResult.Error -> error("Something broke...")
    }}
}