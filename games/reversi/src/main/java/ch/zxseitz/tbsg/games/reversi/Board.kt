package ch.zxseitz.tbsg.games.reversi

class Board(private val _fields: IntArray = IntArray(SIZE)) {
    companion object {
        const val SIZE = 64
        private val EMPTY_INTS = ArrayList<Int>(0)

        /**
         *
         */
        fun index(x: Int, y: Int): Int {
            return if ((x in 0..7) && (y in 0..7)) y * 8 + x else -1
        }
    }

    val fields: IntArray
        get() {
            val result = IntArray(SIZE)
            System.arraycopy(_fields, 0, result, 0, SIZE)
            return result
        }

    fun init() {
        _fields.fill(0, 0, SIZE)
        _fields[27] = 2
        _fields[28] = 1
        _fields[35] = 1
        _fields[36] = 2
    }

    operator fun set(index: Int, color: Int) {
        if (index in 0 until SIZE) {
            _fields[index] = color
        }
    }

    operator fun set(indices: Collection<Int>, color: Int) {
        indices.forEach { index ->
            if (index in 0 until SIZE) {
                _fields[index] = color
            }
        }
    }

    operator fun get(index: Int): Int {
        return if (index in 0 until SIZE) _fields[index] else -1
    }

    fun getOpponentTokens(index: Int, color: Int, opponentColor: Int): Collection<Int>
    {
        val tokens = ArrayList<Int>()
        val x = index % 8
        val y = index / 8
        tokens.addAll(iterate(x, y, 1, 0, color, opponentColor))
        tokens.addAll(iterate(x, y, 1, 1, color, opponentColor))
        tokens.addAll(iterate(x, y, 0, 1, color, opponentColor))
        tokens.addAll(iterate(x, y, -1, 1, color, opponentColor))
        tokens.addAll(iterate(x, y, -1, 0, color, opponentColor))
        tokens.addAll(iterate(x, y, -1, -1, color, opponentColor))
        tokens.addAll(iterate(x, y, 0, -1, color, opponentColor))
        tokens.addAll(iterate(x, y, 1, -1, color, opponentColor))
        return tokens
    }

    private fun iterate(x: Int, y: Int, dx: Int, dy: Int, color: Int, opponentColor: Int): Collection<Int>
    {
        val fields = ArrayList<Int>()
        var ix = x + dx
        var iy = y + dy
        var i = index(ix, iy)
        var c = fields[i]
        while (i >= 0 && c == opponentColor) {
            fields.add(i)
            ix += dx
            iy += dy
            i = index(ix, iy)
            c = fields[i]
        }
        return if(c == color) fields else EMPTY_INTS
    }

    override fun toString(): String
    {
        return fields.contentToString()
    }
}