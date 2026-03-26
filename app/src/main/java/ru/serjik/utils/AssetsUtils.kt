package ru.serjik.utils

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException

object AssetsUtils {
    @JvmStatic
    fun readText(path: String, assets: AssetManager): String {
        try {
            return assets.open(path).use { input ->
                StreamUtils.toString(input)
            }
        } catch (e: IOException) {
            SerjikLog.log(e.message ?: "")
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun readBitmap(path: String, assets: AssetManager): Bitmap {
        try {
            return assets.open(path).use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (e: IOException) {
            SerjikLog.log(e.message ?: "")
            throw RuntimeException(e)
        }
    }
}
