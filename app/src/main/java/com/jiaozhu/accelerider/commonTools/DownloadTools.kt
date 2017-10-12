package com.jiaozhu.accelerider.commonTools

import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloadQueueSet

/**
 * Created by jiaozhu on 2017/10/11.
 */
object DownloadTools {
    private val taskList = arrayListOf<BaseDownloadTask>()
    val downloadListener = object : FileDownloadListener() {
        override fun warn(p0: BaseDownloadTask) {
        }

        override fun completed(p0: BaseDownloadTask) {
        }

        override fun pending(p0: BaseDownloadTask, p1: Int, p2: Int) {
        }

        override fun error(p0: BaseDownloadTask, p1: Throwable) {
        }

        override fun progress(p0: BaseDownloadTask, p1: Int, p2: Int) {
            println("${p0.tag}   $p1/$p2    ${p0.speed}")
        }

        override fun paused(p0: BaseDownloadTask, p1: Int, p2: Int) {
        }
    }
    val queueSet = FileDownloadQueueSet(downloadListener).setWifiRequired(true)

    fun add(vararg tasks: BaseDownloadTask) {
        queueSet.downloadTogether(*tasks).start()
    }

    fun start() {
//        try {
//            queueSet.start()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }
}