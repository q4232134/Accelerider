package com.jiaozhu.accelerider.panel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import checkPermission
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.jiaozhu.accelerider.R
import com.jiaozhu.accelerider.commonTools.HttpResponse
import com.jiaozhu.accelerider.commonTools.SelectorRecyclerAdapter
import com.jiaozhu.accelerider.commonTools.SingerPicker
import com.jiaozhu.accelerider.model.CustomSqliteActor
import com.jiaozhu.accelerider.model.FileModel
import com.jiaozhu.accelerider.model.Task
import com.jiaozhu.accelerider.model.UserModel
import com.jiaozhu.accelerider.panel.adapter.FileAdapter
import com.jiaozhu.accelerider.panel.fragment.CommRecycleFragment
import com.jiaozhu.accelerider.support.ClientFactoryImpl
import com.jiaozhu.accelerider.support.HttpClient
import com.jiaozhu.accelerider.support.Preferences
import com.jiaozhu.accelerider.support.Tools
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_toolbar.*
import kotlinx.android.synthetic.main.view_toolbar_comm.*
import toast
import zlc.season.rxdownload3.RxDownload
import zlc.season.rxdownload3.core.DownloadConfig
import zlc.season.rxdownload3.core.Mission
import java.util.*

class MainActivity : BaseActivity(), SelectorRecyclerAdapter.OnItemClickListener {

    private lateinit var fragment: CommRecycleFragment
    private val fileList: ArrayList<FileModel> = arrayListOf()
    private lateinit var adapter: FileAdapter
    private val stack = Stack<FileModel>()
    private val PERMISSION_FILE = 1030;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mToolbar)
        adapter = FileAdapter(this, fileList)
        fragment = CommRecycleFragment.newInstance(fileList, adapter)
        supportFragmentManager.beginTransaction().add(R.id.layout, fragment).commit()
        adapter.itemClickListener = this
        adapter.selectorMode = SelectorRecyclerAdapter.MODE_MULTI
        adapter.onDownloadClickListener = SelectorRecyclerAdapter.OnItemClickListener { view, position ->
            getUrls(fileList[position])
        }
        adapter.setActionView(mToolbar, object : SelectorRecyclerAdapter.ActionItemClickedListener {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                val inflater = mode.menuInflater
                inflater.inflate(R.menu.menu_action_main, menu)
                return true
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                val id = item.itemId
                when (id) {
                    R.id.action_download -> {
                        val temps = adapter.selectList.map { fileList[it] }
                        getFileList(temps) {
                            getUrls(*it.toTypedArray(), splitName = stack.lastElement().path+"/")
                        }
                        return true
                    }
                }
                return false
            }
        })
        init()
        initDownload()
        mBtn.setOnClickListener {
            val i = Intent(this@MainActivity, DownloadListActivity::class.java)
            startActivity(i)
        }
        mBtn.setOnLongClickListener { getDatabasePath("RxDownload.db").let { Tools.copyFile(it.path, Preferences.DownloadPath + "1.db") }.apply { toast(it.toString()) } }
    }

    /**
     * 初始化下载
     */
    private fun initDownload() {
        checkPermission(PERMISSION_FILE, Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            DownloadConfig.Builder.create(this)
                    .setFps(20)                         //设置更新频率
                    .setMaxRange(Runtime.getRuntime().availableProcessors() + 1)
                    .setMaxMission(2)
                    .setDbActor(CustomSqliteActor(this))
                    .setRetryTimes(5)
                    .enableAutoStart(true)              //自动开始下载
                    .enableDb(true)                             //启用数据库
                    .enableService(true)                        //启用Service
                    .enableNotification(true)                   //启用Notification
                    .setOkHttpClientFacotry(ClientFactoryImpl())
                    .setDefaultPath(Preferences.path)
                    .setOnlyWifiDownload(!Preferences.downloadWithNet)
                    .apply { DownloadConfig.init(this) }
        }
    }


    /**
     * 继承onRequestPermissionsResult方法
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_FILE -> {
                if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    toast("未取得写文件权限，将无法进行下载")
                }
            }
        }
    }

    private fun init() {
        if (Preferences.uk.isEmpty())
            getUserList()
        else {
            stack.clear()
            stack.push(FileModel())
            fresh()
        }
    }

    /**
     * 下载选中的文件或者文件夹，文件夹自动展开下载对应文件
     */
    private fun getFileList(fileModels: List<FileModel>, finish: (List<FileModel>) -> Unit) {
        spinnerDialog.setTitle("正在获取文件目录")
        spinnerDialog.show()
        val map = fileModels.groupBy { it.isdir == 0 }
        val list = mutableListOf<FileModel>()
        val dirs = mutableListOf<FileModel>()
        list.addAll(map[true] ?: emptyList())
        dirs.addAll(map[false] ?: emptyList())
        /**
         * 弹出第一个文件夹
         */
        fun getFirst(): FileModel? {
            val temp = dirs.getOrNull(0) ?: return null
            dirs.removeAt(0)
            return temp
        }

        /**
         * 展开指定文件夹
         */
        fun open(model: FileModel?, finish: () -> Unit) {
            if (model == null) {
                finish.invoke()
//                spinnerDialog.dismiss()
                return
            }
            openDir(model, {
                it.forEach {
                    if (it.isdir == 0) {
                        list.add(it)
                    } else {
                        dirs.add(it)
                    }
                }
                open(getFirst(), finish)
            }, {
                toast(it)
                open(getFirst(), finish)
            })
            dirs.remove(model)
        }

        open(getFirst()) {
            finish.invoke(list)
        }
    }


    private fun openDir(model: FileModel, suc: (List<FileModel>) -> Unit, fail: (String) -> Unit) {
        HttpClient.getFileList(model.path, object : HttpResponse() {
            override fun onSuccess(statusCode: Int, result: JSONObject) {
                suc.invoke(result.getJSONArray("list").toJavaList(FileModel::class.java) as List<FileModel>)
            }

            override fun onFailure(statusCode: Int, msg: String, error: Throwable) {
                fail(msg)
            }

        })
    }

    /**
     * 批量获取下载链接
     */
    private fun getUrls(vararg fileModels: FileModel, splitName: String = "") {
        spinnerDialog.setTitle("正在获取下载地址")
        spinnerDialog.show()
        HttpClient.getDownloadUrl(fileModels.toList(), object : HttpResponse() {

            override fun onSuccess(statusCode: Int, result: JSONObject) {
                println(result.toJSONString())
                val s = result.getJSONObject("links").entries.map {
                    val name = it.key
                    val url = (it.value as List<String>)[0]
                    val model = fileModels.find { model -> model.server_filename == it.key }
                    model?.let {
                        Task(it, Mission(url, it.server_filename, Preferences.DownloadPath + it.path.substringAfter(splitName).substringBeforeLast("/")).apply { tag = Preferences.name + ":" + it.path })
                    } ?: return
                }.apply { createTask(*this.toTypedArray()) }
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
     * 批量生成任务
     */
    private fun createTask(vararg tasks: Task) {
        tasks.forEach {
//            println(it)
            RxDownload.create(it).retry(10).observeOn(AndroidSchedulers.mainThread()).subscribe()
        }
        toast("添加${tasks.size}个任务")
    }


    /**
     * 获取用户列表
     */
    fun getUserList() {
        spinnerDialog.show()
        spinnerDialog.setTitle("正在获取账户列表")
        HttpClient.getUserList(object : HttpResponse() {
            override fun onSuccess(statusCode: Int, result: JSONObject) {
                if (result.getInteger("errno") == 0) {
                    Preferences.userList = result.getString("userlist")
                    showUserListDialog(JSON.parseArray(Preferences.userList, UserModel::class.java))

                }
            }

            override fun onFailure(statusCode: Int, msg: String, error: Throwable) {
                toast(msg)
            }

            override fun onFinish() {
                spinnerDialog.dismiss()
            }

        })
    }

    /**
     * 弹出账户选择列表
     */
    private fun showUserListDialog(userList: List<UserModel>) {
        SingerPicker.showDialog(this, "账户列表", userList, { position, _ ->
            Preferences.uk = userList[position].Uk
            Preferences.name = userList[position].description()
            init()
            fresh()
        }, userList.indexOfFirst { it.Name == Preferences.uk }, true, true)
    }

    /**
     * 刷新文件列表
     * @param   addStack    是否需要进栈
     */
    private fun fresh(file: FileModel? = null, addStack: Boolean = true) {
        spinnerDialog.show()
        spinnerDialog.setTitle("正在获取文件列表")
        HttpClient.getFileList(file?.path ?: "/", object : HttpResponse() {
            override fun onSuccess(statusCode: Int, result: JSONObject?) {
                if (addStack)
                    stack.push(file)
                mTitle.text = Preferences.name + (file?.path ?: "/")
                fileList.clear()
                fileList.addAll(result?.getJSONArray("list")?.toJavaList(FileModel::class.java) as List<FileModel>)
                fragment.adapter?.notifyDataSetChanged()
            }

            override fun onFailure(statusCode: Int, msg: String, error: Throwable) {
                error.printStackTrace()
            }

            override fun onFinish() {
                spinnerDialog.dismiss()
            }

        })
    }


    /**
     * 弹出蜂窝网络警告对话框
     */
    private fun showNetWarnDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("同步数据")
        builder.setMessage("您现在处于非WIFI环境，是否继续进行同步？")
        builder.setPositiveButton("继续") { _, _ -> }
        builder.setNegativeButton("取消", null)
        builder.create().show()
    }

    override fun onItemClick(view: View?, position: Int) {
        val model = fileList[position]
        if (model.isdir != 0)
            fresh(model)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_change_user -> getUserList()
            R.id.action_setting -> toSettingActivity()
            R.id.action_fresh -> fresh(stack.lastElement(), false)
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 进入设定界面
     */
    private fun toSettingActivity() {
        val i = Intent(this@MainActivity, SettingActivity::class.java)
        startActivity(i)
    }


    private var backPressTime: Long = 0
    override fun onBackPressed() {
        if (stack.size > 1) {
            stack.pop()
            fresh(stack.lastElement(), false)
            return
        }
        if (System.currentTimeMillis() - backPressTime > 2000) {
            backPressTime = System.currentTimeMillis()
            Toast.makeText(this, "双击返回键退出应用", Toast.LENGTH_SHORT).show()
        } else {
            finish()
            System.exit(0)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}


