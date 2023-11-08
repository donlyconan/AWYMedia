package com.utc.donlyconan.media.app.localinteraction

import java.nio.ByteBuffer
import kotlin.reflect.KClass

class Command private constructor(){
    var code: Byte = 0
    lateinit var bytes: ByteArray

    private constructor(code: Byte, bytes: ByteArray) : this() {
        this.code = code
        this.bytes = bytes
    }

    private constructor(buffer: ByteBuffer) : this() {
        code = buffer.get()
        bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
    }

    fun putInto(buffer: ByteBuffer) {
        buffer.put(code)
        buffer.put(bytes)
    }

    fun toByteArray(): ByteArray {
        return ByteArray(bytes.size + 1).apply {
            set(0, code)
            for (i in 0 until bytes.size) {
                set(i + 1, bytes[i])
            }
        }
    }

    inline fun <reified T: Any> get(kClass: KClass<T>): T? {
        return when(kClass) {
            String::class -> String(bytes, Charsets.UTF_8).trim() as T
            ByteArray::class -> bytes as? T
            else -> {
                Log("kClass: $kClass is not found.")
                null
            }
        }
    }

    override fun toString(): String {
        return "code=$code, bytes.size=${bytes.size}"
    }

    companion object {
        const val CODE_MESSAGE_SEND: Byte   = 1
        const val CODE_OBJECT_SEND: Byte    = 2

        const val CODE_FILE_START: Byte     = 11
        const val CODE_FILE_SENDING: Byte   = 12
        const val CODE_FILE_END: Byte       = 13
        const val CODE_VIDEO_ENCODE: Byte   = 14

        const val CODE_ADJUST_POSITION: Byte= 15
        const val CODE_CHANGE_SPEED: Byte   = 16

        @JvmStatic
        fun from(buffer: ByteBuffer): Command = Command(buffer)

        @JvmStatic
        fun from(msg: String): Command = Command(CODE_MESSAGE_SEND, msg.toByteArray())

        @JvmStatic
        fun from(code: Byte, bytes: ByteArray) = Command(code, bytes)

        @JvmStatic
        fun hasCode(code: Byte): Boolean {
            return when(code) {
                CODE_MESSAGE_SEND, CODE_OBJECT_SEND,
                CODE_FILE_START, CODE_FILE_SENDING, CODE_VIDEO_ENCODE,
                CODE_ADJUST_POSITION, CODE_CHANGE_SPEED,
                    -> true
                else -> false
            }
        }
    }
}