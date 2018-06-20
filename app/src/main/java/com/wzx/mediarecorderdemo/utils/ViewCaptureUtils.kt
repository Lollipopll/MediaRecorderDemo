package com.wzx.mediarecorderdemo.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.webkit.WebView


/**
 * View截图
 */
object ViewCaptureUtils {

    /**
     * WebView截图
     */
    fun captureWebViewLollipop(webView: WebView): Bitmap {
        val scale = webView.scale
        val width = webView.width
        val height = (webView.contentHeight * scale + 0.5).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        webView.draw(canvas)
        return bitmap
    }

    /**
     * 将view转换为bitmap
     *
     * @param v
     * @return
     */
    fun loadBitmapFromView(v: View?): Bitmap? {
        if (v == null) {
            return null
        }
        v!!.setDrawingCacheEnabled(true)
        var screenshot: Bitmap?

        v!!.measure(View.MeasureSpec.makeMeasureSpec(v!!.getWidth(),
                View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(
                v!!.getHeight(), View.MeasureSpec.EXACTLY))
        v!!.layout(0, 0, v!!.getMeasuredWidth(), v!!.getMeasuredHeight())
        v!!.buildDrawingCache()
        screenshot = v!!.getDrawingCache()
        if (screenshot == null) {
            v!!.setDrawingCacheEnabled(true)

            screenshot = Bitmap.createBitmap(v!!.getWidth(), v!!.getHeight(),
                    Bitmap.Config.ARGB_4444)
            val c = Canvas(screenshot!!)
            c.translate((-v!!.getScrollX()).toFloat(), (-v!!.getScrollY()).toFloat())
            v!!.draw(c)
            return screenshot
        }
        return screenshot
    }

}