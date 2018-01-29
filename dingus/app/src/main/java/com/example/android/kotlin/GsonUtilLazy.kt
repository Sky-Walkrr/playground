package com.example.android.kotlin

import com.google.gson.Gson

/**
 * 懒汉式单例，调用时才会初始化
 * Created by gchfeng on 2018/1/29.
 * E-mail:gchfeng.me@gmail.com
 */
class GsonUtilLazy private constructor() {
    companion object {
        val instance by lazy { Gson() }
    }
}