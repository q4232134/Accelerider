package com.jiaozhu.accelerider.panel

import android.content.Context
import android.content.res.Configuration
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.SwitchPreference
import com.jiaozhu.accelerider.support.Preferences


abstract class BaseSettingsActivity : PreferenceActivity() {
    override fun onIsMultiPane(): Boolean = isXLargeTablet(this)

    companion object {
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            if (preference is SwitchPreference) {
                preference.isChecked = value as Boolean
                return@OnPreferenceChangeListener true
            }

            val stringValue = value.toString()
            if (preference is ListPreference) {
                val index = preference.findIndexOfValue(stringValue)
                preference.summary = if (index >= 0) preference.entries[index] else null
            } else {
                preference.summary = stringValue
            }
            true
        }

        fun bindPreferenceSummaryToValue(preference: Preference) {
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener
            if (preference is SwitchPreference) {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                        Preferences.prefs.getBoolean(preference.key, true))
            } else {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                        Preferences.prefs.getString(preference.key, ""))
            }
        }

        fun isXLargeTablet(context: Context): Boolean =
                context.resources.configuration.screenLayout and
                        Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
    }
}
