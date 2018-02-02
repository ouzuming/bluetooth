package com.vise.bledemo.myClass;

import android.util.Log;

import com.vise.bledemo.activity.HttpCallbackLister;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by 3ivr on 2018/2/2.
 */

public class OkHttpUtil {
    private  HttpCallbackLister httpCallbackLister;
    private static final String TAG = "TAG_OKHttpUtil";
    public OkHttpUtil(HttpCallbackLister lister){
        httpCallbackLister = lister;
    }

    public void sendHttpRequest(final String uri_address) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "client" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(uri_address)
                        .build();
                Response response = null;
                try {
                    Log.d(TAG, "newCall" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    response = client.newCall(request).execute();
                    if (response == null) {
                        Log.d(TAG, "response is null" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                        httpCallbackLister.failure();
                        return;
                    }
                    String responseData = response.body().string();
                    Log.d(TAG, "response" + responseData + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    httpCallbackLister.success(responseData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
