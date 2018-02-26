package com.vise.bledemo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.vise.bledemo.R;
public class functinSelectActivity extends AppCompatActivity implements View.OnClickListener {
    Button mbt_atuo, mbt_manual, mbt_file, mbt_photo, mbt_mediaPlayer,mbt_video;
    Button mbt_webView, mbt_service,mbt_SQL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_functin_select);
        widget_init();

    }
    private void widget_init(){
        mbt_atuo = findViewById(R.id.bt_auto);
        mbt_manual = findViewById(R.id.bt_manual);
        mbt_file = findViewById(R.id.bt_file);
        mbt_photo = findViewById(R.id.bt_photo);
        mbt_mediaPlayer = findViewById(R.id.bt_mediaPlayer);
        mbt_video = findViewById(R.id.bt_video);
        mbt_webView = findViewById(R.id.bt_webView);
        mbt_service = findViewById(R.id.bt_serviceTest);
        mbt_SQL = findViewById(R.id.bt_SQL);
        mbt_atuo.setOnClickListener(this);
        mbt_manual.setOnClickListener(this);
        mbt_file.setOnClickListener(this);
        mbt_photo.setOnClickListener(this);
        mbt_mediaPlayer.setOnClickListener(this);
        mbt_video.setOnClickListener(this);
        mbt_webView.setOnClickListener(this);
        mbt_service.setOnClickListener(this);
        mbt_SQL.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.bt_auto:
                intent = new Intent(functinSelectActivity.this,AutoConnectActivity.class);
                break;
            case R.id.bt_manual:
                intent = new Intent(functinSelectActivity.this,mybleActivity.class);
                break;

            case R.id.bt_file:
                intent = new Intent(functinSelectActivity.this,fileActivity.class);
                break;
            case R.id.bt_photo:
                intent = new Intent(functinSelectActivity.this,photoActivity.class);
                break;

            case R.id.bt_mediaPlayer:
                intent = new Intent(functinSelectActivity.this,mediaPlayerActivity.class);
                break;

            case R.id.bt_video:
                intent = new Intent(functinSelectActivity.this,VideoActivity.class);
                break;

            case R.id.bt_webView:
                intent = new Intent(functinSelectActivity.this,webActivity.class);
                break;

            case R.id.bt_serviceTest:
                intent = new Intent(functinSelectActivity.this,ServiceActivity.class);
                break;

            case R.id.bt_SQL:
                intent = new Intent(functinSelectActivity.this,SQLActivity.class);
                break;

        }
        startActivity(intent);
    }
}
