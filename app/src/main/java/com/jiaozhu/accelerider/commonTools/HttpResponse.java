package com.jiaozhu.accelerider.commonTools;

import android.support.annotation.Nullable;

import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;


/**
 * Created by apple on 15/11/6.
 * http回调封装
 */
public abstract class HttpResponse extends AsyncHttpResponseHandler {
    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        String temp = getString(responseBody);
        Log.d("HttpResponse", temp);
        onSuccess(statusCode, temp);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        onFailure(statusCode, error);
    }

    @Nullable
    private String getString(byte[] responseBody) {
        String str = null;
        try {
            str = new String(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    abstract public void onSuccess(int statusCode, String result);

    abstract public void onFailure(int statusCode, Throwable error);
}
