package config

import java.net.InetSocketAddress

data class ApiConfig(
    val groupAddress: InetSocketAddress = groupAddress()
){
    companion object Defaults{
        fun groupAddress(): InetSocketAddress = InetSocketAddress("239.192.0.4", 9192)
    }
}
