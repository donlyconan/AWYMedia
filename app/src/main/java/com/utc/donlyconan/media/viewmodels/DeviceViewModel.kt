package com.utc.donlyconan.media.viewmodels

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.utc.donlyconan.media.app.localinteraction.Client

class DeviceViewModel() : ViewModel() {
    val devicesMdl = MutableLiveData<List<Client>>()
    val qrCodeMdl = MutableLiveData<Bitmap>()
    val ipAddress: MutableLiveData<String> = MutableLiveData()

    fun submit(clients: List<Client>) {
        devicesMdl.postValue(clients)
    }

    fun generateQRCode(content: String) {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix: BitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 256, 256)
        val width: Int = bitMatrix.width
        val height: Int = bitMatrix.height
        val qrCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                qrCodeBitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        qrCodeMdl.postValue( qrCodeBitmap)
    }

}