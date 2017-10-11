package com.jiaozhu.accelerider.panel

import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.view.MenuItem
import com.jiaozhu.accelerider.R
import com.jiaozhu.accelerider.support.Preferences

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

    /**
     * 构建fragment与数据绑定
     */
    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            //设定内容
            addPreferencesFromResource(R.xml.preference)
            //绑定Preference
            preferenceManager.sharedPreferencesName = Preferences.SHAREDPREFERENCES_NAME
            setHasOptionsMenu(true)
            //绑定值
            bindPreferenceSummaryToValue(findPreference(Preferences.SETTING_ADDRESS))
        }

    }

    /**
     * 固定结构
     */
    override fun isValidFragment(fragmentName: String): Boolean =
            PreferenceFragment::class.java.name == fragmentName ||
                    GeneralPreferenceFragment::class.java.name == fragmentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
    }

    private fun setupActionBar() {
        val actionBar = actionBar
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