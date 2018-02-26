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
import com.vise.bledemo.myClass.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class webActivity extends AppCompatActivity implements View.OnClickListener {
    OkHttpUtil okHttpUtil;
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
        okHttpUtil = new OkHttpUtil(new HttpCallbackLister() {
            @Override
            public void success(String responseData) {
                Log.d(TAG, "call back success" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                parseJsonData(responseData);
            }

            @Override
            public void failure(IOException e) {
                Log.d(TAG, "call back failure" + e + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            }
        });
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
                Log.d(TAG, "bt_openOKHttp onClick  " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                String uri = json_uri;
                okHttpUtil.sendHttpRequest(uri);
                break;
            case R.id.bt_openWebView:
                Log.d(TAG, "bt_openWebView onClick " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                webViewFunction();
                break;
        }
    }
}

