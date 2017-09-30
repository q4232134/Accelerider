package com.jiaozhu.accelerider.panel.fragment

import android.content.Context
import android.support.v4.app.Fragment


/**
 * Created by apple on 15/11/4.
 */
abstract class BaseFragment : Fragment() {
    var titleStr: String? = null//Fragment标题
    private var mListener: OnFragmentInteractionListener? = null

    /**
     * 数据更新完成时触发此方法
     */
    open fun onRefresh() {

    }

    protected fun setTitle(title: String) {
        activity.title = title
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            mListener = context as OnFragmentInteractionListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }

    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    protected fun setProgress(flag: Boolean) {
        mListener!!.setProgress(flag)
    }

    interface OnFragmentInteractionListener {
        fun setProgress(flag: Boolean)
    }

}
