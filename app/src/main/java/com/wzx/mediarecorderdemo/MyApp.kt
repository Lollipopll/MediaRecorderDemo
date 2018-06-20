package com.wzx.mediarecorderdemo

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger


class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initLog()
    }

    private fun initLog() {

        Logger.addLogAdapter(AndroidLogAdapter())
    }

}