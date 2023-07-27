package com.lambda.net.packet

import java.io.ByteArrayOutputStream

class Builder {
    private val buf = ByteArrayOutputStream()

    fun writeField(vararg fields: Any) {
        for (f in fields) {
            f.writeTo(buf)
        }
    }

    fun packet(id: kotlin.Int): Packet {
        return Packet(id, buf.toByteArray())
    }
}