package model

class ProtoMapper private constructor(){
    companion object {

        @Volatile
        private var instance: ProtoMapper? = null

        fun getInstance() =
            instance ?: synchronized(this){
                instance ?: ProtoMapper().also { instance = it }
            }
    }

}