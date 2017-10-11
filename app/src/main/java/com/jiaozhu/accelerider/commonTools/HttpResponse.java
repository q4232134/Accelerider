package com.jiaozhu.accelerider.commonTools;

import android.support.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
        onSuccess(statusCode, JSON.parseObject(temp));
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        error.printStackTrace();
        String msg;
        msg = statusCode + "与服务器通信失败";
        onFailure(statusCode, msg, error);
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

    abstract public void onSuccess(int statusCode, JSONObject result);

    abstract public void onFailure(int statusCode, String msg, Throwable error);
}
