package com.wzx.mediarecorderdemo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.View
import android.view.View.OnClickListener
import com.wzx.mediarecorderdemo.utils.MediaHelper
import com.wzx.mediarecorderdemo.utils.MeidaRecoderThread
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

class MainActivity : AppCompatActivity(), OnClickListener, EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {

    val REQUEST_SCREEN_SHOT_CODE: Int = 1
    val REQUEST_SCREEN_REC_CODE: Int = 2

    lateinit var mMediaProjectionManager: MediaProjectionManager
    lateinit var mMediaProjection: MediaProjection

    var width: Int = 0
    var height: Int = 0
    var dpi: Int = 0

    lateinit var rec: MeidaRecoderThread

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        EasyPermissions.requestPermissions(
                this,
                "读写内存权限",
                123,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)

        val metric = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metric)
        width = metric.widthPixels
        height = metric.heightPixels
        dpi = metric.densityDpi

        mMediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        initListener()
    }

    private fun initListener() {
        btn_start_rec.setOnClickListener(this)
        btn_stop_rec.setOnClickListener(this)
        btn_screenshot.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(v: View?) {

        when (v!!.id) {
            R.id.btn_start_rec -> startRec()

            R.id.btn_stop_rec -> stopRec()

            R.id.btn_screenshot -> startScreenShot()
        }

    }



    /**
     * 截图
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startScreenShot() {
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_SCREEN_SHOT_CODE)
    }

    /**
     * 录屏
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startRec() {
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_SCREEN_REC_CODE)
    }

    /**
     * 停止录屏
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun stopRec() {
        rec.stopRecorder()
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {

            requestCode == REQUEST_SCREEN_SHOT_CODE && resultCode == Activity.RESULT_OK -> {
                mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data)
                var bitmap = MediaHelper.getScreenShot(mMediaProjection, width, height, dpi)
                /**使用let函数判断在不为null的情况下执行**/
                bitmap?.let { iv_show.setImageBitmap(bitmap) }
            }

            requestCode == REQUEST_SCREEN_REC_CODE && resultCode == Activity.RESULT_OK -> {
                mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data)

                val dir = File(getExternalStorageDirectory().absolutePath + "/MediaRecorderDemo")
                if (!dir.exists()) {
                    dir.mkdir()
                }

                val file = File(getExternalStorageDirectory().absolutePath + "/MediaRecorderDemo",
                        "record-" + width + "x" + height + "-" + System.currentTimeMillis() + ".mp4")
                var filePath = file.getAbsolutePath()
                width = 720
                height = 1280
                dpi = 1
                rec = MeidaRecoderThread(width, height, dpi, mMediaProjection, filePath)
                rec.start()
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onRationaleDenied(requestCode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRationaleAccepted(requestCode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }
}
