package com.example.android.kotlin

import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.widget.EditText

/**
 * Created by gchfeng on 2018/1/29.
 * E-mail:gchfeng.me@gmail.com
 */
fun EditText.validate(passIcon: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_pass),
                      failedIcon: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_fail),
                      validator: EditText.() -> Boolean): Boolean {
    this.setCompoundDrawablesWithIntrinsicBounds(null, null,
            if (validator(this)) passIcon else failedIcon, null)
    return validator(this)
}