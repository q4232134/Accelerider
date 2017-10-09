package com.jiaozhu.accelerider.panel

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import com.jiaozhu.accelerider.commonTools.HttpResponse
import com.jiaozhu.accelerider.support.HttpClient
import com.jiaozhu.accelerider.support.Preference
import toast


class LoginActivity : BaseLoginActivity() {
    private var mCode: EditText? = null
    private var canBack = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Preference.userName = "q4232134"
        if (!Preference.token.isEmpty()) toNextActivity()

    }

    /**
     * 非本地登录
     */
    override fun onCommitPressed() {
        val name = mName.text.toString()
        val password = mPassword.text.toString()
        login(name, password)
    }

    /**
     * 登录验证

     * @param name
     * *
     * @param password
     * *
     * @return
     */
    internal override fun login(name: String, password: String): Boolean {
        spinnerDialog.show()
        spinnerDialog.setTitle("正在登陆")
        HttpClient.login(name, password, object : HttpResponse() {
            override fun onSuccess(statusCode: Int, result: String?) {
            }

            override fun onFailure(statusCode: Int, error: Throwable?) {
                error?.printStackTrace()
                toast("$statusCode 与服务器通信失败，请检查网络连接")
            }

            override fun onFinish() {
                spinnerDialog.dismiss()
            }

        })
        return true
    }

    /**
     * 登录验证通过之后的动作
     */
    override fun onLoginSuccess(name: String, password: String) {
        Preference.userName = name
    }

    /**
     * 进入下一个界面
     */
    private fun toNextActivity() {
        val intent = Intent()
        intent.setClass(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        spinnerDialog.dismiss()
        finish()
    }

    override fun onBackPressed() {
        if (canBack) {
            super.onBackPressed()
        }
    }


    companion object {
        /**
         * 是否能够回退
         */
        val PARAM_CAN_BACK = "canBack"
    }
}
