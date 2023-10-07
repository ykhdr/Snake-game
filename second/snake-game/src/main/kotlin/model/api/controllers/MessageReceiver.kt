package model.api.controllers

import java.net.DatagramPacket
import java.net.MulticastSocket
import java.util.concurrent.atomic.AtomicBoolean


object MessageReceiver {
    private const val BUFFER_SIZE = 1024

    private val buffer = ByteArray(BUFFER_SIZE)

    private var running = AtomicBoolean(true)

    fun receive(socket: MulticastSocket): ByteArray {
        val datagramPacket = DatagramPacket(buffer, buffer.size)
        socket.receive(datagramPacket)
        return datagramPacket.data.copyOf(datagramPacket.length)
    }




    fun stop() {
        running.set(false)
    }


}