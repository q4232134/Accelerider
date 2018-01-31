package com.jiaozhu.accelerider.model

import zlc.season.rxdownload3.core.Mission

/**
 * Created by jiaozhu on 2017/10/10.
 */
data class FileModel(
        var fs_id: Long = 0,//file id
        var server_ctime: Long = 0,//create time in server
        var server_mtime: Long = 0,//modify time in server
        var local_ctime: Long = 0,//create time in local
        var local_mtime: Long = 0,//modify time in local
        var size: Long = 0,//
        var isdir: Int = 1,//
        var path: String = "/",//
        var server_filename: String = "",//
        var empty: Int = 0,//
        var md5: String? = null
)

class Task(val model: FileModel, mission: Mission) : Mission(mission) {
    override fun toString(): String =
            "Task(model=$model,saveName=$saveName,savePath=$savePath,rangeFlag=$rangeFlag,tag=$tag,url=$url)\n"
}