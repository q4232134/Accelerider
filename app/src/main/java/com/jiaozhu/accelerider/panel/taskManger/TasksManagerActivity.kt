/*
 * Copyright (c) 2015 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiaozhu.accelerider.panel.taskManger

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.jiaozhu.accelerider.R
import com.jiaozhu.accelerider.panel.taskManger.TasksManager.modelList
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import kotlinx.android.synthetic.main.view_toolbar.*
import kotlinx.android.synthetic.main.view_toolbar_comm.*
import toast
import java.lang.ref.WeakReference

/**
 * Created by Jacksgong on 1/9/16.
 */
class TasksManagerActivity : AppCompatActivity(), TasksManager.INotifyDataChanged, TaskItemAdapter.TaskItemListener {
    private lateinit var adapter: TaskItemAdapter


    private val taskDownloadListener = object : TasksManager.DownloadListener {
        override fun warn(task: BaseDownloadTask) {

        }

        private fun checkCurrentHolder(task: BaseDownloadTask): TaskItemViewHolder? {
            val tag = task.tag as? TaskItemViewHolder ?: return null
            return if (tag.id != task.id) {
                null
            } else tag
        }

        override fun pending(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {
            val tag = checkCurrentHolder(task) ?: return

            tag.updateDownloading(FileDownloadStatus.pending.toInt(), soFarBytes.toLong(), totalBytes.toLong(), -1)
            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_pending)
        }

        override fun started(task: BaseDownloadTask) {
            val tag = checkCurrentHolder(task) ?: return

            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_started)
        }

        override fun connected(task: BaseDownloadTask, etag: String, isContinue: Boolean, soFarBytes: Long, totalBytes: Long) {
            val tag = checkCurrentHolder(task) ?: return

            tag.updateDownloading(FileDownloadStatus.connected.toInt(), soFarBytes.toLong(), totalBytes.toLong(), -1)
            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_connected)
        }

        override fun progress(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {
            val tag = checkCurrentHolder(task) ?: return

            tag.updateDownloading(FileDownloadStatus.progress.toInt(), soFarBytes.toLong(), totalBytes.toLong(), task.speed)
        }

        override fun error(task: BaseDownloadTask, e: Throwable) {
            e.printStackTrace()
            toast(e.message)
            val tag = checkCurrentHolder(task) ?: return

            tag.updateNotDownloaded(FileDownloadStatus.error.toInt(), task.largeFileSoFarBytes, task.largeFileTotalBytes)
            TasksManager.removeTask(task.id)
        }

        override fun paused(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {
            val tag = checkCurrentHolder(task) ?: return

            tag.updateNotDownloaded(FileDownloadStatus.paused.toInt(), soFarBytes.toLong(), totalBytes.toLong())
            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_paused)
            TasksManager.removeTask(task.id)
        }

        override fun completed(task: BaseDownloadTask) {
            val tag = checkCurrentHolder(task) ?: return

            tag.updateDownloaded()
            TasksManager.removeTask(task.id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks_manager)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        mTitle.text = "下载管理器"
        setSupportActionBar(mToolbar)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskItemAdapter()
        recyclerView.adapter = adapter
        adapter.listener = this
        TasksManager.registerListener(WeakReference(this))
        TasksManager.downloadListener = taskDownloadListener
    }

    override fun onItemClick(holder: TaskItemViewHolder, position: Int) {

    }

    override fun onItemLongClick(holder: TaskItemViewHolder, position: Int) {
        showDeleteDialog(TasksManager[position])
    }

    override fun onButtonClick(holder: TaskItemViewHolder, position: Int) {
        val action = holder.taskActionBtn.text
        when (action) {
            resources.getString(R.string.pause) -> FileDownloader.getImpl().pause(holder.id)
            resources.getString(R.string.start) -> {
                val model = TasksManager[holder.position]
                TasksManager.startTask(model)
                TasksManager.updateViewHolder(holder.id, holder)
            }

        }
    }


    /**
     * 弹出删除警告对话框
     */
    private fun showDeleteDialog(model: TasksManagerModel) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("删除任务")
        builder.setMessage("是否删除${model.name}任务（已经下载完成的文件不会删除）?")
        builder.setPositiveButton("删除") { _, _ ->
            TasksManager.deleteTaskById(model.id)
            adapter.notifyDataSetChanged()
        }
        builder.setNegativeButton("取消", null)
        builder.create().show()
    }

    override fun onDestroy() {
        TasksManager.unregisterServiceConnectionListener()
        TasksManager.downloadListener = null
        super.onDestroy()
    }


    override fun postNotifyDataChanged() {
        runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_task, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.task_clear -> clearFinished()
            R.id.task_pause -> TasksManager.pauseAll()
            R.id.task_start -> {
                TasksManager.startAll()
                adapter.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * 清空已完成
     */
    private fun clearFinished() {
        val needDelete = arrayListOf<Int>()
        modelList.forEach {
            //如果为已完成
            if (TasksManager.isDownloaded(TasksManager.getStatus(it.id, it.path))) {
                needDelete.add(it.id)
            }
        }
        needDelete.forEach {
            TasksManager.deleteTaskById(it)
        }
        adapter.notifyDataSetChanged()
    }

}
