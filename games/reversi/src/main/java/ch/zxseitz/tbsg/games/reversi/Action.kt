package ch.zxseitz.tbsg.games.reversi;

data class Action(val index: Int): Comparable<Action> {
    override fun compareTo(other: Action): Int {
        return index.compareTo(other.index);
    }
}
