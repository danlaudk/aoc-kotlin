package grid

import arrow.optics.optics

class IncorrectGridDimensions(message:String): Exception(message)

@optics
data class Point(val row: Int, val col: Int) {
    companion object
}

class Grid<T> {
    private var rows: Int
    private var cols: Int
    private val data: MutableList<T>

    constructor(rows: Int, cols: Int, data: List<T>) {
        if (rows * cols != data.size) {
            throw IncorrectGridDimensions("The data has size ${data.size} but you told me it was ${rows * cols}")
        }
        this.rows = rows
        this.cols = cols
        this.data = data.toMutableList()
    }

    private fun inBounds(p: Point): Boolean =
        p.row < 0 || p.row > this.rows || p.col < 0 || p.col > this.cols

    private fun toIndex(p: Point): Int =
       this.cols * p.row + p.col

    operator fun get(p: Point): T? = run {
        if (inBounds(p)) {
            null
        } else {
            this.data[toIndex(p)]
        }
    }

    /**
     * If the row/column are valid, set the value and return it
     * Else, return null
     */
    operator fun set(p: Point, value: T): T? = run {
        if (inBounds(p)) {
            null
        } else {
            this.data[toIndex(p)] = value
            value
        }
    }

    fun display() {
        for (row in 0 until this.rows) {
            for (col in 0 until this.cols) {
                val idx = toIndex(Point(row, col))
                print("${this.data[idx]}, ")
            }
            println()
        }
    }

    fun first(pred: (T) -> Boolean) = run {
        val idx = this.data.indexOfFirst(pred)
        if (idx == -1) {
            null
        } else {
            val row = idx / this.rows
            val col = idx % this.cols
            Point(row, col)
        }
    }

    fun up(p: Point): T? = run {
        val up = Point.row.modify(p) { it - 1 }
        this[up]
    }

    fun down(p: Point): T? = run {
        val down = Point.row.modify(p) { it + 1 }
        this[down]
    }

    fun left(p: Point): T? = run {
        val left = Point.col.modify(p) { it - 1 }
        this[left]
    }

    fun right(p: Point): T? = run {
        val right = Point.col.modify(p) { it + 1 }
        this[right]
    }

    fun rows() = run {
        this.rows
    }

    fun cols() = run {
        this.cols
    }

    fun dimensions() = run {
        this.rows to this.cols
    }
}