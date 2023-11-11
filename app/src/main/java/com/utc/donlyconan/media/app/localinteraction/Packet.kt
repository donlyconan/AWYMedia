package com.utc.donlyconan.media.app.localinteraction
import com.utc.donlyconan.media.data.models.Video
import java.nio.ByteBuffer

class Packet private constructor(){
    private val buffer: ByteBuffer by lazy { ByteBuffer.allocate(EGPSystem.DEFAULT_BUFFER_SIZE) }

    private constructor(code: Byte, bytes: ByteArray) : this() {
        buffer.put(code)
        buffer.putInt(bytes.size)
        buffer.put(bytes)
    }

    private constructor(bytes: ByteArray) : this() {
        buffer.put(bytes)
    }

    /**
     * Replace all data in the packet
     */
    fun replace(bytes: ByteArray): Packet {
        buffer.clear()
        buffer.put(bytes)
        return this
    }

    fun code(code: Byte): Packet {
        buffer.put(INDEX_CODE, code)
        return this
    }

    fun code(): Byte {
        return buffer.get(INDEX_CODE)
    }

    fun length(length: Int): Packet {
        buffer.putInt(INDEX_LENGTH, length)
        return this
    }

    fun length(): Int {
        return buffer.getInt(INDEX_LENGTH)
    }

    fun data(data: ByteArray): Packet {
        buffer.position(INDEX_LENGTH)
        buffer.put(data)
        return this
    }

    fun data(): ByteArray {
        return buffer.array().copyOfRange(INDEX_DATA, length() + INDEX_DATA)
    }

    fun bytes(): ByteArray {
        return buffer.array()
    }


    fun capacity(): Int {
        return buffer.capacity()
    }

    inline fun <reified T: Any> get(): T? {
        return when(T::class) {
            String::class -> String(data(), Charsets.UTF_8).trim() as? T
            ByteArray::class -> data() as? T
            Video::class -> data().deserialize() as? T
            else -> {
                Log("kClass: ${T::class.simpleName} is not found.")
                null
            }
        }
    }

    override fun toString(): String {
        return "Packet {code=${code()}, length=${length()}}"
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

        ///
        private const val INDEX_CODE = 0
        private const val INDEX_LENGTH = 1
        const val INDEX_DATA = 5

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
                CODE_DEVICE_NAME,
                    -> true
                else -> false
            }
        }

        fun code(bytes: ByteArray): Byte = bytes[0]
        fun length(bytes: ByteArray) = ByteBuffer.wrap(bytes, INDEX_LENGTH, 4).int
        fun data(bytes: ByteArray): ByteArray {
            val length = length(bytes)
            return bytes.copyOfRange(INDEX_DATA, length + INDEX_DATA)
        }
    }
}