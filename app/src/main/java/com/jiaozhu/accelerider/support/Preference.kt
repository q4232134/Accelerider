package com.jiaozhu.accelerider.support
import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by jiaozhu on 2017/6/27.
 */
class Preference<T>(val name: String, private val default: T)
    : ReadWriteProperty<Any?, T> {

    companion object {
        private var context: Context = UserInfo.context
        /**
         * 配置文件名称
         */
        val SHAREDPREFERENCES_NAME = "Setting"

        val prefs: SharedPreferences = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE)

        var userName by Preference("UserName", "")
        var passWord by Preference("PassWord", "")
        var token by Preference("token", "")
        var uk by Preference("uk", "")
        var name by Preference("name", "")
        var userList by Preference("userList", "")
        var iSavePassWord by Preference("ISavePassWord", false)
    }


    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return findPreference(name, default)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putPreference(name, value)
    }

    private fun <U> putPreference(name: String, value: U) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("This type can't be saved into Preferences ")
        }.apply()
    }

    @SuppressWarnings("unchecked")
    private fun <T> findPreference(name: String, default: T): T = with(prefs) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw IllegalArgumentException("This type can't be saved into Preferences")
        }
        res as T
    }
}
