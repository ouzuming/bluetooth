package com.vise.bledemo.activity;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vise.bledemo.R;

import java.io.File;
import java.io.IOException;

public class mediaPlayerActivity extends AppCompatActivity implements View.OnClickListener  {
    Button btn_mediaPlay,btn_mediaPause,btn_mediaStop;
    TextView tv_mediaStatus ,tv_progressPercent,tv_mediaTime;
    MediaPlayer mediaPlayer = new MediaPlayer();
    ProgressBar musicProgressBar;
    private static final int musicProgressMax = 100;
    private int mediaTotalLength = 0;
    private int currentPosition;
    private int percent;
    private static boolean isPrepare = false;
    private static  boolean musicPlayFlag = false;
    private static final String musicName = "music.mp3";
    private static final String TAG = "mediaPlay";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        widgetInit();
        progressBarInit();
        if(ContextCompat.checkSelfPermission(mediaPlayerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "apply for permission" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            ActivityCompat.requestPermissions(mediaPlayerActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE },1);

        }else {
            Log.d(TAG, "start init mediaPlayer" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            mediaPlayerInit();
        }
    }

    private void progressBarInit() {
        musicProgressBar = findViewById(R.id.pb_musicProgress);
        musicProgressBar.setMax(musicProgressMax);
        musicProgressBar.setProgress(0);
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
         mediaTotalLength = mediaGetDuration();
        Log.d(TAG, "media total length: "+ mediaTotalLength + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d(TAG, "media on prepared: " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                isPrepare = true;
                mediaPlayer.start();
                btn_mediaPlay.setEnabled(false);
                btn_mediaStop.setEnabled(true);
                btn_mediaPause.setEnabled(true);
                mediaStatus("playing");
                startProgressBarThread();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    private void widgetInit() {
        btn_mediaPlay = findViewById(R.id.mediaPlay);
        btn_mediaPause = findViewById(R.id.mediaPause);
        btn_mediaStop = findViewById(R.id.mediaStop);
        tv_mediaStatus = findViewById(R.id.tv_mediaPlayerStatus);
        tv_progressPercent = findViewById(R.id.progressPercent);
        tv_mediaTime = findViewById(R.id.mediaTime);
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
        isPrepare = false;
    }

    private void mediaStop() {
        Log.d(TAG, "mediaStop " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        musicPlayFlag = false;
        mediaPlayer.pause();
        mediaPlayer.stop();
        mediaStatus("stop");
        tv_progressPercent.setText("0%");
        tv_mediaTime.setText("0/0");
        btn_mediaPlay.setEnabled(true);
        btn_mediaStop.setEnabled(false);
        btn_mediaPause.setEnabled(false);
        isPrepare = false;
    }

    private void mediaPause() {
        if(mediaPlayer.isPlaying()){
            Log.d(TAG, "mediaPause " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
           mediaPlayer.pause();
            btn_mediaPlay.setEnabled(true);
            btn_mediaStop.setEnabled(true);
            btn_mediaPause.setEnabled(false);
            mediaStatus("pause");
        }
    }

    private void mediaPlay() {
        Log.d(TAG, "mediaPlay " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        if(!mediaPlayer.isPlaying()){
            if(isPrepare == false){
                try {
                    Log.d(TAG, "prepare "+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                mediaPlayer.start();
                btn_mediaPlay.setEnabled(false);
                btn_mediaStop.setEnabled(true);
                btn_mediaPause.setEnabled(true);
                mediaStatus("playing");
            }
        }
    }

    private void  startProgressBarThread(){
        musicPlayFlag = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (musicPlayFlag){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(mediaPlayer.isPlaying() ){
                        musicProgressBar.setProgress(getCurrentPositionPercent());
                    }
                }
            }
        }).start();
    }
    private int getCurrentPositionPercent() {
        currentPosition = mediaGetCurrentPosition();
        if((currentPosition / 1000) >= (mediaTotalLength / 1000) ){
            Log.d(TAG, "the end of media = " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            musicPlayFlag = false;
            percent = musicProgressMax;
        }else {
            percent = (currentPosition * musicProgressMax) / mediaTotalLength;
        }
       // Log.d(TAG, "currentPosition = " + currentPosition + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_progressPercent.setText(percent+"%");
                tv_mediaTime.setText(currentPosition/1000 + "/" + mediaTotalLength/1000);
            }
        });
        return  percent;
    }
    // uint:ms
    private int mediaGetDuration(){
       return mediaPlayer.getDuration();
    }

    private int mediaGetCurrentPosition(){
        return  mediaPlayer.getCurrentPosition();
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
