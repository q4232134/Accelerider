package com.jiaozhu.accelerider.panel.taskManger

import android.text.TextUtils
import android.util.SparseArray
import com.jiaozhu.accelerider.support.Preferences
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadConnectListener
import com.liulishuo.filedownloader.FileDownloadLargeFileListener
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import com.liulishuo.filedownloader.util.FileDownloadUtils
import java.lang.ref.WeakReference

/**
 * Created by jiaozhu on 2017/10/12.
 */

object TasksManager {
    private val dbController: TasksManagerDBController = TasksManagerDBController()
    val modelList: MutableList<TasksManagerModel>

    private val taskSparseArray = SparseArray<BaseDownloadTask>()

    private var listener: FileDownloadConnectListener? = null

    private val taskDownloadListener = object : FileDownloadLargeFileListener() {
        override fun warn(task: BaseDownloadTask) {
            downloadListener?.warn(task)
        }

        override fun pending(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {
            downloadListener?.pending(task, soFarBytes, totalBytes)
        }

        override fun started(task: BaseDownloadTask) {
            downloadListener?.started(task)
        }

        override fun connected(task: BaseDownloadTask, etag: String, isContinue: Boolean, soFarBytes: Long, totalBytes: Long) {
            downloadListener?.connected(task, etag, isContinue, soFarBytes, totalBytes)
        }

        override fun progress(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {
            downloadListener?.progress(task, soFarBytes, totalBytes)
        }

        override fun error(task: BaseDownloadTask, e: Throwable) {
            downloadListener?.error(task, e)
        }

        override fun paused(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {
            downloadListener?.paused(task, soFarBytes, totalBytes)
        }

        override fun completed(task: BaseDownloadTask) {
            downloadListener?.completed(task)

        }
    }

    interface DownloadListener {
        fun pending(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long)

        fun started(task: BaseDownloadTask)

        fun connected(task: BaseDownloadTask, etag: String, isContinue: Boolean, soFarBytes: Long, totalBytes: Long)

        fun progress(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long)

        fun error(task: BaseDownloadTask, e: Throwable)

        fun paused(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long)

        fun completed(task: BaseDownloadTask)

        fun warn(task: BaseDownloadTask)
    }

    var downloadListener: DownloadListener? = null

    val isReady: Boolean
        get() = FileDownloader.getImpl().isServiceConnected

    val taskCounts: Int
        get() = modelList.size

    interface INotifyDataChanged {
        fun postNotifyDataChanged()
    }

    //初始化列表
    init {
        modelList = dbController.allTasks
        modelList.forEach {
            if (it.isFinished == 0) {
                val task = FileDownloader.getImpl().create(it.url)
                        .setPath(it.path)
                        .setWifiRequired(!Preferences.downloadWithNet)
                        .setCallbackProgressTimes(100)
                        .setListener(taskDownloadListener)
                TasksManager.addTask(task)
            }
        }
    }


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
        val model = modelList.first { id == it.id }
        modelList.remove(model)
        //任务未完成则删除临时文件
        FileDownloadUtils.deleteTempFile(model.path + ".temp")
        val task = getTaskById(id) ?: return
        task.pause()
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
        modelList.forEach {
            startTask(it)
        }
    }

    fun pauseAll() {
        FileDownloader.getImpl().pauseAll()
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

    public fun unregisterServiceConnectionListener() {
        FileDownloader.getImpl().removeServiceConnectListener(listener)
        listener = null
    }

    fun onCreate() {
        if (!FileDownloader.getImpl().isServiceConnected) {
            FileDownloader.getImpl().bindService()
            FileDownloader.getImpl().setMaxNetworkThreadCount(2)
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
        return modelList.firstOrNull { it.id == id }
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

    fun getSoFar(id: Int): Long = FileDownloader.getImpl().getSoFar(id)


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
        newModel?.let { modelList.add(it) }
        return newModel
    }


    /**
     * 开始下载任务
     */
    fun startTask(model: TasksManagerModel?) {
        if (model == null || model.isFinished == 1) return
        val task = FileDownloader.getImpl().create(model.url)
                .setPath(model.path)
                .setAutoRetryTimes(15)
                .setWifiRequired(!Preferences.downloadWithNet)
                .setCallbackProgressMinInterval(500)
                .setCallbackProgressTimes(5000)
                .setListener(taskDownloadListener)
        TasksManager.addTask(task)
        taskSparseArray.put(task.id, task)
        task.start()
    }


    fun createPath(url: String): String? {
        return if (TextUtils.isEmpty(url)) {
            null
        } else FileDownloadUtils.getDefaultSaveFilePath(url)

    }

}

