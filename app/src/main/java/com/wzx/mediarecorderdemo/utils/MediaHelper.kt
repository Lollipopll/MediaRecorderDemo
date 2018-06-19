package com.wzx.mediarecorderdemo.utils

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Build
import android.os.SystemClock
import android.support.annotation.RequiresApi

object MediaHelper {
    /**使用object声明的类，其中的方法为静态方法，或者使用companion object{}代码块声明静态方法 **/

    /**
     * 设置截图
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getScreenShot(mMediaProjection: MediaProjection, width: Int, height: Int, dpi: Int): Bitmap? {
        var imageReader: ImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)
        mMediaProjection.createVirtualDisplay("ScreenShot", width, height, dpi
                , DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface, null, null)

        SystemClock.sleep(1000)
        var imageName: String = System.currentTimeMillis().toString() + ".png"

        /**如果image获取的为null直接return null**/
        var image: Image = imageReader.acquireNextImage() ?: return null

        val width = image.width
        val height = image.height

        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        var bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()
        return bitmap
    }
}