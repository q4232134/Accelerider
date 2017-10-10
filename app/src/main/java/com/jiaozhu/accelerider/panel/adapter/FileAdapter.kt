package com.jiaozhu.accelerider.panel.adapter

import android.content.Context
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jiaozhu.accelerider.R
import com.jiaozhu.accelerider.commonTools.SelectorRecyclerAdapter
import com.jiaozhu.accelerider.model.FileModel
import com.jiaozhu.accelerider.support.Tools
import kotlinx.android.synthetic.main.item_file_list.view.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by jiaozhu on 2017/10/10.
 */
class FileAdapter(val context: Context, val list: ArrayList<FileModel>) : SelectorRecyclerAdapter<FileAdapter.ViewHolder>() {
    override fun getItemCount(): Int = list.size
    private val resource: Resources = context.resources
    private val format = SimpleDateFormat("hh:mm  yyyy-MM-dd")

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindView(holder: ViewHolder, position: Int, isSelected: Boolean) {
        val model = list[position]
        with(holder.itemView) {
            if (isSelected) {
                mSelectView.visibility = View.VISIBLE
                setBackgroundColor(resource.getColor(R.color.main_item_selected_bg))
            } else {
                mSelectView.visibility = View.GONE
                background = null
            }
            if (model.isdir) {
                mImageView.setImageResource(R.drawable.ico_folder)
                mDownload.visibility = View.GONE
                mSize.text = ""
            } else {
                val index = model.server_filename.lastIndexOf(".")
                val end = model.server_filename.drop(index + 1).toLowerCase()
                mImageView.setImageResource(getResource(end))
                mDownload.visibility = View.VISIBLE
                mSize.text = Tools.getSizeString(model.size)
            }

            mTitle.text = model.server_filename
            mTime.text = format.format(model.server_mtime * 1000)
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v)

    /**
     * 根据资源名称获取id
     */
    private fun getResource(end: String): Int {
        //如果没有在"mipmap"下找到imageName,将会返回0
        var temp = resource.getIdentifier("ico_$end", "drawable", context.packageName)
        if (temp == 0) {
            temp = resource.getIdentifier("ico_htm", "drawable", context.packageName)
        }
        return temp
    }

}