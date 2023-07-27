package com.lambda.net.packet

import io.ktor.utils.io.*
import java.io.InputStream

class Packet(var id: Int, var data: ByteArray, var inputStream: InputStream? = null) {
    suspend fun pack(writer: ByteWriteChannel) {
        // Write the packet size
        writer.writeInt(4 + data.size)

        // Write the packet id
        writer.writeInt(id)

        // Write the packet data
        writer.writeFully(data)
    }

    suspend fun unpack(reader: ByteReadChannel) {
        // Read the packet size
        val size = reader.readInt()

        // Read the packet id
        val id = reader.readInt()

        val lenOfData = size - 4

        // Read the packet data
        val data = ByteArray(lenOfData)
        reader.readFully(data)

        // Set the packet id
        this.id = id

        // Set the packet data
        this.data = data

        // Set the packet input stream used for unmarshalling
        this.inputStream = data.inputStream()
    }

    companion object {
        fun marshal(id: Int, vararg block: Any): Packet {
            val builder = Builder()
            builder.writeField(*block)
            return builder.packet(id)
        }

        fun nil(): Packet {
            return Packet(0, ByteArray(0))
        }
    }
}