package com.vise.bledemo.activity;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

import java.util.UUID;


public class AutoConnectActivity extends AppCompatActivity implements View.OnClickListener {
    private String service_uuid = "0000fff1-0000-1000-8000-00805f9b34fb";
    private String notify_uuid = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private String write_uudi = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private String read_uuid = "0000ffe2-0000-1000-8000-00805f9b34fb";
    private String descriptors_uuid = "00002902-0000-1000-8000-00805f9b34fb";
    private String battery_service_uuid = "0000180f-0000-1000-8000-00805f9b34fb";
    private String battery_read_uuid = "00002a19-0000-1000-8000-00805f9b34fb";


    private static final int READ_BATTERY_MSG = 1;
    private static final int READ_SIGNATURE_MSG = 2;
    private static final int READ_NOTIFY_MSG = 3;

    private static final int START_SCAN_MSG = 1;
    private static final int CONNECT_SUCCESS_MSG = 2;
    private static final int RUN_RESTART_MSG = 3;

    private static final String BATTERY_DATA = "BATTERY_DATA";
    private static final String SIGNATURE_DATA = "SIGNATURE_DATA";
    private static final String NOTIFY_DATA = "NOTIFY_DATA";
    private  int scanTimeOutCounter = 0;
    private boolean isTimeThreadBusy = false;

    // private String ble_name = "dylan";
    //private String ble_name = "i3vr controller";
     private String ble_name = "i3vr";


    Button mBt_function;
    ListView mLv_dev;
    TextView mTv_items, mTv_Name, mTv_Mac, mTv_Battery, mTv_Signature, mTv_Notify;
    DeviceMirror mDeviceMirror;
    private BluetoothLeDeviceStore bluetoothLeDeviceStore = new BluetoothLeDeviceStore();
    private AutoConnectAdapter adapter;
    BluetoothLeDevice mDevice;
    private int connectNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_connect);
        widget_init();
        Ble_config_init();
        if (bleHandler != null) {
            Message message = new Message();
            message.what = START_SCAN_MSG;
            bleHandler.sendMessage(message);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("ble_Status=", "onDestroy" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        if (mDevice != null) {
            if (ViseBle.getInstance().isConnect(mDevice)) {
                disconnectDevice(mDevice);
            }
        }
        stop_scan();
        bt_clear();
        //finish();
        System.exit(0);
        super.onDestroy();

    }

    @Override
    protected void onStop() {
        Log.d("ble_Status=", "onStop" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d("ble_Status=", "onPause" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        super.onPause();
    }

    private void widget_init() {
        mLv_dev = findViewById(R.id.lv_dev);
        mTv_items = findViewById(R.id.tv_items2);
        mTv_Name = findViewById(R.id.tv_connectDeviceName);
        mTv_Mac = findViewById(R.id.tv_connectMac);
        mTv_Battery = findViewById(R.id.tv_connectBattery);
        mTv_Signature = findViewById(R.id.tv_connectSignature);
        mTv_Notify = findViewById(R.id.tv_connectNotify);
        mBt_function = findViewById(R.id.bt_test);
        mBt_function.setOnClickListener(this);
        adapter = new AutoConnectAdapter(this);
        mLv_dev.setAdapter(adapter);
    }

    private void Ble_config_init() {
        ViseBle.config()
                .setScanTimeout(10000)//扫描超时时间
                .setConnectTimeout(10000)//连接超时时间
                .setOperateTimeout(5 * 1000)//设置数据操作超时时间
                .setConnectRetryCount(3)//设置连接失败重试次数
                .setConnectRetryInterval(500)//设置连接失败重试间隔时间
                .setOperateRetryCount(3)//设置数据操作失败重试次数
                .setOperateRetryInterval(500)//设置数据操作失败重试间隔时间
                .setMaxConnectCount(6);//设置最大连接设备数量
        ViseBle.getInstance().init(this);//蓝牙信息初始化，全局唯一，必须在应用初始化时调用
    }

    private void updateItemCount(int count) {
        mTv_items.setText(getString(R.string.formatter_item_count, String.valueOf(count)));
    }

    private void start_scan() {
        Log.d("ble_Status=", "start scan " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AutoConnectActivity.this, "start scan", Toast.LENGTH_SHORT).show();
            }
        });

        ViseBle.getInstance().startScan(new ScanCallback(new IScanCallback() {
            @Override
            public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
                //  Log.d("ble_Status", "scan device name: " + bluetoothLeDevice.getName()+"__" + bluetoothLeDevice.getAddress());
                String name = bluetoothLeDevice.getName();
                if (name != null) {
                    if (bluetoothLeDevice.getName().equalsIgnoreCase(ble_name)) {
                        //  Log.d("ble_Status", "add device name: " + bluetoothLeDevice.getName() + bluetoothLeDevice.getAddress());
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
                }
            }

            @Override
            public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {
                scanTimeOutCounter = 0;
                Log.d("ble_Status=", "scan finish" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                if (adapter.getCount() != 0) {
                    connect_device_period();
                } else {
                    Log.d("ble_Status=", "no correct device to connect" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    if (bleHandler != null) {
                        Message message = new Message();
                        message.what = RUN_RESTART_MSG;
                        bleHandler.sendMessage(message);
                   }
                }
            }

            @Override
            public void onScanTimeout() {
                Log.d("ble_Status=", "scan timeOut" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AutoConnectActivity.this, "Scan Timeout", Toast.LENGTH_SHORT).show();
                    }
                });
                scanTimeOutCounter++;
                if(scanTimeOutCounter > 5){
                    scanTimeOutCounter = 0;
                    if(ViseBle.getInstance().getBluetoothAdapter().isEnabled()){
                        ViseBle.getInstance().getBluetoothAdapter().disable();
                        Log.d("ble_Status=", "disable Bluetooth adapter......" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    }
                }
                if (bleHandler != null) {
                    ViseBle.getInstance().clear();
                    Message message = new Message();
                    message.what = RUN_RESTART_MSG;
                    bleHandler.sendMessage(message);
                }
            }
        }));
    }

    private void stop_scan() {
        Log.d("ble_Status=", "stop_scan" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        ViseBle.getInstance().stopScan(perScanCallback);
    }

    private ScanCallback perScanCallback = new ScanCallback(new IScanCallback() {
        @Override
        public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
        }

        @Override
        public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {
        }

        @Override
        public void onScanTimeout() {
        }
    });

    private void bt_readData_init(DeviceMirror deviceMirror, String serviceUUID, String readUUID) {
        Log.d("ble_Status=", "read data init" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
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
                Bundle bundle = new Bundle();
                if (bluetoothGattChannel.getCharacteristicUUID().equals(UUID.fromString(battery_read_uuid))) {
                    message.what = READ_BATTERY_MSG;
                    bundle.putByteArray(BATTERY_DATA, data);
                } else {
                    message.what = READ_SIGNATURE_MSG;
                    bundle.putByteArray(SIGNATURE_DATA, data);
                }
                message.setData(bundle);
                if (dataHandler != null) {
                    dataHandler.sendMessage(message);
                }
            }

            @Override
            public void onFailure(BleException exception) {
                Log.d("ble_Status=", "read failure" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                if (bleHandler != null) {
                    Message message = new Message();
                    message.what = RUN_RESTART_MSG;
                    bleHandler.sendMessage(message);
                }
            }
        }, bluetoothGattChannel);
        deviceMirror.readData();
    }

    private void bt_notifyData_init(final DeviceMirror deviceMirror) {
        Log.d("ble_Status=", "notify init" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
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
                deviceMirror.setNotifyListener(bluetoothGattChannel.getGattInfoKey(), new IBleCallback() {
                    @Override
                    public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                       // String str_notify = HexUtil.encodeHexStr(data);
                        Log.d("ble_Status=", "notify listener" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                        Message message = new Message();
                        message.what = READ_NOTIFY_MSG;
                        Bundle bundle = new Bundle();
                        bundle.putByteArray(NOTIFY_DATA, data);
                        message.setData(bundle);
                        dataHandler.sendMessage(message);
                        deviceMirror.unregisterNotify(false);
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        if (bleHandler != null) {
                            Message message = new Message();
                            message.what = RUN_RESTART_MSG;
                            bleHandler.sendMessage(message);
                        }
                    }
                });
            }

            @Override
            public void onFailure(BleException exception) {
                Log.d("ble_Status=", "notify init fail" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                if (bleHandler != null) {
                    Message message = new Message();
                    message.what = RUN_RESTART_MSG;
                    bleHandler.sendMessage(message);
                }
            }
        }, bluetoothGattChannel);
    }

    private void bt_readData(DeviceMirror deviceMirror, String serviceUUID, String readUUID) {
        if (deviceMirror != null) {
            bt_readData_init(deviceMirror, serviceUUID, readUUID);
        }
    }

    private int bt_readBatteryEnergy() {
        int battery = 0;
        Log.d("ble_Status=", "start read battery data" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        bt_readData(mDeviceMirror, battery_service_uuid, battery_read_uuid);
        return battery;
    }

    private int bt_readSignatureData() {
        int battery = 0;
        Log.d("ble_Status=", "start read signature data" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        bt_readData(mDeviceMirror, service_uuid, read_uuid);
        return battery;
    }

    private void connectDevice(BluetoothLeDevice cDevice) {
        Log.d("ble_Status=", "start connect devict" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        ViseBle.getInstance().connect(cDevice, new IConnectCallback() {
            @Override
            public void onConnectSuccess(DeviceMirror deviceMirror) {
                Log.d("ble_Status=", "connect success" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                // displayGattServices(deviceMirror.getBluetoothGatt().getServices());
                mDeviceMirror = deviceMirror;
                // bt_writeData_init(mDeviceMirror);
                 bt_notifyData_init(mDeviceMirror);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((adapter != null) && (bluetoothLeDeviceStore != null)) {
                            adapter.setListAll(bluetoothLeDeviceStore.getDeviceList());
                            updateItemCount(adapter.getCount());
                        }
                    }
                });
                if (bleHandler != null) {
                    Message message = new Message();
                    message.what = CONNECT_SUCCESS_MSG;
                    bleHandler.sendMessage(message);
                }
            }

            @Override
            public void onConnectFailure(BleException exception) {
                Log.d("ble_Status=", "connect fail" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                if (bleHandler != null) {
                    Message message = new Message();
                    message.what = RUN_RESTART_MSG;
                    bleHandler.sendMessage(message);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AutoConnectActivity.this, "connect failure", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onDisconnect(boolean isActive) {
                Log.d("ble_Status=", "disconnect" + "[" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                if (bleHandler != null) {
                    Message message = new Message();
                    message.what = RUN_RESTART_MSG;
                    bleHandler.sendMessage(message);
                }
            }
        });
    }
    private void disconnectDevice(BluetoothLeDevice cDdvice) {
        Log.d("ble_Status=", "disconnect device"  + cDdvice.getAddress() + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        //ViseBle.getInstance().disconnect(cDdvice);
        ViseBle.getInstance().disconnect();
    }

    private void bt_clear() {
        Log.d("ble_Status=", "bt clear" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        ViseBle.getInstance().clear();
    }

    private void connect_device_period() {
        int deviceNum = adapter.getCount();
        if (deviceNum != 0) {
            if (connectNum >= deviceNum) {
                connectNum = 0;
            }
            mDevice = (BluetoothLeDevice) adapter.getItem(connectNum);
            Log.d("ble_Status=", "start connect to " + mDevice.getAddress() + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AutoConnectActivity.this, " start connect " + mDevice.getAddress(), Toast.LENGTH_SHORT).show();
                }
            });
            connectDevice(mDevice);
            connectNum++;
        }
    }

    private void updateConnectInfoUi(final boolean mode, final DeviceMirror mDeviceMirror, final String signatureData, final byte[] batteryData, final String notifyData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mode) {
                    if (mDeviceMirror != null) {
                        mTv_Name.setText(String.format("Name: %s", mDeviceMirror.getBluetoothLeDevice().getName()));
                        mTv_Mac.setText(String.format("Mac: %s   RSSI: %ddB", mDeviceMirror.getBluetoothLeDevice().getAddress(), mDeviceMirror.getBluetoothLeDevice().getRssi()));
                        Log.d("ble_Status=", "set text Name and Mac" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    }
                    if (signatureData != null) {
                        mTv_Signature.setText(String.format("Signature: %s", signatureData));
                        Log.d("ble_Status=", "set text signature!" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    }
                    if (batteryData != null) {
                        mTv_Battery.setText(String.format("Battery: %s%%", batteryData[0]));
                        Log.d("ble_Status=", "set text battery!" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");

                    }
                    if(notifyData != null){
                        mTv_Notify.setText(String.format("Notify: %s", notifyData));
                        Log.d("ble_Status=", "set text notify2!" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    }
                } else {
                    clearUpdateConnectInfo();
                    Log.d("ble_Status=", "clear info window " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                }
            }
        });
    }

    private void clearUpdateConnectInfo() {
        Log.d("ble_Status=", "clearUpdateConnectInfo" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        mTv_Name.setText("Name:");
        mTv_Mac.setText("Mac:");
        mTv_Signature.setText("Signature:");
        mTv_Battery.setText("Battery:");
        mTv_Notify.setText("Notify:");
    }

    private Handler dataHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case READ_BATTERY_MSG:
                    Log.d("ble_Status=", "READ_BATTERY_MSG" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    byte[] readBatteryData;
                    Bundle bundleBattery = message.getData();
                    readBatteryData = bundleBattery.getByteArray(BATTERY_DATA);
                    if(readBatteryData == null){
                        return false;
                    }
                    if ((readBatteryData[0] >= 0) && (readBatteryData[0] <= 100)) {
                        Log.d("ble_Status=", "battery data: " + readBatteryData[0] + "%" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                        updateConnectInfoUi(true, null, null, readBatteryData, null);

                    }
                    mDeviceMirror.registerNotify(true);
                    //更新UI
                    //启动定时器1
                    break;

                case READ_SIGNATURE_MSG:
                    Log.d("ble_Status=", "READ_SIGNATURE_MSG" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    byte[] readSignatureData;
                    Bundle bundleSignature = message.getData();
                    readSignatureData = bundleSignature.getByteArray(SIGNATURE_DATA);
                    if (readSignatureData != null) {
                        String str_read = HexUtil.encodeHexStr(readSignatureData);
                        Log.d("ble_Status=", " signature data[ " + "len:" + readSignatureData.length + "  Data:" + str_read + " ]");
                        updateConnectInfoUi(true, null, str_read, null, null);
                    }
                    bt_readBatteryEnergy();
                    //更新UI
                    //读battery data;
                    break;

                case READ_NOTIFY_MSG:
                    Log.d("ble_Status", "READ_NOTIFY_MSG" + "[" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    byte[] readNotifyData;
                    Bundle bundleNotify = message.getData();
                    readNotifyData = bundleNotify.getByteArray(NOTIFY_DATA);
                    if (readNotifyData != null) {
                        String str_notify = HexUtil.encodeHexStr(readNotifyData);
                        Log.d("ble_Status=", " notify data[ " + "len:" + readNotifyData.length + "  Data:" + str_notify + " ]");
                        updateConnectInfoUi(true, null, null, null, str_notify);
                    }
                    Message message1 = new Message();
                    message1.what = RUN_RESTART_MSG;
                    message1.arg1 = READ_NOTIFY_MSG;
                    bleHandler.sendMessage(message1);
                    break;
            }
            return true;
        }
    });

    private Handler bleHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case START_SCAN_MSG:
                    isTimeThreadBusy = false;
                    Log.d("ble_Status", "START_SCAN_MSG" + "[" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    if (mDevice != null) {
                        if (ViseBle.getInstance().isConnect(mDevice)) {
                            Log.d("ble_Status", "START_SCAN_MSG_onDisconnect" + "[" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                            disconnectDevice(mDevice);
                            return true;
                        }
                    }
                    stop_scan();
                    bluetoothLeDeviceStore.clear();
                    adapter.clear();
                    updateConnectInfoUi(false, null, null, null, null);
                    start_scan();
                    // 停止当前扫描（如果有）
                    // 清空UI;
                    // 断开蓝牙;
                    // 开始扫描
                    break;

                case CONNECT_SUCCESS_MSG:
                    Log.d("ble_Status", "CONNECT_SUCCESS_MSG" + "[" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    updateConnectInfoUi(true, mDeviceMirror, null, null,null);
                    bt_readSignatureData();
                    break;

                case RUN_RESTART_MSG:
                    if(!ViseBle.getInstance().getBluetoothAdapter().isEnabled()){
                        Log.d("ble_Status", "enable bluetooth adapter" + "[" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                        ViseBle.getInstance().getBluetoothAdapter().enable();
                        new Thread(new ThreadDelay()).start();
                        return true;
                    }
                    Log.d("ble_Status", "RUN_RESTART_MSG" + "[" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    if (isTimeThreadBusy) {
                        Log.d("ble_Status", "isTimeThreadBusy" + "[" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                        return true;
                    } else {
                        isTimeThreadBusy = true;
                        new Thread(new ThreadTime()).start();
                    }
                    if (message.arg1 == READ_BATTERY_MSG) {
                        Log.d("ble_Status", "RUN_RESTART_MSG at READ_BATTERY_MSG " + "[" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    }
                    // 启动定时器1
            }
            return true;
        }
    });

    @Override
    public void onClick(View view) {
        Message message = new Message();
        switch (view.getId()) {
            case R.id.bt_test:
                if(ViseBle.getInstance().getBluetoothAdapter().isEnabled()){
                    Toast.makeText(AutoConnectActivity.this, " close ble", Toast.LENGTH_SHORT).show();
                    ViseBle.getInstance().getBluetoothAdapter().disable();
                }else{
                    Toast.makeText(AutoConnectActivity.this, " open ble", Toast.LENGTH_SHORT).show();
                    ViseBle.getInstance().getBluetoothAdapter().enable();

                }

//                if(bleHandler != null){
//                    message.what = START_SCAN_MSG;
//                    bleHandler.sendMessage(message);
//                }
                //ViseBle.getInstance().disconnect();
//                if(timeThread != null){
//                    timeThread.start();
//                }

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



    class ThreadTime implements Runnable {
        @Override
        public void run() {
            // while (true){
            try {
                Log.d("ble_Status", "Thread_Time_start" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                Thread.sleep(2000);
                if (bleHandler != null) {
                    Message message = new Message();
                    message.what = START_SCAN_MSG;
                    bleHandler.sendMessage(message);
                }
                Log.d("ble_Status", "ThreadTime timeout" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            } catch (Exception e) {
                Log.d("ble_Status", e.getStackTrace().toString() + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            }
            // }
        }
    }

    class ThreadDelay implements Runnable {
        @Override
        public void run() {
            // while (true){
            try {
                Log.d("ble_Status", "ThreadDelay" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                Thread.sleep(3000);
                if (bleHandler != null) {
                    Message message = new Message();
                    message.what = RUN_RESTART_MSG;
                    bleHandler.sendMessage(message);
                }
            } catch (Exception e) {
                Log.d("ble_Status", e.getStackTrace().toString() + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            }
            // }
        }
    }

    public class MyService extends Service{

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }


}
