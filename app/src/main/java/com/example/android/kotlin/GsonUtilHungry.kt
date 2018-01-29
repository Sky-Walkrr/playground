package com.example.android.kotlin

import com.google.gson.Gson

/**
 * 饿汉式单例，类加载时就初始化
 * Created by gchfeng on 2018/1/29.
 * E-mail:gchfeng.me@gmail.com
 */
object GsonUtilHungry {
    fun getInstance(): Gson = Gson()
}