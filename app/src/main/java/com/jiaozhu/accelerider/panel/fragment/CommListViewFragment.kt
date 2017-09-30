package com.jiaozhu.accelerider.panel.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import com.jiaozhu.accelerider.R
import com.jiaozhu.accelerider.commonTools.BackgroundExecutor
import com.jiaozhu.accelerider.panel.fragment.BaseFragment
import kotlin.properties.Delegates


/**
 * Created by jiaozhu on 15/12/25.
 */
class CommListViewFragment : BaseFragment() {
    var adapter: BaseAdapter? = null
    var list by Delegates.notNull<MutableList<*>>()
    var mListView: ListView? = null
    private var onRefresh: OnRefreshListener? = null

    var onItemClickListener by Delegates.observable(null as AdapterView.OnItemClickListener?) { _, _, newValue ->
        mListView?.onItemClickListener = newValue
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_list, container, false)
        val listView = view.findViewById<ListView>(R.id.list)
        listView.adapter = adapter
        listView.onItemClickListener = onItemClickListener
        onRefresh()
        return view
    }

    override fun onRefresh() {
        if (onRefresh == null) return
        super.onRefresh()
        setProgress(true)
        BackgroundExecutor.getInstance().runInBackground(object : BackgroundExecutor.Task {
            override fun runnable() {
                list.clear()
                onRefresh?.let { list.addAll(it.onRefresh()) }
            }

            override fun onBackgroundFinished() {
                adapter?.notifyDataSetChanged()
                setProgress(false)
            }
        })
    }

    interface OnRefreshListener {
        fun onRefresh(): Collection<Nothing>
    }

    companion object {
        fun newInstance(list: MutableList<*>, adapter: BaseAdapter,
                        onRefresh: OnRefreshListener?): CommListViewFragment {
            val fragment = CommListViewFragment()
            fragment.list = list
            fragment.adapter = adapter
            fragment.onRefresh = onRefresh
            return fragment
        }
    }
}
