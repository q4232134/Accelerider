package com.jiaozhu.accelerider.panel

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.widget.Toast
import com.jiaozhu.accelerider.R
import com.jiaozhu.accelerider.panel.BaseActivity
import com.jiaozhu.accelerider.support.Tools
import com.kuopu.recordmeter.panel.fragment.CommListViewFragment
import toast

class MainActivity : BaseActivity() {
    private lateinit var fragment: CommListViewFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    /**
     * 开始同步
     */
    private fun syn() {
        if (!Tools.isConnect(this)) {
            toast("无可用网络！")
            return
        }
        if (!Tools.isWifi(this)) {
            showNetWarnDialog()
        } else {
        }
    }

    /**
     * 弹出蜂窝网络警告对话框
     */
    private fun showNetWarnDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("同步数据")
        builder.setMessage("您现在处于非WIFI环境，是否继续进行同步？")
        builder.setPositiveButton("继续") { _, _ -> }
        builder.setNegativeButton("取消", null)
        builder.create().show()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    private var backPressTime: Long = 0
    override fun onBackPressed() {
        if (System.currentTimeMillis() - backPressTime > 2000) {
            backPressTime = System.currentTimeMillis()
            Toast.makeText(this, "双击返回键退出应用", Toast.LENGTH_SHORT).show()
        } else {
            finish()
            System.exit(0)
        }
    }
}


