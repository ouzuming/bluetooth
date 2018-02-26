package com.vise.bledemo.myClass;

import android.util.Log;

import com.vise.bledemo.activity.HttpCallbackLister;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
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
                    Log.d(TAG, "newCall" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d(TAG, "call onFailure-1" + e + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                            httpCallbackLister.failure(e);
                            Log.d(TAG, "onFailure" + e + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                            return;
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.d(TAG, "call onResponse" + response + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                            Response get_response = response;
                            String responseData = get_response.body().string();
                            httpCallbackLister.success(responseData);
                        }
                    });
            }
        }).start();
    }
}
