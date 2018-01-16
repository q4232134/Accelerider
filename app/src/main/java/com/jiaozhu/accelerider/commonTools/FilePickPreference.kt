package com.jiaozhu.accelerider.commonTools

import android.content.Context
import android.os.Environment
import android.preference.Preference
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jiaozhu.accelerider.R
import kotlinx.android.synthetic.main.item_preference_file.view.*
import java.io.FileFilter


/**
 * Created by 教主 on 2018/1/15.
 */

class FilePickPreference(context: Context, attributeSet: AttributeSet) : Preference(context, attributeSet) {
    private lateinit var view: View
    private var defaultValue: String? = null

    init {
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.FilePickPreference, 0, 0)
        defaultValue = a.getString(R.styleable.FilePickPreference_defaultPath)
        a.recycle()
    }

    override fun onCreateView(parent: ViewGroup): View {
        super.onCreateView(parent)
        view = LayoutInflater.from(context).inflate(R.layout.item_preference_file, parent, false)
        return view
    }


    override fun onBindView(view: View) {
        super.onBindView(view)
        fresh()
        view.mLayout.setOnClickListener {
            showPickDialog()
        }
    }

    private fun fresh() {
        view.mTitle.text = title
        //TODO 获取defalut
        view.mContent.text = getPersistedString(defaultValue)
    }

    private fun showPickDialog() {
        val rootLength = Environment.getExternalStorageDirectory().path.length
        FileMultiPicker.showDialog(this.context, "选择目录", Environment.getExternalStorageDirectory(), FileFilter { it.isDirectory }, FileMultiPicker.OnSelectFinishListener {
            it.getOrNull(0)?.path?.let {
                callChangeListener(it.substring(rootLength))
            }
        }, true)
    }

    override fun callChangeListener(newValue: Any): Boolean {
        (newValue as? String)?.let {
            editor.putString(key, it).commit()
        }
        return super.callChangeListener(newValue)
    }

}
