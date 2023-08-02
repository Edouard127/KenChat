package com.lambda.net.packet

import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import java.io.*
import java.nio.ByteBuffer
import java.util.*
import kotlin.Byte
import kotlin.collections.ArrayList

fun Boolean.writeTo(outputStream: OutputStream): Int {
    val v: Int = if (this) 0x01 else 0x00
    outputStream.write(byteArrayOf(v.toByte()))
    return 1
}

fun readBooleanFrom(inputStream: InputStream): Boolean {
    val v = inputStream.read()
    if (v != -1) {
        return v == 0x01
    } else {
        throw EOFException("End of stream reached")
    }
}

fun String.writeTo(outputStream: OutputStream): Int {
    val byteStr = this.toByteArray(Charsets.UTF_8)
    val size = byteStr.size
    size.writeTo(outputStream)
    outputStream.write(byteStr)
    return size + byteStr.size
}

fun readStringFrom(inputStream: InputStream): String {
    val size = readIntFrom(inputStream)
    val bs = ByteArray(size)
    val n = inputStream.read(bs)
    if (n != -1) {
        return String(bs, Charsets.UTF_8)
    } else {
        throw EOFException("End of stream reached")
    }
}

fun Byte.writeTo(outputStream: OutputStream): Int {
    outputStream.write(byteArrayOf(this))
    return 1
}

fun readByteFrom(inputStream: InputStream): Byte {
    val v = inputStream.read()
    if (v != -1) {
        return v.toByte()
    } else {
        throw EOFException("End of stream reached")
    }
}

fun Short.writeTo(outputStream: OutputStream): Int {
    val n = this.toUInt()
    val v = byteArrayOf(
        (n shr 8).toByte(),
        n.toByte()
    )
    outputStream.write(v)
    return v.size
}

fun readShortFrom(inputStream: InputStream): Short {
    val bs = ByteArray(2)
    inputStream.read(bs)
    return (((bs[0].toInt() shl 8)) or
        (bs[1].toInt())).toShort()
}

fun Int.writeTo(outputStream: OutputStream): Int {
    val n = this.toUInt()
    val v = byteArrayOf(
        (n shr 24).toByte(),
        (n shr 16).toByte(),
        (n shr 8).toByte(),
        n.toByte()
    )
    outputStream.write(v)
    return v.size
}

@OptIn(ExperimentalUnsignedTypes::class)
fun readIntFrom(inputStream: InputStream): Int {
    val bs = ByteArray(4)
    inputStream.read(bs)
    val ubs = bs.map { it.toUByte() }.toUByteArray()
    return (((ubs[0].toInt() shl 24)) or
        ((ubs[1].toInt() shl 16)) or
        ((ubs[2].toInt() shl 8)) or
        (ubs[3].toInt()))
}

fun ByteArray.writeTo(outputStream: OutputStream): Int {
    val size = this.size
    size.writeTo(outputStream)
    outputStream.write(this)
    return size + this.size
}

fun readByteArrayFrom(inputStream: InputStream): ByteArray {
    val size = readIntFrom(inputStream)
    val bs = ByteArray(size)
    val n = inputStream.read(bs)
    if (n != -1) {
        return bs
    } else {
        throw EOFException("End of stream reached")
    }
}

fun Array<*>.writeTo(outputStream: OutputStream): Int {
    val size = this.size
    size.writeTo(outputStream)
    for (i in 0 until size) {
        val v = this[i]!!
        v.writeTo(outputStream)
    }
    return 4+size
}

fun <Type : Any> readArrayFrom(inputStream: InputStream, typeValue: Type): Array<Type> {
    val size = readIntFrom(inputStream)
    val arr = java.lang.reflect.Array.newInstance(typeValue::class.java, size) as Array<Type>
    for (i in 0 until size) {
        val v = readFrom(inputStream, typeValue)
        arr[i] = v
    }
    return arr
}

fun Float.writeTo(outputStream: OutputStream): Int {
    val n = this.toRawBits()
    val v = byteArrayOf(
        (n shr 24).toByte(),
        (n shr 16).toByte(),
        (n shr 8).toByte(),
        n.toByte()
    )
    outputStream.write(v)
    return v.size
}

fun readFloatFrom(inputStream: InputStream): Float {
    val bs = ByteArray(4)
    inputStream.read(bs)
    return Float.fromBits(
        (((bs[0].toInt() shl 24)) or
            ((bs[1].toInt() shl 16)) or
            ((bs[2].toInt() shl 8)) or
            (bs[3].toInt()))
    )
}

fun Double.writeTo(outputStream: OutputStream): Int {
    return this.toRawBits().writeTo(outputStream)
}

fun readDoubleFrom(inputStream: InputStream): Double {
    val long = readLongFrom(inputStream)
    return Double.fromBits(long)
}

fun Long.writeTo(outputStream: OutputStream): Int {
    val buf = ByteBuffer.allocate(ULong.SIZE_BYTES)
    buf.putLong(this)
    val array = buf.array()
    outputStream.write(array)
    return array.size
}

fun readLongFrom(inputStream: InputStream): Long {
    val bs = ByteArray(8)
    inputStream.read(bs)

    val buf = ByteBuffer.allocate(Long.SIZE_BYTES)
    buf.put(bs)
    buf.flip()
    return buf.long
}

fun UUID.writeTo(outputStream: OutputStream): Int {
    val str = this.toString()
    val bs = str.toByteArray(Charsets.UTF_8)
    bs.writeTo(outputStream)
    return 4 + bs.size
}

fun readUUIDFrom(inputStream: InputStream): UUID {
    val len = readIntFrom(inputStream)
    val bs = ByteArray(len)
    val n = inputStream.read(bs)
    if (n != -1) {
        return UUID.nameUUIDFromBytes(bs)
    } else {
        throw EOFException("End of stream reached")
    }
}

fun ITextComponent.writeTo(outputStream: OutputStream): Int {
    val v = ITextComponent.Serializer.componentToJson(this)
    v.writeTo(outputStream)
    return v.length
}

fun ITextComponent.fromString(str: String): ITextComponent {
    return TextComponentString(str)
}

fun readTextComponentFrom(inputStream: InputStream): ITextComponent {
    val size = readIntFrom(inputStream)
    val bs = ByteArray(size)
    val n = inputStream.read(bs)
    if (n != -1) {
        return ITextComponent.Serializer.jsonToComponent(String(bs, Charsets.UTF_8))!!
    } else {
        throw EOFException("End of stream reached")
    }
}

fun Any.writeTo(outputStream: OutputStream): Int {
    return when (this) {
        is Boolean -> this.writeTo(outputStream)
        is Byte -> this.writeTo(outputStream)
        is Short -> this.writeTo(outputStream)
        is Int -> this.writeTo(outputStream)
        is Float -> this.writeTo(outputStream)
        is Double -> this.writeTo(outputStream)
        is Long -> this.writeTo(outputStream)
        is UUID -> this.writeTo(outputStream)
        is ITextComponent -> this.writeTo(outputStream)
        is String -> this.writeTo(outputStream)
        is ByteArray -> this.writeTo(outputStream)
        is Array<*> -> this.writeTo(outputStream)
        else -> throw Error("Unsupported type ${this::class}")
    }
}

fun <T : Any> readFrom(inputStream: InputStream, typeValue: T): T {
    return when (typeValue) {
        is Boolean -> readBooleanFrom(inputStream) as T
        is Byte -> readByteFrom(inputStream) as T
        is Short -> readShortFrom(inputStream) as T
        is Int -> readIntFrom(inputStream) as T
        is Float -> readFloatFrom(inputStream) as T
        is Double -> readDoubleFrom(inputStream) as T
        is Long -> readLongFrom(inputStream) as T
        is UUID -> readUUIDFrom(inputStream) as T
        is ITextComponent -> readTextComponentFrom(inputStream) as T
        is String -> readStringFrom(inputStream) as T
        is ByteArray -> readByteArrayFrom(inputStream) as T
        is Array<*> -> readArrayFrom(inputStream, typeValue) as T
        else -> throw Error("Unsupported type ${typeValue::class}")
    }
}


