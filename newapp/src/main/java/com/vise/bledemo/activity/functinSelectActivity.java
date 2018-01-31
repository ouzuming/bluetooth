package com.vise.bledemo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.vise.bledemo.R;
public class functinSelectActivity extends AppCompatActivity implements View.OnClickListener {
    Button mbt_atuo, mbt_manual, mbt_file, mbt_photo, mbt_mediaPlayer;
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
        mbt_atuo.setOnClickListener(this);
        mbt_manual.setOnClickListener(this);
        mbt_file.setOnClickListener(this);
        mbt_photo.setOnClickListener(this);
        mbt_mediaPlayer.setOnClickListener(this);
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

        }
        startActivity(intent);
    }
}
