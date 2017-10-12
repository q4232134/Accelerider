package com.jiaozhu.accelerider.panel.taskManger

import android.text.TextUtils
import android.util.SparseArray
import com.jiaozhu.accelerider.R

import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadConnectListener
import com.liulishuo.filedownloader.FileDownloadSampleListener
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import com.liulishuo.filedownloader.util.FileDownloadUtils
import java.io.File

import java.lang.ref.WeakReference

/**
 * Created by jiaozhu on 2017/10/12.
 */

class TasksManager private constructor() {

    private val dbController: TasksManagerDBController = TasksManagerDBController()
    private val modelList: MutableList<TasksManagerModel>

    private val taskSparseArray = SparseArray<BaseDownloadTask>()

    private var listener: FileDownloadConnectListener? = null

    val isReady: Boolean
        get() = FileDownloader.getImpl().isServiceConnected

    val taskCounts: Int
        get() = modelList.size

    interface INotifyDataChanged {
        fun postNotifyDataChanged()
    }

    private object HolderClass {
        val INSTANCE = TasksManager()
    }

    init {
        modelList = dbController.allTasks

        initDemo()
    }

    private fun initDemo() {}

    fun addTask(task: BaseDownloadTask) {
        taskSparseArray.put(task.id, task)
    }

    fun removeTask(id: Int) {
        taskSparseArray.remove(id)
    }

    /**
     * 根据ID删除任务
     *
     * @param id
     */
    fun deleteTaskById(id: Int) {
        dbController.deleteTaskById(id)
        modelList.removeAt(modelList.indexOfFirst { id == it.id })
        val task = getTaskById(id) ?: return
        task.pause()
        //任务未完成则删除临时文件
        if (task.status != FileDownloadStatus.completed) {
            File(task.path).delete()
        }
        taskSparseArray.remove(id)
    }

    fun updateViewHolder(id: Int, holder: TaskItemViewHolder) {
        val task = taskSparseArray.get(id) ?: return

        task.tag = holder
    }

    /**
     * 开始所有任务
     */
    fun startAll() {
        for (i in 0 until taskSparseArray.size()) {
            taskSparseArray.valueAt(i).start()
        }
    }

    /**
     * 根据Id获取任务
     */
    fun getTaskById(id: Int): BaseDownloadTask? {
        return taskSparseArray[id]
    }

    fun releaseTask() {
        taskSparseArray.clear()
    }

    private fun registerServiceConnectionListener(activityWeakReference: WeakReference<INotifyDataChanged>?) {
        if (listener != null) {
            FileDownloader.getImpl().removeServiceConnectListener(listener)
        }

        listener = object : FileDownloadConnectListener() {

            override fun connected() {
                if (activityWeakReference?.get() == null) {
                    return
                }

                activityWeakReference.get()?.postNotifyDataChanged()
            }

            override fun disconnected() {
                if (activityWeakReference?.get() == null) {
                    return
                }

                activityWeakReference.get()?.postNotifyDataChanged()
            }
        }

        FileDownloader.getImpl().addServiceConnectListener(listener)
    }

    private fun unregisterServiceConnectionListener() {
        FileDownloader.getImpl().removeServiceConnectListener(listener)
        listener = null
    }

    fun onCreate() {
        if (!FileDownloader.getImpl().isServiceConnected) {
            FileDownloader.getImpl().bindService()
        }
    }

    /**
     * 注册回调
     *
     * @param activityWeakReference
     */
    fun registerListener(activityWeakReference: WeakReference<INotifyDataChanged>) {
        registerServiceConnectionListener(activityWeakReference)
    }

    fun onDestroy() {
        unregisterServiceConnectionListener()
        releaseTask()
    }

    operator fun get(position: Int): TasksManagerModel {
        return modelList[position]
    }

    fun getById(id: Int): TasksManagerModel? {
        for (model in modelList) {
            if (model.id == id) {
                return model
            }
        }

        return null
    }

    /**
     * @param status Download Status
     * @return has already downloaded
     * @see FileDownloadStatus
     */
    fun isDownloaded(status: Int): Boolean {
        return status == FileDownloadStatus.completed.toInt()
    }

    fun getStatus(id: Int, path: String): Int {
        return FileDownloader.getImpl().getStatus(id, path).toInt()
    }

    fun getTotal(id: Int): Long {
        return FileDownloader.getImpl().getTotal(id)
    }

    fun getSoFar(id: Int): Long {
        return FileDownloader.getImpl().getSoFar(id)
    }

    @JvmOverloads
    fun createTask(url: String, path: String? = createPath(url), name: String = url): TasksManagerModel? {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(path)) {
            return null
        }

        val id = FileDownloadUtils.generateId(url, path)
        val model = getById(id)
        if (model != null) {
            return model
        }
        val newModel = dbController.addTask(url, path, name)
        if (newModel != null) {
            modelList.add(newModel)
        }
        val task = FileDownloader.getImpl().create(newModel!!.url)
                .setPath(newModel!!.path)
                .setWifiRequired(true)
                .setCallbackProgressTimes(100)
                .setListener(taskDownloadListener)
        TasksManager.impl.addTask(task)
        task.start()
        return newModel
    }

    fun createPath(url: String): String? {
        return if (TextUtils.isEmpty(url)) {
            null
        } else FileDownloadUtils.getDefaultSaveFilePath(url)

    }

    companion object {

        val impl: TasksManager
            get() = HolderClass.INSTANCE


        private val taskDownloadListener = object : FileDownloadSampleListener() {

            private fun checkCurrentHolder(task: BaseDownloadTask?): TaskItemViewHolder? {
                val tag = task!!.tag as TaskItemViewHolder
                return if (tag.id != task.id) {
                    null
                } else tag
            }

            override fun pending(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                super.pending(task, soFarBytes, totalBytes)
                val tag = checkCurrentHolder(task) ?: return

                tag.updateDownloading(FileDownloadStatus.pending.toInt(), soFarBytes.toLong(), totalBytes.toLong(), -1)
                tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_pending)
            }

            override fun started(task: BaseDownloadTask?) {
                super.started(task)
                val tag = checkCurrentHolder(task) ?: return

                tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_started)
            }

            override fun connected(task: BaseDownloadTask?, etag: String?, isContinue: Boolean, soFarBytes: Int, totalBytes: Int) {
                super.connected(task, etag, isContinue, soFarBytes, totalBytes)
                val tag = checkCurrentHolder(task) ?: return

                tag.updateDownloading(FileDownloadStatus.connected.toInt(), soFarBytes.toLong(), totalBytes.toLong(), -1)
                tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_connected)
            }

            override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                super.progress(task, soFarBytes, totalBytes)
                val tag = checkCurrentHolder(task) ?: return

                tag.updateDownloading(FileDownloadStatus.progress.toInt(), soFarBytes.toLong(), totalBytes.toLong(), task!!.speed)
            }

            override fun error(task: BaseDownloadTask?, e: Throwable?) {
                super.error(task, e)
                val tag = checkCurrentHolder(task) ?: return

                tag.updateNotDownloaded(FileDownloadStatus.error.toInt(), task!!.largeFileSoFarBytes, task.largeFileTotalBytes)
                TasksManager.impl.removeTask(task.id)
            }

            override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                super.paused(task, soFarBytes, totalBytes)
                val tag = checkCurrentHolder(task) ?: return

                tag.updateNotDownloaded(FileDownloadStatus.paused.toInt(), soFarBytes.toLong(), totalBytes.toLong())
                tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_paused)
                TasksManager.impl.removeTask(task!!.id)
            }

            override fun completed(task: BaseDownloadTask?) {
                super.completed(task)
                val tag = checkCurrentHolder(task) ?: return

                tag.updateDownloaded()
                TasksManager.impl.removeTask(task!!.id)
            }
        }
    }
}

