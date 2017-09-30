package com.kuopu.recordmeter.panel.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jiaozhu.accelerider.R
import com.jiaozhu.accelerider.commonTools.BackgroundExecutor
import com.jiaozhu.accelerider.commonTools.DividerLine
import com.jiaozhu.accelerider.panel.fragment.BaseFragment


/**
 * Created by jiaozhu on 15/12/28.
 */
class CommRecycleFragment : BaseFragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var list: ArrayList<*>
    lateinit var layoutManager: LinearLayoutManager
    var adapter: RecyclerView.Adapter<*>? = null
    private var onRefresh: OnRefreshListener? = null
    var onLayoutChangeListener: View.OnLayoutChangeListener? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_comm_recycle, container, false)
        recyclerView = view as RecyclerView
        layoutManager = LinearLayoutManager(view.context)
        recyclerView.layoutManager = layoutManager
        val dividerLine = DividerLine(DividerLine.VERTICAL)
        dividerLine.setSize(1)
        dividerLine.setColor(0xFFDDDDDD.toInt())
        recyclerView.addItemDecoration(dividerLine)
        adapter?.let { recyclerView.adapter = it }
        onLayoutChangeListener?.let { recyclerView.addOnLayoutChangeListener(it) }
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
                onRefresh?.let { list.addAll(it.onRefresh() as Collection<Nothing>) }
            }

            override fun onBackgroundFinished() {
                adapter?.notifyDataSetChanged()
                setProgress(false)
            }
        })
    }

    interface OnRefreshListener {
        fun onRefresh(): Collection<*>
    }

    companion object {
        fun newInstance(list: ArrayList<*>, adapter: RecyclerView.Adapter<*>,
                        onRefresh: OnRefreshListener?): CommRecycleFragment {
            val fragment = CommRecycleFragment()
            fragment.list = list
            fragment.adapter = adapter
            fragment.onRefresh = onRefresh
            return fragment
        }
    }
}
