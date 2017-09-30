package com.jiaozhu.accelerider.commonTools;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.List;

/**
 * Created by Administrator on 2015/1/20.
 * 选择器
 */
public class SingerPicker {
    private static String[] str = null;
    private static Dialog dialog;


    /**
     * 显示单选对话框
     *
     * @param context
     * @param title       标题
     * @param list        数据列表
     * @param callBack    选择完成回调
     * @param currentTask 当前选择(无默认则为-1)
     * @param canCancel   是否可以取消
     * @param isObject    是否只存在一个实例
     */
    public static void showDialog(final Context context, String title, final List<Description> list,
                                  final OnItemSelectedListener callBack, int currentTask,
                                  boolean canCancel, boolean isObject) {
        if (isObject && dialog != null && dialog.isShowing()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        str = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            str[i] = list.get(i).description();
        }
        builder.setCancelable(canCancel);
        builder.setSingleChoiceItems(str, currentTask, new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callBack.onItemSelected(which, list.get(which));
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position, Description description);
    }

    /**
     * 描述接口
     */
    public interface Description {
        String description();
    }
}
