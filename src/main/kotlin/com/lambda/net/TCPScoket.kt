package com.lambda.net

import com.lambda.net.packet.Packet
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TCPSocket(private val address: String = "localhost", private val port: Int = 42069) {
    private var socket: Socket? = null
    private var recvChannel: ByteReadChannel? = null
    private var sendChannel: ByteWriteChannel? = null
    private val packet = Packet.nil()
    private var handlers = mutableMapOf<Int, suspend (TCPSocket, Packet) -> Unit>()

    suspend fun doDial() {
        val manager = SelectorManager(Dispatchers.IO)
        socket = aSocket(manager).tcp().connect(address, port)
        recvChannel = socket!!.openReadChannel()
        sendChannel = socket!!.openWriteChannel()

        while (true) {
            packet.unpack(recvChannel!!)
            handlers[packet.id]?.invoke(this@TCPSocket, packet)
        }
    }

    suspend fun writePacket(p: Packet) {
        p.pack(sendChannel!!)
    }

    fun javaWrite(packet: Packet) {
        runBlocking {
            launch {
                packet.pack(sendChannel!!)
            }
        }
    }

    fun addHandler(vararg handlers: Pair<Int, suspend (TCPSocket, Packet) -> Unit>) {
        handlers.forEach {
            this.handlers[it.first] = it.second
        }
    }

    fun isConnected(): Boolean {
        return socket != null
    }

    fun close() {
        socket = null
        recvChannel = null
        sendChannel = null
    }
}
