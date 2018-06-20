package com.wzx.mediarecorderdemo.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


object PhotoUtils {

    /***********************************保存图片相关******************************************/


    fun isHaveSDCard(): Boolean {
        val SDState = android.os.Environment.getExternalStorageState()
        return if (SDState == android.os.Environment.MEDIA_MOUNTED) {
            true
        } else false
    }

    /**
     * 保存图片到本地
     */
    fun saveBitmap(bm: Bitmap, path: String, picName: String): Boolean {
        isHaveSDCard()
        val file = File(path)
        if (!file.isDirectory) {
            file.delete()
            file.mkdirs()
        }
        if (!file.exists()) {
            file.mkdirs()
        }
        val isOk = writeBitmap(path, picName, bm)
        return if (isOk) {
            true
        } else false
    }

    /**
     * 保存图片
     *
     * @param path
     * @param name
     * @param bitmap
     */
    fun writeBitmap(path: String, name: String?, bitmap: Bitmap): Boolean {
        val file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }

        val _file = File(path + name!!)
        if (_file.exists()) {
            _file.delete()
        }
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(_file)
            if (name != null && "" != name) {
                val index = name.lastIndexOf(".")
                if (index != -1 && index + 1 < name.length) {
                    val extension = name.substring(index + 1).toLowerCase()
                    if ("png" == extension) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    } else if ("jpg" == extension || "jpeg" == extension) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos)
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        return true
    }

    /***********************************uri获取图片相关******************************************/
    /**
     * 从图片uri获取path
     *
     * @param context 上下文
     * @param uri     图片uri
     */
    fun getPathFromUri(context: Context, uri: Uri): String? {
        var outPath: String? = ""
        val cursor = context.getContentResolver()
                .query(uri, null, null, null, null)
        if (cursor == null) {
            // miui 2.3 有可能为null
            return uri.getPath()
        } else {
            if (uri.toString().contains("content://com.android.providers.media.documents/document/image")) { // htc 某些手机
                // 获取图片地址
                var _id: String? = null
                val uridecode = Uri.decode(uri.toString())
                val id_index = uridecode.lastIndexOf(":")
                _id = uridecode.substring(id_index + 1)
                val mcursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, " _id = " + _id!!, null, null)
                mcursor.moveToFirst()
                val column_index = mcursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                outPath = mcursor.getString(column_index)
                if (!mcursor.isClosed()) {
                    mcursor.close()
                }
                if (!cursor!!.isClosed()) {
                    cursor!!.close()
                }
                return outPath
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (DocumentsContract.isDocumentUri(context, uri)) {
                        val docId = DocumentsContract.getDocumentId(uri)
                        if ("com.android.providers.media.documents" == uri.getAuthority()) {
                            //Log.d(TAG, uri.toString());
                            val id = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                            val selection = MediaStore.Images.Media._ID + "=" + id
                            outPath = getImagePath(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
                        } else if ("com.android.providers.downloads.documents" == uri.getAuthority()) {
                            //Log.d(TAG, uri.toString());
                            val contentUri = ContentUris.withAppendedId(
                                    Uri.parse("content://downloads/public_downloads"),
                                    java.lang.Long.valueOf(docId))
                            outPath = getImagePath(context, contentUri, null)
                        }
                        return outPath
                    }
                }
                if ("content".equals(uri.getScheme(), ignoreCase = true)) {
                    val auth = uri.getAuthority()
                    if (auth == "media") {
                        outPath = getImagePath(context, uri, null)
                    } else if (auth == "com.example.administrator.aitang.fileprovider") {
                        //参看file_paths_public配置
                        outPath = Environment.getExternalStorageDirectory().absolutePath + "/Pictures/" + uri.getLastPathSegment()
                    }
                    return outPath
                }
            }
            return outPath
        }

    }


    /**
     * 从uri中取查询path路径
     *
     * @param context   上下文
     * @param uri
     * @param selection
     */
    fun getImagePath(context: Context, uri: Uri, selection: String?): String? {
        var path: String? = null
        val cursor = context.getContentResolver().query(uri, null, selection, null, null)
        if (cursor != null) {
            if (cursor!!.moveToFirst()) {
                path = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor!!.close()
        }
        return path
    }
}