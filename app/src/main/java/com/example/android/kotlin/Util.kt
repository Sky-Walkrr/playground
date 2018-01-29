package com.example.android.kotlin

import android.content.Context
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast
import java.io.Closeable
import java.io.IOException

/**
 * Created by gchfeng on 2018/1/29.
 * E-mail:gchfeng.me@gmail.com
 */
class Util {

    companion object {
        fun showToast(context: Context, msg: String): Unit {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        fun showSnackBar(parentView: View, msg: CharSequence, duration: Int,
                         actionStr: CharSequence?, action: ((view: View) -> Unit)?) {
            val snackbar = Snackbar.make(parentView, msg, duration)
            if (!actionStr.isNullOrEmpty() && action != null) snackbar.setAction(actionStr, action)
            snackbar.show()
        }

        fun close(closeable: Closeable?) {
            if (closeable == null) return
            try {
                closeable.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


}