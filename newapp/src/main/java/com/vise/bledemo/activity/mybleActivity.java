package com.vise.bledemo.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.scan.IScanCallback;
import com.vise.baseble.callback.scan.ScanCallback;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.BluetoothLeDeviceStore;
import com.vise.bledemo.R;
import com.vise.bledemo.adapter.DeviceAdapter;

public class mybleActivity extends AppCompatActivity implements View.OnClickListener {
    private String ble_name = "i3vr";
    private Button mbt_scan;
    private Button mbt_stop;
    private ListView mlv_scan;
    private TextView mtv_items;
    private BluetoothLeDeviceStore bluetoothLeDeviceStore = new BluetoothLeDeviceStore();
    private DeviceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myble);
        widget_init();
        Ble_config_init();
    }

    private void Ble_config_init(){
        ViseBle.config()
                .setScanTimeout(10000)
                .setConnectTimeout(10*1000)
                .setOperateRetryCount(3)
                .setConnectRetryInterval(1000)
                .setOperateRetryCount(3)
                .setOperateRetryInterval(1000)
                .setMaxConnectCount(3);
        ViseBle.getInstance().init(this);
    }
    private void widget_init(){
        mbt_scan = findViewById(R.id.bt_scan);
        mbt_stop = findViewById(R.id.bt_stop);
        mbt_scan.setOnClickListener(this);
        mbt_stop.setOnClickListener(this);
        mlv_scan = findViewById(R.id.lv_scanResut);
        mtv_items = findViewById(R.id.tv_items);
        adapter = new DeviceAdapter(this);
        mlv_scan.setAdapter(adapter);
        mlv_scan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("ble_Status=","onItemClick"+" item:"+i);
                BluetoothLeDevice device = (BluetoothLeDevice) adapter.getItem(i);
                if(device == null) return;
                Intent intent = new Intent(mybleActivity.this, ConnectActivity.class);
                intent.putExtra(ConnectActivity.CONNECT_DEVICE,device);
                startActivity(intent);
            }
        });
    }
    private void updateItemCount(int count){
        mtv_items.setText(getString(R.string.formatter_item_count, String.valueOf(count)));
    }

    private  void start_scan(){
        ViseBle.getInstance().startScan(new ScanCallback(new IScanCallback() {
            @Override
            public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
                Log.d("ble_scan","device found:"+bluetoothLeDevice.getName()+bluetoothLeDevice.getAddress());
                if(!bluetoothLeDevice.getName().equalsIgnoreCase(ble_name)){
                    Log.d("ble_scan","add device name: "+bluetoothLeDevice.getName()+bluetoothLeDevice.getAddress());
                    return;
                }
                bluetoothLeDeviceStore.addDevice(bluetoothLeDevice);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if((adapter != null) && (bluetoothLeDeviceStore != null)){
                            adapter.setListAll(bluetoothLeDeviceStore.getDeviceList());
                            updateItemCount(adapter.getCount());
                        }
                    }
                });
            }
            @Override
            public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {
                Toast.makeText(mybleActivity.this,"onScanFinish",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onScanTimeout() {
                Toast.makeText(mybleActivity.this,"onScanTimeout",Toast.LENGTH_SHORT).show();
            }
        }));
    }
    private  void stop_scan(){
        ViseBle.getInstance().stopScan(perScanCallback);
    }
    private ScanCallback perScanCallback = new ScanCallback(new IScanCallback() {
        @Override
        public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
            Toast.makeText(mybleActivity.this,"STOP_onDeviceFound",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {
            Toast.makeText(mybleActivity.this,"STOP_onScanFinish",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onScanTimeout() {
            Toast.makeText(mybleActivity.this,"STOP_onScanTimeout",Toast.LENGTH_SHORT).show();
        }
    });

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_scan:
                Toast.makeText(mybleActivity.this,"start scan",Toast.LENGTH_SHORT).show();
                start_scan();
                break;
            case R.id.bt_stop:
                Toast.makeText(mybleActivity.this,"stop scan",Toast.LENGTH_SHORT).show();
                stop_scan();
                break;
        }
    }
}
