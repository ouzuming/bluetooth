package com.vise.bledemo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.vise.bledemo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class webActivity extends AppCompatActivity implements View.OnClickListener ,httpCallbackLister{
    WebView mWebView;
    TextView mTv_okHttp, mTv_json;
    Button btn_webView, btn_OKHttp;
    private static final String TAG = "tag_webView";
   // private static final String default_uri = "https://www.baidu.com/";
   //private static final String default_uri = "http://192.168.4.110:80";
   private static final String default_uri = "http://192.168.4.110:80/jdata.json";
    private static final String json_uri = "http://192.168.4.110:80/jdata.json";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "setContentView" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        setContentView(R.layout.activity_web);
        Log.d(TAG, "widgetInit" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        widgetInit();
    }

    private void getJsonData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "client" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                                    .url(json_uri)
                                    .build();
                Response response = null;
                try {
                    Log.d(TAG, "newCall" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    response = client.newCall(request).execute();
                    if(response == null){
                        Log.d(TAG, "response is null" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                        return;
                    }
                    String responseData = response.body().string();
                    Log.d(TAG, "response" + responseData + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    parseJsonData(responseData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJsonData(String jsonData) {
        try {
            Log.d(TAG, "jsonArray: " + jsonData+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            JSONArray jsonArray = new JSONArray(jsonData);
            Log.d(TAG, "jsonArray length: " + jsonArray.length()+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < jsonArray.length()-1; i++){
                JSONObject jsonObject =  jsonArray.getJSONObject(i);
                builder.append("id: ").append(jsonObject.getString("id")).append("\n")
                        .append("version: ").append(jsonObject.getString("version")).append("\n")
                        .append("name: ").append(jsonObject.getString("name")).append("\n");

//                Log.d(TAG, "get id = "+ jsonObject.getString("id") + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
//                Log.d(TAG, "get version = "+ jsonObject.getString("version") + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
//                Log.d(TAG, "get name = "+ jsonObject.getString("name") + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                }
            tv_setJsonData(builder.toString());
        } catch (JSONException e) {
            Log.d(TAG, "builder data error"+ e + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            e.printStackTrace();
        }
    }

    private void tv_setJsonData(final String jsonData) {
        Log.d(TAG, "tv_setJsonData" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "json setText" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                mTv_json.setText(jsonData);
            }
        });

    }

    private void widgetInit(){
        mTv_okHttp = findViewById(R.id.tv_okHttp);
        mTv_json = findViewById(R.id.tv_json);
        btn_OKHttp = findViewById(R.id.bt_openOKHttp);
        btn_webView = findViewById(R.id.bt_openWebView);
        btn_webView.setOnClickListener(this);
        btn_OKHttp.setOnClickListener(this);
    }

    private void okHttpGetData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(default_uri)
                        .build();
                Log.d(TAG, "Request" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                try {
                    Response response = client.newCall(request).execute();
                    Log.d(TAG, "responseData" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    String responseData = response.body().string();
                     showResponseInfo(responseData);
                } catch (IOException e) {
                    Log.d(TAG, "response"+e + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    e.printStackTrace();
                }finally {

                }
            }
        }).start();
    }

    private void okHttpPostData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                                              .add("username","admin")
                                              .add("password","123456")
                                              .build();
                Request request = new Request.Builder()
                                        .url(default_uri)
                                        .post(requestBody)
                                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    String responseData = response.body().string();
                    showResponseInfo(responseData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void showResponseInfo(final String responseData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "textView set data"+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                mTv_okHttp.setText(responseData);
            }
        });
    }

    private void webViewFunction(){
        mWebView = findViewById(R.id.wv_webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());
        Log.d(TAG, "load uri " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        mWebView.loadUrl(default_uri);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_openOKHttp:
                getJsonData();
                break;
            case R.id.bt_openWebView:
                webViewFunction();
                break;
        }
    }

    @Override
    public void http_finish() {

    }

    @Override
    public void http_create() {

    }
}

