package com.utc.donlyconan.media.app.localinteraction

import android.provider.MediaStore.Video
import kotlin.reflect.KClass

class Packet private constructor(){
    private lateinit var bytes: ByteArray
    private var length = 0

    private constructor(code: Byte, bytes: ByteArray) : this() {
        this.bytes = ByteArray(bytes.size + 1)
        this.bytes[0] = code
        this.bytes.fill(bytes, 1)
        length = bytes.size
    }

    private constructor(bytes: ByteArray, length: Int) : this() {
        this.bytes = bytes
        this.length = length
    }

    fun code(code: Byte) {
        bytes[0] = code
    }

    fun code(): Byte {
        return bytes[0]
    }

    fun bytes(): ByteArray {
        return bytes
    }

    fun data(): ByteArray {
        return bytes.copyOfRange(1, length)
    }

    inline fun <reified T: Any> get(): T? {
        return when(T::class) {
            String::class -> String(data(), Charsets.UTF_8).trim() as? T
            ByteArray::class -> data() as? T
            Video::class -> data().serialize() as? T
            else -> {
                Log("kClass: ${T::class.simpleName} is not found.")
                null
            }
        }
    }

    override fun toString(): String {
        return "code=${code()}, data.size=${bytes.size - 1}"
    }

    companion object {
        const val CODE_MESSAGE_SEND: Byte   = 1
        const val CODE_OBJECT_SEND: Byte    = 2

        const val CODE_FILE_START: Byte     = 11
        const val CODE_FILE_SENDING: Byte   = 12
        const val CODE_FILE_END: Byte       = 13
        const val CODE_VIDEO_ENCODE: Byte   = 14
        const val CODE_SUBTITLE_ENCODE: Byte   = 15

        const val CODE_ADJUST_POSITION: Byte= 30
        const val CODE_CHANGE_SPEED: Byte   = 31
        const val CODE_DEVICE_NAME: Byte = 32

        @JvmStatic
        fun from(msg: String): Packet = Packet(CODE_MESSAGE_SEND, msg.toByteArray())

        fun from(bytes: ByteArray, length: Int = bytes.size): Packet = Packet(bytes, length)

        @JvmStatic
        fun from(code: Byte, bytes: ByteArray) = Packet(code, bytes)

        @JvmStatic
        fun hasCode(code: Byte): Boolean {
            return when(code) {
                CODE_MESSAGE_SEND, CODE_OBJECT_SEND,
                CODE_FILE_START, CODE_FILE_SENDING, CODE_VIDEO_ENCODE,
                CODE_ADJUST_POSITION, CODE_CHANGE_SPEED,
                CODE_DEVICE_NAME,
                    -> true
                else -> false
            }
        }
    }
}