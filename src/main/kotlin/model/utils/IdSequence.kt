package model.utils

object IdSequence {
    private var currentId = 0

    fun setStartId(id: Int){
        currentId = id
    }

    fun getNextId(): Int {
        if (currentId == Int.MAX_VALUE) {
            currentId = 0
        }
        return currentId++
    }
}