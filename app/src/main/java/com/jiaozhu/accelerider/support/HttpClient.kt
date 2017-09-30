package com.jiaozhu.accelerider.support
import Preference
import com.jiaozhu.accelerider.commonTools.HttpResponse
import com.jiaozhu.accelerider.commonTools.Log
import com.jiaozhu.accelerider.support.Constants
import com.jiaozhu.accelerider.support.Tools
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.RequestParams
import com.loopj.android.http.ResponseHandlerInterface
import logTag


/**
 * Created by apple on 15/11/6.
 */
object HttpClient {
    private val client: AsyncHttpClient = AsyncHttpClient()
    private val token: String get() = Preference.token

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
    }


    /**
     * 登录接口
     * @param userName 用户名
     * @param passWord 密码
     */
    fun login(userName: String, passWord: String, handler: HttpResponse) {
        val params = basicParams
        params.add("security", "MD5")
        params.add("name", userName)
        params.add("password", Tools.md5Encode(passWord))
        params.add("clienttype", Constants.UA)
        post("http://api.usmusic.cn/signup", params, handler)
    }

}