package com.jiaozhu.accelerider.support

import com.alibaba.fastjson.JSON
import com.jiaozhu.accelerider.commonTools.HttpResponse
import com.jiaozhu.accelerider.commonTools.Log
import com.jiaozhu.accelerider.model.FileModel
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.RequestParams
import com.loopj.android.http.ResponseHandlerInterface
import logTag
import java.net.URLEncoder


/**
 * Created by apple on 15/11/6.
 */
object HttpClient {
    private val client: AsyncHttpClient = AsyncHttpClient()
    private val token: String get() = Preferences.token

    init {
        client.setMaxRetriesAndTimeout(0, Constants.CONNECT_TIME_OUT + Constants.RESPONSE_TIME_OUT)
        client.connectTimeout = Constants.CONNECT_TIME_OUT
        client.responseTimeout = Constants.RESPONSE_TIME_OUT
        client.isLoggingEnabled = false
    }

    /**
     * 获取基础Param

     * @return
     */
    private val basicParams: RequestParams
        get() {
            val params = RequestParams()
            return params
        }

    /**
     * 发送post请求

     * @param address 地址
     * *
     * @param params  参数
     * *
     * @param handler 回调
     */

    private fun post(address: String, params: RequestParams, handler: ResponseHandlerInterface) {
        Log.i(logTag, address)
        Log.i(logTag, params.toString())
        client.post(address, params, handler)
//        client.setUserAgent("netdisk;8.2.0;android-android;4.4.4")
    }


    /**
     * 登录接口
     * @param userName 用户名
     * @param passWord 密码
     */
    fun login(userName: String, passWord: String, handler: HttpResponse) {
        val params = basicParams
        params.add("name", userName)
        params.add("password", Tools.md5Encode(passWord))
        params.add("clienttype", Constants.UA)
        post("http://api.usmusic.cn/login?security=md5", params, handler)
    }


    /**
     * 获取当前账号下面的用户列表
     */
    fun getUserList(handler: HttpResponse) {
        val params = basicParams
        post("http://api.usmusic.cn/userlist?token=${Preferences.token}", params, handler)
    }

    /**
     * 根据指定路径获取文件列表
     */
    fun getFileList(path: String, handler: HttpResponse) {
        val params = basicParams
        val url = "http://api.usmusic.cn/filelist?token=${Preferences.token}&uk=${Preferences.uk}&path=${URLEncoder.encode(path, "utf-8")}"
        post(url, params, handler)
    }

    /**
     * 批量获取下载地址
     */
    fun getDownloadUrl(files: List<FileModel>, handler: HttpResponse) {
        val params = basicParams
        params.put("files", JSON.toJSONString(files))
        post("http://api.usmusic.cn/filelinks?token=${Preferences.token}" +
                "&uk=${Preferences.uk}&method=APPID", params, handler)
    }

}