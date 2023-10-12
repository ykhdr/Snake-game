package gameobjects

data class Lobby(val masterName: String,
                 val masterIp: String,
                 val groupSize: Int,
                 val fieldSize: FieldSize,
                 val foodStatic: Int,
                 val snakesAlive: Int,
                 )
