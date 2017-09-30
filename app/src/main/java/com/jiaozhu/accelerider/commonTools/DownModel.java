package com.jiaozhu.accelerider.commonTools;

import android.app.DownloadManager;
import android.database.Cursor;

public class DownModel {
    private int bytesDL; //已下载数据
    private int fileSize; //文件大小
    private long id; //下载ID
    private int reason; //原因
    private int status; //状态
    private String title; //标题

    public DownModel(Cursor paramCursor) {
        this.status = paramCursor.getInt(paramCursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        this.fileSize = paramCursor.getInt(paramCursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
        this.bytesDL = paramCursor.getInt(paramCursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
        this.reason = paramCursor.getInt(paramCursor.getColumnIndex(DownloadManager.COLUMN_REASON));
        this.id = paramCursor.getLong(paramCursor.getColumnIndex(DownloadManager.COLUMN_ID));
        this.title = paramCursor.getString(paramCursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
    }

    public int getBytesDL() {
        return this.bytesDL;
    }

    public int getFileSize() {
        return this.fileSize;
    }

    public long getId() {
        return this.id;
    }

    public int getReason() {
        return this.reason;
    }

    public int getStatus() {
        return this.status;
    }

    public String getTitle() {
        return this.title;
    }

    public void setBytesDL(int paramInt) {
        this.bytesDL = paramInt;
    }

    public void setFileSize(int paramInt) {
        this.fileSize = paramInt;
    }

    public void setId(long paramLong) {
        this.id = paramLong;
    }

    public void setReason(int paramInt) {
        this.reason = paramInt;
    }

    public void setStatus(int paramInt) {
        this.status = paramInt;
    }

    public void setTitle(String paramString) {
        this.title = paramString;
    }

    public String toString() {
        return "DownModel{status=" + this.status + ", fileSize=" + this.fileSize +
                ", bytesDL=" + this.bytesDL + ", reason=" + this.reason + ", id=" +
                this.id + ", title='" + this.title + '\'' + '}';
    }
}