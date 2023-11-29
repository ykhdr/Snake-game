package model.api.config

import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.NetworkInterface

data class NetworkConfig(
    val groupAddress: InetSocketAddress = groupAddress(),
    val localAddress: InetSocketAddress = localAddress(),
    val nodeSocket: DatagramSocket = nodeSocket(),
    val localInterface: NetworkInterface = localInterface()
) {
    companion object Defaults {
        private fun groupAddress(): InetSocketAddress = InetSocketAddress("239.192.0.4", 9192)
        private fun localAddress() : InetSocketAddress = InetSocketAddress("127.0.0.1",1246)
        private fun nodeSocket(): DatagramSocket = DatagramSocket()
        private fun localInterface(): NetworkInterface = NetworkInterface.getByName("wlp3s0")
    }
}
