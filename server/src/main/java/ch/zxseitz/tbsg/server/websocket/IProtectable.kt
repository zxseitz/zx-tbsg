package ch.zxseitz.tbsg.server.websocket;

interface IProtectable<T>: Comparable<T> {
    fun lock()
    fun unlock()
}
