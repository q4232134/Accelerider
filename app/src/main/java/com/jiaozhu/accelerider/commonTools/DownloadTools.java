package com.jiaozhu.accelerider.commonTools;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadTools {
    private static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");
    private static DownloadManager downloadManager;
    private static Handler handler = new Handler();
    private static DownloadChangeObserver downloadObserver = new DownloadChangeObserver(handler);
    private static List<Long> list = new ArrayList();
    private static IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
    private static OnDownload onDownload;
    private static BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context paramContext, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            DownloadTools.queryDownloadStatus();
            DownloadTools.list.remove(id);
        }
    };

    public static OnDownload getOnDownload() {
        return onDownload;
    }

    /**
     * 查询下载状态
     */
    protected static void queryDownloadStatus() {
        if (list.size() < 1)
            return;
        DownloadManager.Query query = new DownloadManager.Query();
        //将List转换为数组
        long[] ids = new long[list.size()];
        for (int i = 0; i < list.size(); ++i)
            ids[i] = list.get(i);
        query.setFilterById(ids);
        Cursor cursor = downloadManager.query(query);
        HashMap hashMap = new HashMap();
        while (cursor.moveToNext()) {
            DownModel localDownModel = new DownModel(cursor);
            hashMap.put(localDownModel.getId(), localDownModel);
        }
        cursor.close();
        if (onDownload == null) {
            onDownload.onDownloading(hashMap);
        }
    }

    /**
     * 注册过程接收器
     *
     * @param paramContext
     */
    public static void registerObserver(Context paramContext) {
        paramContext.getContentResolver().registerContentObserver(CONTENT_URI, true, downloadObserver);
    }

    /**
     * 注册完成接收器
     *
     * @param paramContext
     */
    public static void registerReceiver(Context paramContext) {
        paramContext.registerReceiver(receiver, filter);
    }

    public static void setOnDownload(OnDownload paramOnDownload) {
        onDownload = paramOnDownload;
    }

    public static long startDownload(Context paramContext, String url, String dirType,
                                     String subPath, int visibility) {
        downloadManager = (DownloadManager) paramContext.getSystemService(Context.DOWNLOAD_SERVICE);
        Request localRequest = new Request(Uri.parse(url));
        localRequest.setDestinationInExternalFilesDir(paramContext, dirType, subPath);
        localRequest.setNotificationVisibility(visibility);
        long l = downloadManager.enqueue(localRequest);
        list.add(l);
        return l;
    }

    /**
     * 解除接收器
     *
     * @param paramContext
     */
    public static void unRegisterReceiver(Context paramContext) {
        paramContext.unregisterReceiver(receiver);
        paramContext.getContentResolver().unregisterContentObserver(downloadObserver);
        onDownload = null;
    }

    static class DownloadChangeObserver extends ContentObserver {
        public DownloadChangeObserver(Handler paramHandler) {
            super(paramHandler);
        }

        public void onChange(boolean paramBoolean) {
            DownloadTools.queryDownloadStatus();
        }
    }

    public interface OnDownload {
        void onDownloading(Map<Long, DownModel> paramMap);
    }
}