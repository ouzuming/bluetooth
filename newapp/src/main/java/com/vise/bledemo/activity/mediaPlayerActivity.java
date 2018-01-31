package com.vise.bledemo.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vise.bledemo.R;

import java.io.File;
import java.io.IOException;

public class mediaPlayerActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn_mediaPlay,btn_mediaPause,btn_mediaStop;
    TextView tv_mediaStatus;
    MediaPlayer mediaPlayer = new MediaPlayer();
    private static final String musicName = "music.mp3";
    private static final String TAG = "mediaPlay";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        widgetInit();

        if(ContextCompat.checkSelfPermission(mediaPlayerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "apply for permission" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            ActivityCompat.requestPermissions(mediaPlayerActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE },1);

        }else {
            Log.d(TAG, "start init mediaPlayer" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            mediaPlayerInit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                Log.d(TAG, "get permission" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mediaPlayerInit();
                }else {
                    Log.d(TAG, "permission refuse" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    Toast.makeText(mediaPlayerActivity.this,"permission refuse",Toast.LENGTH_SHORT).show();
                }
                break;

                default:break;
        }
    }

    private void mediaPlayerInit() {
        mediaReset();
        File file = new File(Environment.getExternalStorageDirectory(),musicName);
        try {
            Log.d(TAG, "set data sourse" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.d(TAG, "set data sourse erroe: "+ e + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            e.printStackTrace();
        }
    }

    private void widgetInit() {
        btn_mediaPlay = findViewById(R.id.mediaPlay);
        btn_mediaPause = findViewById(R.id.mediaPause);
        btn_mediaStop = findViewById(R.id.mediaStop);
        tv_mediaStatus = findViewById(R.id.tv_mediaPlayerStatus);
        btn_mediaPlay.setOnClickListener(this);
        btn_mediaPause.setOnClickListener(this);
        btn_mediaStop.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mediaPlay:
                Log.d(TAG, "mediaPlay " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                mediaPlay();
                break;
            case R.id.mediaPause:
                Log.d(TAG, "mediaPause " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                mediaPause();
                break;
            case R.id.mediaStop:
                Log.d(TAG, "mediaStop " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                mediaStop();
                break;

        }
    }

    private void mediaStatus(final String mediaStatus) {
        Log.d(TAG, "set media status " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        tv_mediaStatus.setText(mediaStatus);
    }
    private void mediaReset() {
        Log.d(TAG, "mediaReset " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        mediaPlayer.reset();
    }

    private void mediaStop() {
        Log.d(TAG, "mediaStop " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        mediaPlayer.pause();
        mediaPlayer.stop();
        //mediaPlayerInit();
        mediaStatus("stop");
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mediaPause() {
        if(mediaPlayer.isPlaying()){
            Log.d(TAG, "mediaPause " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
           mediaPlayer.pause();
            mediaStatus("pause");
        }
    }

    private void mediaPlay() {
        if(!mediaPlayer.isPlaying()){
            Log.d(TAG, "mediaPlay " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");

            mediaPlayer.start();
            if(mediaPlayer.isPlaying()){
                mediaStatus("playing");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
