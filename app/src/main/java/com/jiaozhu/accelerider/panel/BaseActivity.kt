package com.jiaozhu.accelerider.panel

import BaseFragment
import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.view_toolbar_comm.*

/**
 * Created by jiaozhu on 15/12/26.
 */
abstract class BaseActivity : AppCompatActivity(), BaseFragment.OnFragmentInteractionListener {
    protected val spinnerDialog: ProgressDialog by lazy {
        val spinnerDialog = ProgressDialog(this)
        spinnerDialog.setCancelable(false)
        spinnerDialog.setTitle("正在同步数据")
        spinnerDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        spinnerDialog
    }

    protected val horizDialog: ProgressDialog by lazy {
        val horizDialog = ProgressDialog(this)
        horizDialog.setCancelable(false)
        horizDialog.setTitle("正在同步任务")
        horizDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        horizDialog
    }

    override fun setProgress(flag: Boolean) {
        if (flag) {
            mProgressBar.visibility = View.VISIBLE
        } else {
            mProgressBar.visibility = View.GONE
        }
    }


}
