package com.jiaozhu.accelerider.panel.taskManger

import android.view.LayoutInflater
import android.view.ViewGroup
import com.jiaozhu.accelerider.R
import com.jiaozhu.accelerider.commonTools.SelectorRecyclerAdapter
import com.liulishuo.filedownloader.model.FileDownloadStatus
import com.liulishuo.filedownloader.util.FileDownloadUtils
import java.io.File

/**
 * Created by jiaozhu on 2017/10/12.
 */

class TaskItemAdapter : SelectorRecyclerAdapter<TaskItemViewHolder>() {
    var listener: TaskItemListener? = null

    /**
     * 回调
     */
    interface TaskItemListener {
        fun onItemClick(holder: TaskItemViewHolder, position: Int)
        fun onItemLongClick(holder: TaskItemViewHolder, position: Int)
        fun onButtonClick(holder: TaskItemViewHolder, position: Int)
    }

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): TaskItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tasks_manager, parent, false)
        val holder = TaskItemViewHolder(view)
        view.setOnLongClickListener {
            listener?.onItemLongClick(holder, holder.layoutPosition)
            true
        }
        view.setOnClickListener {
            listener?.onItemClick(holder, holder.layoutPosition)
        }
        holder.taskActionBtn.setOnClickListener {
            listener?.onButtonClick(holder, holder.layoutPosition)
        }
        return holder
    }

    override fun onBindView(holder: TaskItemViewHolder, position: Int, isSelected: Boolean) {
        val model = TasksManager[position]

        holder.update(model.id, position)
        holder.taskActionBtn.tag = holder
        holder.taskNameTv.text = model.name

        TasksManager.updateViewHolder(holder.id, holder)



        if (TasksManager.isReady) {
            val status = TasksManager.getStatus(model.id, model.path)
            if (status == FileDownloadStatus.pending.toInt() || status == FileDownloadStatus.started.toInt() ||
                    status == FileDownloadStatus.connected.toInt()) {
                // start task, but file not created yet
                holder.updateDownloading(status, TasksManager.getSoFar(model.id), TasksManager.getTotal(model.id), -1)
            } else if (!File(model.path).exists() && !File(FileDownloadUtils.getTempPath(model.path)).exists()) {
                // not exist file
                holder.updateNotDownloaded(status, 0, 0)
            } else if (TasksManager.isDownloaded(status)) {
                // already downloaded and exist
                holder.updateDownloaded()
            } else if (status == FileDownloadStatus.progress.toInt()) {
                // downloading
                holder.updateDownloading(status, TasksManager.getSoFar(model.id), TasksManager.getTotal(model.id), -1)
            } else {
                // not start
                holder.updateNotDownloaded(status, TasksManager.getSoFar(model.id), TasksManager.getTotal(model.id))
            }
        } else {
            holder.taskStatusTv.setText(R.string.tasks_manager_demo_status_loading)
            holder.taskActionBtn.isEnabled = false
        }
    }


    override fun getItemCount(): Int = TasksManager.taskCounts
}