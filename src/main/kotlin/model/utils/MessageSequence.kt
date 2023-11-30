package model.utils

object MessageSequence {
    private var currentId : Long = 0

    fun getNextSequence(): Long {
        if (currentId == Long.MAX_VALUE) {
            currentId = 0
        }
        return currentId++
    }
}