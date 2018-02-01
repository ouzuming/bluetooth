package com.vise.bledemo.activity;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import com.vise.bledemo.R;

import java.io.File;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener{
    Button mbt_videoPlay, mbt_videoStop, mbt_videoPause;
    VideoView videoView;
    private static final String TAG = "VIDEO";
    private static final String videoName = "video.mp4";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        widgetInit();
        videoViewInit();
    }

    private void videoViewInit() {
        videoView = findViewById(R.id.vv_video);
        File file = new File(Environment.getExternalStorageDirectory(),videoName);
        if(!file.exists()){
            Log.d(TAG, "file is not exists " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            return;
        }
        Log.d(TAG, "file inti path " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        videoView.setVideoPath(file.getPath());
    }

    private void widgetInit() {
        mbt_videoPlay = findViewById(R.id.bt_videoPlay);
        mbt_videoPause = findViewById(R.id.bt_videoPause);
        mbt_videoStop = findViewById(R.id.bt_videoStop);
        mbt_videoPlay.setOnClickListener(this);
        mbt_videoPause.setOnClickListener(this);
        mbt_videoStop.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_videoPlay:
                Log.d(TAG, "bt_videoPlay " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                videoPlay();
                break;
            case R.id.bt_videoPause:
                Log.d(TAG, "bt_videoPause " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                videoPause();
                break;
            case R.id.bt_videoStop:
                Log.d(TAG, "bt_videoStop " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                videoStop();
                break;
        }
    }

    private void videoStop() {
        videoView.suspend();
    }

    private void videoPause() {
        videoView.pause();
    }

    private void videoPlay() {
        videoView.start();
    }
}
