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
import com.jiaozhu.accelerider.R
import com.liulishuo.filedownloader.FileDownloader
import kotlinx.android.synthetic.main.view_toolbar.*
import kotlinx.android.synthetic.main.view_toolbar_comm.*
import java.lang.ref.WeakReference

/**
 * Created by Jacksgong on 1/9/16.
 */
class TasksManagerActivity : AppCompatActivity(), TasksManager.INotifyDataChanged, TaskItemAdapter.TaskItemListener {
    private lateinit var adapter: TaskItemAdapter

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
        TasksManager.impl.registerListener(WeakReference(this))
    }

    override fun onItemClick(holder: TaskItemViewHolder, position: Int) {

    }

    override fun onItemLongClick(holder: TaskItemViewHolder, position: Int) {
        showDeleteDialog(TasksManager.impl[position])
    }

    override fun onButtonClick(holder: TaskItemViewHolder, position: Int) {
        val action = holder.taskActionBtn.text
        when (action) {
            resources.getString(R.string.pause) -> FileDownloader.getImpl().pause(holder.id)
            resources.getString(R.string.start) -> {
                val model = TasksManager.impl[holder.position]
                TasksManager.impl.getTaskById(model.id)?.start()
                TasksManager.impl.updateViewHolder(holder.id, holder)
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
        builder.setPositiveButton("继续") { _, _ ->
            TasksManager.impl.deleteTaskById(model.id)
            adapter.notifyDataSetChanged()
        }
        builder.setNegativeButton("取消", null)
        builder.create().show()
    }


    override fun postNotifyDataChanged() {
        runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }

}
