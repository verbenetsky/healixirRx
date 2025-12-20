package com.example.pharmacystore.common.utlis

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

fun generateBarcodeBitmapFrom13Digits(
    code: String,
    width: Int = 800,
    height: Int = 300
): Bitmap {

    val bitMatrix: BitMatrix = MultiFormatWriter().encode(
        code,
        BarcodeFormat.EAN_13,
        width,
        height
    )

    val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
        }
    }
    return bitmap
}
