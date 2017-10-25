package com.jiaozhu.accelerider.panel

import android.graphics.BitmapFactory
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import android.view.View
import com.jiaozhu.accelerider.R
import com.jiaozhu.accelerider.support.Preferences
import com.jiaozhu.accelerider.support.Tools
import toast

/**
 * Created by jiaozhu on 2017/9/6.
 */
class SettingActivity : BaseSettingsActivity() {
    /**
     * 构建设定标题
     */
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }


    fun click(view: View) {
        showSaveDialog(view.tag.toString())

    }

    private fun showSaveDialog(tag: String) {
        val build = AlertDialog.Builder(this)
        build.setTitle("保存二维码")
        build.setMessage("是否保存${tag}二维码？")
        build.setNegativeButton("取消", null)
        build.setPositiveButton("保存") { _, _ ->
            when (tag) {
                "微信" -> {
                    val temp = BitmapFactory.decodeResource(resources, R.drawable.wx)
                    temp?.let {
                        Tools.saveBitmap(it, Preferences.DownloadPath, "微信二维码.png")
                        toast("微信二维码已保存到默认下载目录")
                    }
                }
                "支付宝" -> {
                    val temp = BitmapFactory.decodeResource(resources, R.drawable.zfb)
                    temp?.let {
                        Tools.saveBitmap(it, Preferences.DownloadPath, "支付宝二维码.jpg")
                        toast("支付宝二维码已保存到默认下载目录")
                    }
                }
            }
        }
        build.create().show()
    }

    /**
     * 设定fragment
     */
    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            //设定内容
            addPreferencesFromResource(R.xml.setting_preference)
            //绑定Preference
            preferenceManager.sharedPreferencesName = Preferences.SHAREDPREFERENCES_NAME
            setHasOptionsMenu(true)
            //绑定值
            bindPreferenceSummaryToValue(findPreference(Preferences.SETTING_ADDRESS))
            bindPreferenceSummaryToValue(findPreference(Preferences.SETTING_WiTH_NET))
            bindPreferenceSummaryToValue(findPreference(Preferences.SETTING_AUTO_LOGIN))
        }

    }


    /**
     * 帮助fragment
     */
    class HelpPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            //设定内容
            addPreferencesFromResource(R.xml.help_preference)
            //绑定Preference
            preferenceManager.sharedPreferencesName = Preferences.SHAREDPREFERENCES_NAME
            setHasOptionsMenu(true)
        }
    }


    /**
     * 帮助fragment
     */
    class AboutPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            //设定内容
            addPreferencesFromResource(R.xml.about_preference)
            //绑定Preference
            preferenceManager.sharedPreferencesName = Preferences.SHAREDPREFERENCES_NAME
            setHasOptionsMenu(true)
        }
    }

    /**
     * 固定结构
     */
    override fun isValidFragment(fragmentName: String): Boolean =
            PreferenceFragment::class.java.name == fragmentName ||
                    GeneralPreferenceFragment::class.java.name == fragmentName ||
                    HelpPreferenceFragment::class.java.name == fragmentName ||
                    AboutPreferenceFragment::class.java.name == fragmentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
    }

    private fun setupActionBar() {
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}