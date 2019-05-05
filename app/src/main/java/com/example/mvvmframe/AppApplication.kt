package com.example.mvvmframe

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex

class AppApplication : Application() {
    companion object {
        lateinit var instance: AppApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    /**
     *启用 Dalvik 可执行文件分包
     */
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}