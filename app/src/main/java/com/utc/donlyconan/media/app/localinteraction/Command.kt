package com.utc.donlyconan.media.app.localinteraction

import com.utc.donlyconan.media.data.models.Video
import kotlinx.android.parcel.Parcelize
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

    fun toByteBuffer(): ByteBuffer {
        val buffer = ByteBuffer.allocate(bytes.size + 1)
        buffer.put(code)
        buffer.put(bytes)
        return buffer
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
        val CODE_MESSAGE_SEND: Byte = 1
        val CODE_OBJECT_SEND: Byte = 2
        val CODE_FILE_START: Byte = 11
        val CODE_FILE_SENDING: Byte = 12
        val CODE_FILE_END: Byte = 13
        val CODE_VIDEO_START: Byte = 14

        @JvmStatic
        fun from(buffer: ByteBuffer): Command = Command(buffer)

        @JvmStatic
        fun from(msg: String): Command = Command(CODE_MESSAGE_SEND, msg.toByteArray())

        @JvmStatic
        fun from(code: Byte, bytes: ByteArray) = Command(code, bytes)
    }
}