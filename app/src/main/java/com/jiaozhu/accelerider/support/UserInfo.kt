package com.jiaozhu.accelerider.support

import android.content.Context
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloadQueueSet

/**
 * Created by jiaozhu on 2017/6/29.
 */

object UserInfo {
    val id get() = Preferences.userName
    var name = ""
    lateinit var context: Context
}
