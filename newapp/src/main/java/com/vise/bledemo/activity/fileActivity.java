package com.vise.bledemo.activity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vise.bledemo.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class fileActivity extends AppCompatActivity implements View.OnClickListener,SensorEventListener{
    private static final String TAG = "sensor";
    Context mContext;
    Button btn_write, btn_read, btn_getSensor;
    TextView mtv_sensorInfo;
    private  SensorManager  mSensorManager;
    private  Sensor mAccelerometer;
    public static final float STANDARD_GRAVITY = 9.80665F;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        mContext =this;
       widgetInit();
       mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
       // mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }
    void widgetInit(){
        btn_write = findViewById(R.id.bt_writeFile);
        btn_read = findViewById(R.id.bt_readFile);
        btn_getSensor = findViewById(R.id.bt_getSensor);
        mtv_sensorInfo = findViewById(R.id.tv_Sensor);
        btn_write.setOnClickListener(this);
        btn_read.setOnClickListener(this);
        btn_getSensor.setOnClickListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    private void getSensorList(){
        SensorManager sensorManager  = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensor = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (int i = 0; i < sensor.size(); i++ ){
                Log.d(TAG,"sensor name: "+sensor.get(i).getName());
                Log.d(TAG,"sensor vendor: "+sensor.get(i).getVendor());
                Log.d(TAG,"sensor power: "+sensor.get(i).getPower());
                Log.d(TAG, "sensor resolution: " + sensor.get(i).getResolution());
        }
    }
    private void fileWrite(){
        String FILENAME = "hello_file";
        String fileData = "what is your name";
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(FILENAME,mContext.MODE_PRIVATE);
            fos.write(fileData.getBytes());
            fos.close();
            Log.d("file", "writeFile:"+ fileData+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fileRead(){
        String FILENAME = "hello_file";
        String fileData = "what is your name";
        FileInputStream fos = null;
        final int Len = 1024;
        byte[] readByte = new byte[Len];
        try{
            fos = openFileInput(FILENAME);
           int readLength =  fos.read(readByte);
           byte[] data = new byte[readLength];
           System.arraycopy(readByte,0, data,0, readLength);
           // Log.d("file", "readfile:"+ readByte.toString()+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            Log.d("file", "read length" + readLength + " readfile:"+ new String(data)+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.bt_writeFile:
               fileWrite();
               break;

           case R.id.bt_readFile:
               fileRead();
               break;

           case R.id.bt_getSensor:
               getSensorList();
               break;
       }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "value size:: " + event.values.length);
        final float xValue = event.values[0];
        final float yValue = event.values[1];
        final float zValue = event.values[2];
        Log.d(TAG, "value: " + "x:"+ xValue + "y:"+ yValue+ "z:"+ zValue);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mtv_sensorInfo.setText("x轴：" + xValue + "\n"  + "y轴：" + yValue + "\n"+"z轴：" + zValue);
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
