package com.wzx.mediarecorderdemo.utils

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.projection.MediaProjection
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.Surface
import java.util.concurrent.atomic.AtomicBoolean
import android.widget.Toast




@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MeidaRecoderThread : Thread {

    private val TAG = "MeidaRecoderThread"

    private val width: Int
    private val height: Int
    private val dpi: Int
    private val filePath: String

    private val mMediaProjection: MediaProjection
    private var mediaCodec: MediaCodec? = null
    private var mediaMuxer: MediaMuxer? = null
    private var surface: Surface? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var bufferInfo = MediaCodec.BufferInfo()
    private var videoTrackIndex: Int = -1


    private var muxerStarted = false
    private val mQuit = AtomicBoolean(false)

    constructor(width: Int, height: Int, dpi: Int, mMediaProjection: MediaProjection, filePath: String) {
        this.width = width
        this.height = height
        this.dpi = dpi
        this.mMediaProjection = mMediaProjection
        this.filePath = filePath
    }

    override fun run() {
        super.run()

        try {
            prepareEncoder()

            mediaMuxer = MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            virtualDisplay = mMediaProjection.createVirtualDisplay(TAG + "-display",
                    width, height, dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    surface, null, null)

            recordVirtualDisplay()

        } finally {
            release()
        }
    }


    /**
     * 准备
     */
    private fun prepareEncoder() {
        val format = MediaFormat.createVideoFormat("video/avc", width, height)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 6000000)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)

        mediaCodec = MediaCodec.createEncoderByType("video/avc")
        mediaCodec!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        surface = mediaCodec!!.createInputSurface()
        mediaCodec!!.start()
    }


    private fun recordVirtualDisplay() {
        while (!mQuit.get()) {
            val index = mediaCodec!!.dequeueOutputBuffer(bufferInfo, 10000)
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                resetOutputFormat()
            } else if (index >= 0) {
                encodeToVideoTrack(index)
                mediaCodec!!.releaseOutputBuffer(index, false)
            }
        }
    }


    private fun resetOutputFormat() {
        val newFormat = mediaCodec!!.outputFormat
        videoTrackIndex = mediaMuxer!!.addTrack(newFormat)
        mediaMuxer!!.start()
        muxerStarted = true

    }

    private fun encodeToVideoTrack(index: Int) {
        var encodedData = mediaCodec!!.getOutputBuffer(index)

        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG !== 0) {
            bufferInfo.size = 0
        }
        if (bufferInfo.size === 0) {
            encodedData = null
        }
        if (encodedData != null) {
            encodedData.position(bufferInfo.offset)
            encodedData.limit(bufferInfo.offset + bufferInfo.size)
            mediaMuxer!!.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
        }
    }

    fun stopRecorder() {
        mQuit.set(true)
    }

    /**
     * 释放资源
     */
    private fun release() {

        if (mediaCodec != null) {
            mediaCodec!!.stop()
            mediaCodec!!.release()
            mediaCodec = null
        }

        if (virtualDisplay != null) {
            virtualDisplay!!.release()
        }

        if (mMediaProjection != null) {
            mMediaProjection.stop()
        }

        if (mediaMuxer != null) {
            mediaMuxer!!.release()
            mediaMuxer = null
        }
    }
}