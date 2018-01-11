package com.vise.bledemo.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.IBleCallback;
import com.vise.baseble.callback.IConnectCallback;
import com.vise.baseble.callback.scan.IScanCallback;
import com.vise.baseble.callback.scan.ScanCallback;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.BluetoothGattChannel;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.exception.BleException;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.BluetoothLeDeviceStore;
import com.vise.baseble.utils.HexUtil;
import com.vise.bledemo.R;
import com.vise.bledemo.adapter.AutoConnectAdapter;
import com.vise.bledemo.myClass.MyLogClass;
import com.vise.log.config.LogDefaultConfig;

import java.util.UUID;


public class AutoConnectActivity extends AppCompatActivity implements View.OnClickListener {
    private String service_uuid = "0000fff1-0000-1000-8000-00805f9b34fb";
    private String notify_uuid = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private String write_uudi = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private String read_uuid = "0000ffe2-0000-1000-8000-00805f9b34fb";
    private String descriptors_uuid = "00002902-0000-1000-8000-00805f9b34fb";
    private String battery_service_uuid = "0000180f-0000-1000-8000-00805f9b34fb";
    private String battery_read_uuid = "00002a19-0000-1000-8000-00805f9b34fb";

    private static final int UPDATE_TEXT = 1;
    private static final int READ_TEXT = 2;
    private static final int START_TEXT = 3;
    private static final int STOP_TEXT = 4;
    private static final int CONNECTED_TEXT = 5;
    private static final int TIMEOUT_TEXT = 6;
    private static final String BATTERY_DATA = "BATTERY_DATA";
    private static final String SIGNATURE_DATA = "SIGNATURE_DATA";

    private boolean isBatteryRead = false;

    private String ble_name = "i3vr";
    Button mBt_start, mBt_function;
    ListView mLv_dev;
    TextView mTv_items, mTv_Name, mTv_Mac, mTv_Battery, mTv_Signature;
    DeviceMirror mDeviceMirror;
    private BluetoothLeDeviceStore bluetoothLeDeviceStore = new BluetoothLeDeviceStore();
    private AutoConnectAdapter adapter;
    BluetoothLeDevice mDevice;
    Thread timeThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_connect);
        widget_init();
        Ble_config_init();
        timeThread =  new Thread(new ThreadTime());
    }

    @Override
    protected void onDestroy() {
        disconnectDevice(mDevice);
        super.onDestroy();
    }

    private void widget_init() {
        mLv_dev = findViewById(R.id.lv_dev);
        mTv_items = findViewById(R.id.tv_items2);
        mTv_Name = findViewById(R.id.tv_connectDeviceName);
        mTv_Mac = findViewById(R.id.tv_connectMac);
        mTv_Battery = findViewById(R.id.tv_connectBattery);
        mTv_Signature = findViewById(R.id.tv_connectSignature);

        mBt_start = findViewById(R.id.bt_scan2);
        mBt_function = findViewById(R.id.bt_test);
        mBt_start.setOnClickListener(this);
        mBt_function.setOnClickListener(this);
        adapter = new AutoConnectAdapter(this);
        mLv_dev.setAdapter(adapter);
    }

    private void Ble_config_init() {
        ViseBle.config()
                .setScanTimeout(3000)
                .setConnectTimeout(10 * 1000)
                .setOperateRetryCount(3)
                .setConnectRetryInterval(1000)
                .setOperateRetryCount(3)
                .setOperateRetryInterval(1000)
                .setMaxConnectCount(3);
        ViseBle.getInstance().init(this);
    }

    private void updateItemCount(int count) {
        mTv_items.setText(getString(R.string.formatter_item_count, String.valueOf(count)));
    }

    private void start_scan() {
        ViseBle.getInstance().startScan(new ScanCallback(new IScanCallback() {
            @Override
            public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {

                if (!bluetoothLeDevice.getName().equalsIgnoreCase(ble_name)) {
                    return;
                }
               // Log.d("ble_Status", "add device name: " + bluetoothLeDevice.getName() + bluetoothLeDevice.getAddress());
                bluetoothLeDeviceStore.addDevice(bluetoothLeDevice);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((adapter != null) && (bluetoothLeDeviceStore != null)) {
                            adapter.setListAll(bluetoothLeDeviceStore.getDeviceList());
                            updateItemCount(adapter.getCount());
                        }
                    }
                });
            }

            @Override
            public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {
                Toast.makeText(AutoConnectActivity.this, "onScanFinish", Toast.LENGTH_SHORT).show();
                connect_device_period();
            }

            @Override
            public void onScanTimeout() {
                Toast.makeText(AutoConnectActivity.this, "onScanTimeout", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void stop_scan() {
        ViseBle.getInstance().stopScan(perScanCallback);
    }

    private ScanCallback perScanCallback = new ScanCallback(new IScanCallback() {
        @Override
        public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
            Toast.makeText(AutoConnectActivity.this, "STOP_onDeviceFound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {
            Toast.makeText(AutoConnectActivity.this, "STOP_onScanFinish", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onScanTimeout() {
            Toast.makeText(AutoConnectActivity.this, "STOP_onScanTimeout", Toast.LENGTH_SHORT).show();
        }
    });

    private void bt_readData_init(DeviceMirror deviceMirror, String serviceUUID, String readUUID) {
        Log.d("ble_Status=", "read data start init");
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_READ)
                .setServiceUUID(UUID.fromString(serviceUUID))
                .setCharacteristicUUID(UUID.fromString(readUUID))
                .builder();
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Message message = new Message();
                message.what = READ_TEXT;
                Bundle bundle = new Bundle();

                if (bluetoothGattChannel.getCharacteristicUUID().equals(UUID.fromString(battery_read_uuid))) {
                    bundle.putByteArray(BATTERY_DATA,data);
                } else {
                    bundle.putByteArray(SIGNATURE_DATA,data);
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }

            @Override
            public void onFailure(BleException exception) {
                Log.d("ble_Status=", "read init failure!!" + exception);
            }
        }, bluetoothGattChannel);
        deviceMirror.readData();
    }

    private void bt_notifyData_init(final DeviceMirror deviceMirror) {
        Log.d("ble_Status=", "notify  start init"+ Thread.currentThread().getId()+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_INDICATE)
                .setServiceUUID(UUID.fromString(service_uuid))
                .setCharacteristicUUID(UUID.fromString(notify_uuid))
                .setDescriptorUUID(UUID.fromString(descriptors_uuid))
                .builder();
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.d("ble_Status=", "notify success");
                deviceMirror.setNotifyListener(bluetoothGattChannel.getGattInfoKey(), new IBleCallback() {
                    @Override
                    public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                        String str_notify = HexUtil.encodeHexStr(data);
                        Log.d("ble_Status=", "len:" + data.length + "  Data:" + str_notify);
                        Log.d("thread", "setNotifyListener onSuccess  current Thread's name:" + Thread.currentThread().getName());
                    }

                    @Override
                    public void onFailure(BleException exception) {

                    }
                });
            }

            @Override
            public void onFailure(BleException exception) {
                Log.d("ble_Status=", "notify init fail" + exception + Thread.currentThread().getId()+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            }
        }, bluetoothGattChannel);
        //deviceMirror.registerNotify(true);
    }

    private void bt_readData(DeviceMirror deviceMirror, String serviceUUID, String readUUID) {
        if (deviceMirror != null) {
            bt_readData_init(deviceMirror, serviceUUID, readUUID);
        }
    }

    private int bt_readBatteryEnergy() {
        int battery = 0;
        bt_readData(mDeviceMirror, battery_service_uuid, battery_read_uuid);
        return battery;
    }

    private int bt_readSignatureData() {
        int battery = 0;
        bt_readData(mDeviceMirror, service_uuid, read_uuid);
        return battery;
    }

    private void connectDevice(BluetoothLeDevice cDevice) {
        ViseBle.getInstance().connect(cDevice, new IConnectCallback() {
            @Override
            public void onConnectSuccess(DeviceMirror deviceMirror) {

                Log.d("ble_Status=", "connect success"+ " ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                // displayGattServices(deviceMirror.getBluetoothGatt().getServices());
                mDeviceMirror = deviceMirror;
                // bt_writeData_init(mDeviceMirror);
               // bt_notifyData_init(mDeviceMirror);
                if(handler != null){
                    Message message = new Message();
                    message.what = CONNECTED_TEXT;
                    handler.sendMessage(message);
                }
                Log.d("ble_Status=", "onConnectSuccess" + " [" + Thread.currentThread().getId()+"]"+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            }

            @Override
            public void onConnectFailure(BleException exception) {
                Log.d("ble_Status=", "connect fail"+ " ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                Message message = new Message();
                message.what = STOP_TEXT;
                handler.sendMessage(message);

            }

            @Override
            public void onDisconnect(boolean isActive) {
                Log.d("ble_Status=", "disconnect"+ "[" + Thread.currentThread().getId()+"]"+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                Message message = new Message();
                message.what = START_TEXT;
                handler.sendMessage(message);
            }
        });
    }

    private void disconnectDevice(BluetoothLeDevice cDdvice) {
        ViseBle.getInstance().disconnect(cDdvice);
        updateConnectInfoUi(false, null, null,null);
    }

    private void connect_device_period() {
        int deviceNum = adapter.getCount();
        int i = 0;
        Log.d("ble_Status=", "adapter number" + deviceNum+" [" + Thread.currentThread().getId()+"]"+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
        mDevice = (BluetoothLeDevice) adapter.getItem(i);
        connectDevice(mDevice);
    }

    private void updateConnectInfoUi(final boolean mode, final DeviceMirror mDeviceMirror, final String signatureData, final byte[] batteryData) {
        Log.d("ble_Status=", "thread id: "+ Thread.currentThread().getId()+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mode){
                    if (mDeviceMirror != null) {
                        Log.d("ble_Status=", "set text name mac!"+ Thread.currentThread().getId()+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                        mTv_Name.setText(String.format("Name: %s", mDeviceMirror.getBluetoothLeDevice().getName()));
                        mTv_Mac.setText(String.format("Mac: %s   RSSI: %ddB", mDeviceMirror.getBluetoothLeDevice().getAddress(), mDeviceMirror.getBluetoothLeDevice().getRssi()));
                    }
                    if (signatureData != null) {
                        Log.d("ble_Status=", "set text signature!"+ Thread.currentThread().getId()+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                        mTv_Signature.setText(String.format("Signature: %s", signatureData));
                    }
                    if (batteryData != null) {
                        Log.d("ble_Status=", "set text battery!"+ Thread.currentThread().getId()+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                        mTv_Battery.setText(String.format("Battery: %s%%", batteryData[0]));

                    }
                }else {
                    Log.d("ble_Status=", "clear info window!"+ Thread.currentThread().getId()+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                    clearUpdateConnectInfo();
                }
            }
        });
    }

    private void clearUpdateConnectInfo(){
        mTv_Name.setText("Name:");
        mTv_Mac.setText("Mac:");
        mTv_Signature.setText("Signature:");
        mTv_Battery.setText("Battery:");
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            switch (message.what){
                case UPDATE_TEXT:
                    timeThread.stop();
                    Bundle bundle = message.getData();
                    String getStr = bundle.getString("Name");
                    Log.d("handler","handler recceive message:"+ getStr);
                    break;
                case START_TEXT:
                    Log.d("handler","handler START_TEXT" + Thread.currentThread().getId()+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                    start_scan();
                    break;

                case STOP_TEXT:
                    Log.d("handler","handler STOP_TEXT" + Thread.currentThread().getId()+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                    stop_scan();
                    if(mDevice != null){
                        disconnectDevice(mDevice);
                    }
                    adapter.clear();
                    isBatteryRead = false;
                    break;

                case READ_TEXT:
                    Log.d("handler","handler READ_TEXT" + Thread.currentThread().getId()+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                    byte[] readData;
                    Bundle bundle1 = message.getData();
                    readData= bundle1.getByteArray(BATTERY_DATA);
                    if(readData != null){
                        Log.d("handler=","handler battery data[ "+readData[0]+"% ]");
                        updateConnectInfoUi(true, null, null, readData);
                    }
                    readData= bundle1.getByteArray(SIGNATURE_DATA);
                    if(readData != null){
                        String str_read =  HexUtil.encodeHexStr(readData);
                        Log.d("handler=","handler signature data[ " + "len:"+readData.length + "  Data:" + str_read + " ]");
                        updateConnectInfoUi(true, null, str_read, null);
                    }

                    if(!isBatteryRead){
                        isBatteryRead = true;
                        bt_readBatteryEnergy();
                        timeThread.start();
                    }
                    break;

                case CONNECTED_TEXT:
                    Log.d("handler","handler CONNECTED_TEXT" + Thread.currentThread().getId()+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                    updateConnectInfoUi(true, mDeviceMirror, null, null);
                    bt_readSignatureData();
                    break;
                case TIMEOUT_TEXT:
                    Log.d("handler","handler TIMEOUT_TEXT" + Thread.currentThread().getId()+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                    if(mDevice != null){
                        disconnectDevice(mDevice);
                    }
                    start_scan();
                    break;
            }
            return false;
        }
    });

    @Override
    public void onClick(View view) {
        Message message = new Message();
        switch (view.getId()) {
            case R.id.bt_scan2:
                if(mBt_start.getText().equals("START")){
                    message.what = START_TEXT;
                    handler.sendMessage(message);
                    mBt_start.setText("STOP");

                }else{
                    message.what = STOP_TEXT;
                    handler.sendMessage(message);
                    mBt_start.setText("START");
                }
                break;

            case R.id.bt_test:
                timeThread.start();
               // message.what = UPDATE_TEXT;
               // handler.sendMessage(message);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.d("ble_Status","send message");
//                        String putStr = "test runing...";
//                        Message message = new Message();
//                        Bundle bundle = new Bundle();
//                        bundle.putString("Name",putStr);
//                        message.setData(bundle);
//                        message.what = UPDATE_TEXT;
//                        handler.sendMessage(message);
//                    }
//                }).start();

                break;
            default:
               break;
        }
    }
    class ThreadTime implements Runnable{
        @Override
        public void run() {
           // while (true){
                try{
                    Thread.sleep(2000);
                    Message message = new Message();
                    message.what = TIMEOUT_TEXT;
                    handler.sendMessage(message);
                    Log.d("handler","timeout" + " ["+ Thread.currentThread().getId()+"]"+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                }catch (Exception e){
                    Log.d("handler",e.getStackTrace().toString() + " ["+ Thread.currentThread().getId()+"]"+" ["+ Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                }
           // }
        }
    }
}
