package com.jiaozhu.accelerider.support
import android.content.Context

/**
 * Created by jiaozhu on 2017/6/29.
 */

object UserInfo {
    val id get() = Preference.userName
    var name = ""
    lateinit var context: Context
}
