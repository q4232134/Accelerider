package com.jiaozhu.accelerider.panel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.jiaozhu.accelerider.R
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_tasks_manager.*
import kotlinx.android.synthetic.main.item_tasks_manager.view.*
import kotlinx.android.synthetic.main.view_toolbar.*
import zlc.season.rxdownload3.RxDownload
import zlc.season.rxdownload3.core.*
import zlc.season.rxdownload3.helper.dispose
import zlc.season.rxdownload3.helper.loge


class DownloadListActivity : AppCompatActivity() {
    lateinit var adapter: Adapter
    val list = mutableListOf<Mission>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks_manager)
        setSupportActionBar(mToolbar)

        adapter = Adapter(list)
        mRecycler.layoutManager = LinearLayoutManager(this)
        mRecycler.adapter = adapter
        loadData()
    }


    private fun loadData() {
        RxDownload.getAllMission().observeOn(mainThread()).subscribe {
            list.clear()
            list.addAll(it)
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
                RxDownload.deleteAll(true).subscribe()
                RxDownload.clearAll().subscribe()
                loadData()
            }
            R.id.task_pause -> RxDownload.stopAll().subscribe()
            R.id.task_start -> RxDownload.startAll().subscribe()
        }
        return super.onOptionsItemSelected(item)
    }

    class Adapter(val list: List<Mission>) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent!!.context)
            return ViewHolder(inflater.inflate(R.layout.item_tasks_manager, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private var mission: Mission? = null
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
            RxDownload.start(mission!!.url).subscribe({}, { println(it) })
        }

        private fun stop() {
            RxDownload.stop(mission!!.url).subscribe()
        }

        fun setData(mission: Mission) {
            this.mission = mission
        }

        fun onAttach() {
            disposable = RxDownload.create(mission!!.url)
                    .observeOn(mainThread())
                    .subscribe {
                        if (currentStatus is Failed) {
                            loge("Failed", (currentStatus as Failed).throwable)
                        }
                        currentStatus = it
                        view.task_name_tv.text = mission?.saveName
                        if (it is Succeed) {
                            it.downloadSize = it.totalSize
                        }
                        view.task_status_tv.text = it.percent()
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

            view.mSpeed.text = it.formatString()
        }

        private fun setActionText(status: Status) {
            val text = when (status) {
                is Normal -> "开始"
                is Suspend -> "已暂停"
                is Waiting -> "等待中"
                is Downloading -> "暂停"
                is Failed -> "失败"
                is Succeed -> "完成"
                is Deleted -> "已删除"
                else -> ""
            }
            view.mBtn.text = text
        }
    }
}