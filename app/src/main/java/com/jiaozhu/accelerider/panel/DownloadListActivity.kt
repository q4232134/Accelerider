package com.jiaozhu.accelerider.panel

import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.alibaba.fastjson.JSONObject
import com.jiaozhu.accelerider.R
import com.jiaozhu.accelerider.commonTools.HttpResponse
import com.jiaozhu.accelerider.commonTools.SelectorRecyclerAdapter
import com.jiaozhu.accelerider.commonTools.SelectorRecyclerAdapter.MODE_MULTI
import com.jiaozhu.accelerider.model.Task
import com.jiaozhu.accelerider.support.HttpClient
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_tasks_manager.*
import kotlinx.android.synthetic.main.item_tasks_manager.view.*
import kotlinx.android.synthetic.main.view_toolbar.*
import toast
import zlc.season.rxdownload3.RxDownload
import zlc.season.rxdownload3.core.*
import zlc.season.rxdownload3.helper.dispose
import zlc.season.rxdownload3.helper.loge


class DownloadListActivity : BaseActivity() {
    lateinit var adapter: SelectorRecyclerAdapter<ViewHolder>
    val list = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks_manager)
        setSupportActionBar(mToolbar)

        adapter = Adapter(list)
        mRecycler.layoutManager = LinearLayoutManager(this)
        mRecycler.adapter = adapter.apply {
            selectorMode = MODE_MULTI
            setActionView(mToolbar, object : SelectorRecyclerAdapter.ActionItemClickedListener {
                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                    val inflater = mode.menuInflater
                    inflater.inflate(R.menu.menu_action, menu)
                    return true
                }

                override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                    val id = item.itemId
                    when (id) {
                        R.id.action_delete -> {
                            println(adapter.selectList)
                            return true
                        }
                        R.id.action_fresh -> {
                            freshUrls(*adapter.selectList.map { list[it] }.toTypedArray())
                            adapter.cancelSelectorMode()
                            return true
                        }
                    }
                    return false
                }
            })
        }
        loadData()
    }


    private fun loadData() {
        RxDownload.getAllMission().observeOn(mainThread()).subscribe {
            list.clear()
            list.addAll(it as Collection<Task>)
            println(list)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_task, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.task_clear -> {
                RxDownload.deleteAll(true).subscribe { loadData() }
            }
            R.id.task_pause -> RxDownload.stopAll().subscribe()
            R.id.task_start -> RxDownload.startAll().subscribe()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 适配器
     */
    inner class Adapter(val list: List<Task>) : SelectorRecyclerAdapter<ViewHolder>() {
        override fun onCreateHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent!!.context)
            return ViewHolder(inflater.inflate(R.layout.item_tasks_manager, parent, false))
        }

        override fun onBindView(holder: ViewHolder, position: Int, isSelected: Boolean) {
            if (isSelected) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.view.mLayout.setBackgroundColor(this@DownloadListActivity.getColor(R.color.main_item_selected_bg))
                } else {
                    holder.view.mLayout.setBackgroundColor(this@DownloadListActivity.resources.getColor(R.color.main_item_selected_bg))
                }
            } else {
                holder.view.mLayout.background = null
            }
            holder.setData(list[position])
        }

        override fun getItemCount(): Int = list.size

        override fun onViewAttachedToWindow(holder: ViewHolder) {
            super.onViewAttachedToWindow(holder)
            holder.onAttach()
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder) {
            super.onViewDetachedFromWindow(holder)
            holder.onDetach()
        }
    }


    /**
     * 批量刷新下载链接
     */
    fun freshUrls(vararg tasks: Task) {
        spinnerDialog.show()
        spinnerDialog.setTitle("正在获取下载地址")
        HttpClient.getDownloadUrl(tasks.map { it.model ?: return }, object : HttpResponse() {

            override fun onSuccess(statusCode: Int, result: JSONObject) {
                val s = result.getJSONObject("links").entries.map {
                    val name = it.key
                    val url = (it.value as List<String>)[0]
                    tasks.find { model -> model.model?.server_filename == it.key }?.apply { this.url = url }.let { RxDownload.update(it as Mission) }
                }
            }

            override fun onFailure(statusCode: Int, msg: String?, error: Throwable?) {
                toast(msg)
            }

            override fun onFinish() {
                spinnerDialog.dismiss()
            }
        })
    }

    /**
     * item控件
     */
    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private var task: Task? = null
        private var disposable: Disposable? = null
        private var currentStatus: Status? = null

        init {
            view.mBtn.setOnClickListener {
                when (currentStatus) {
                    is Normal -> start()
                    is Suspend -> start()
                    is Failed -> start()
                    is Downloading -> stop()
                }
            }
        }

        private fun start() {
            RxDownload.start(task!!.url).subscribe({}, { println(it) })
        }

        /**
         * 重新获取url
         */
        private fun reGetUrl(vararg tasks: Task) {
            freshUrls(*tasks)
        }

        private fun stop() {
            RxDownload.stop(task!!.url).subscribe()
        }

        fun setData(mission: Task) {
            this.task = mission
        }

        fun onAttach() {
            disposable = RxDownload.create(task!!.url)
                    .observeOn(mainThread())
                    .subscribe {
                        if (currentStatus is Failed) {
                            loge("Failed", (currentStatus as Failed).throwable)
                        }
                        currentStatus = it
                        view.task_name_tv.text = task?.saveName
                        if (it is Succeed) {
                            it.downloadSize = it.totalSize
                        }
                        view.mSpeed.text = it.percent()
                        setProgress(it)
                        setActionText(it)
                    }
        }

        fun onDetach() {
            dispose(disposable)
        }

        private fun setProgress(it: Status) {
            view.mProgress.max = it.totalSize.toInt()
            view.mProgress.progress = it.downloadSize.toInt()

            view.task_status_tv.text = it.formatString()
        }

        private fun setActionText(status: Status) {
            val text = when (status) {
                is Normal -> "开始"
                is Suspend -> "已暂停"
                is Waiting -> "等待中"
                is Downloading -> "暂停"
                is Failed -> {
                    view.task_status_tv.text = status.throwable.localizedMessage
                    view.mSpeed.text = status.formatDownloadSize()
                    "失败"
                }
                is Succeed -> "完成"
                is Deleted -> "已删除"
                else -> ""
            }
            view.mBtn.text = text
        }
    }
}