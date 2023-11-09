package com.utc.donlyconan.media.app.localinteraction

import java.nio.ByteBuffer
import kotlin.reflect.KClass

class Packet private constructor(){
    var code: Byte = 0
        private set
    lateinit var bytes: ByteArray
        private set

    private constructor(code: Byte, bytes: ByteArray) : this() {
        this.code = code
        this.bytes = bytes
    }

    private constructor(bytes: ByteArray) : this() {
        this.code = bytes[0]
        this.bytes = bytes.copyOfRange(1, bytes.size)
    }

    fun toByteArray(): ByteArray {
        return ByteArray(bytes.size + 1).apply {
            this[0] = code
            fill(bytes, 1)
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
        fun from(msg: String): Packet = Packet(CODE_MESSAGE_SEND, msg.toByteArray())

        fun from(bytes: ByteArray): Packet = Packet(bytes)

        @JvmStatic
        fun from(code: Byte, bytes: ByteArray) = Packet(code, bytes)

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