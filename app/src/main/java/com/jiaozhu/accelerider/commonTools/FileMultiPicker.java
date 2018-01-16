package com.jiaozhu.accelerider.commonTools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.jiaozhu.accelerider.R;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2015/1/20.
 * 带过滤功能的多项选择器
 */
public class FileMultiPicker {
    private static AlertDialog dialog;
    private static List<File> list;
    private static SelectFileAdapter adapter;
    private static File currentFile;
    private static File rootFile;
    private static Context con;
    private static FileFilter filter;


    public static void showDialog(final Context context, String title, File root, @Nullable FileFilter fileFilter,
                                  final OnSelectFinishListener listener, boolean canCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        rootFile = currentFile = root;
        con = context;
        filter = fileFilter;
        list = new ArrayList<>(Arrays.asList(root.listFiles()));
        builder.setCancelable(canCancel);
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_comm_recycle, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.listView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SelectFileAdapter(list);
        adapter.setFileListListener(new SelectFileAdapter.FileListListener() {
            @Override
            public void onItemClick(int position, File file, SelectFileAdapter.ViewHolder holder) {
                if (file.isFile()) {
                    adapter.getSelectedSet();
                    holder.mCheckBox.setChecked(!holder.mCheckBox.isChecked());
                } else {
                    fresh(file);
                }
            }

        });
        recyclerView.setAdapter(adapter);
        builder.setView(view);
        builder.setCancelable(canCancel);
        builder.setNeutralButton("上一级目录", null);
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null)
                    listener.onSelectFinished(adapter.getSelectedSet());
            }
        });
        dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fresh(currentFile.getParentFile());
            }
        });
    }

    private static void fresh(File file) {
        if (file.equals(rootFile.getParentFile())) {
            Toast.makeText(con, "已经是最上级目录了", Toast.LENGTH_SHORT).show();
            return;
        }
        currentFile = file;
        adapter.clearSelect();
        list.clear();
        if (filter == null) {
            list.addAll(Arrays.asList(file.listFiles()));
        } else {
            list.addAll(Arrays.asList(file.listFiles(filter)));
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 选择完成接口
     */
    public interface OnSelectFinishListener {
        /**
         * 返回被选中的项目
         *
         * @param list
         */
        void onSelectFinished(List<File> list);
    }

}
