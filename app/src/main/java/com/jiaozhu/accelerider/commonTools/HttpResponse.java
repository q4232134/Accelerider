package com.jiaozhu.accelerider.commonTools;

import android.support.annotation.Nullable;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;

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
        System.out.println(new String(this.getCharset()));
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        onFailure(statusCode, error);
    }

    @Override
    public void onPostProcessResponse(ResponseHandlerInterface instance, cz.msebera.android.httpclient.HttpResponse response) {
        System.out.println(response.getEntity());
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
