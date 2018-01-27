package com.vise.bledemo.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.IBleCallback;
import com.vise.baseble.callback.IConnectCallback;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.BluetoothGattChannel;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.exception.BleException;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.resolver.GattAttributeResolver;
import com.vise.baseble.utils.HexUtil;
import com.vise.bledemo.R;
import com.vise.bledemo.common.BluetoothDeviceManager;
import com.vise.bledemo.myClass.MyFileHandle;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import static com.vise.baseble.utils.HexUtil.encodeHexStr;

public class ConnectActivity extends AppCompatActivity implements View.OnClickListener{
    private String service_uuid = "0000fff1-0000-1000-8000-00805f9b34fb";
    private String notify_uuid = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private String write_uudi = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private String read_uuid = "0000ffe2-0000-1000-8000-00805f9b34fb";
    private String descriptors_uuid = "00002902-0000-1000-8000-00805f9b34fb";
    private String battery_service_uuid ="0000180f-0000-1000-8000-00805f9b34fb";
    private String battery_read_uuid ="00002a19-0000-1000-8000-00805f9b34fb";
    private static final String LIST_NAME = "NAME";
    private static final String LIST_UUID = "UUID";
    public static final String WRITE_CHARACTERISTI_UUID_KEY = "write_uuid_key";
    public static final String NOTIFY_CHARACTERISTIC_UUID_KEY = "notify_uuid_key";
    public static final String WRITE_DATA_KEY = "write_data_key";

    private static final int CONNECT_SUCCESS_MSG = 0;
    private static final int DISCONNECT_MSG = 1;
    private static final int FILE_DATA_MSG = 2;
    private static final String FILEDATAKEY = "FILEDATA";

    private  byte[] write_data = {0x01,0x02,0x03,0x04,0x05};
    public static final String CONNECT_DEVICE = "connect_device";
    private BluetoothLeDevice mDevice;
    private TextView mTv_conInfo;
    private StringBuilder sb_btInfo;
    private Button mBt_connect,mBt_write,mBt_read,mBt_notify,mBt_battery,mBt_browse;
    private EditText mEt_path;
    private List<BluetoothGattService> mGattServices = new ArrayList<>();
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    private DeviceMirror mDeviceMirror;
    private SimpleExpandableListAdapter simpleExpandableListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        widget_init();
       MyFileHandle.verifyStoragePermissions(this);
    }

    @Override
    protected void onDestroy() {
        System.exit(0);
        super.onDestroy();
    }

    private void widget_init(){
        mDevice = getIntent().getParcelableExtra(CONNECT_DEVICE);
        mTv_conInfo = findViewById(R.id.tv_conInfo);
        sb_btInfo = new StringBuilder();
        mEt_path = findViewById(R.id.et_path);
        mBt_connect = findViewById(R.id.bt_connect);
        mBt_write = findViewById(R.id.bt_write);
        mBt_read = findViewById(R.id.bt_read);
        mBt_notify = findViewById(R.id.bt_notify);
        mBt_battery = findViewById(R.id.bt_battery);
        mBt_browse = findViewById(R.id.bt_browse);
        mBt_connect.setOnClickListener(this);
        mBt_write.setOnClickListener(this);
        mBt_read.setOnClickListener(this);
        mBt_notify.setOnClickListener(this);
        mBt_battery.setOnClickListener(this);
        mBt_browse.setOnClickListener(this);
    }

    private void showConnectInfo(BluetoothLeDevice cDevice){
        Log.d("ble_Status=", "show connect info" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        sb_btInfo.append("name: ").append(cDevice.getName()).append("\n")
                .append("MAC：").append(cDevice.getAddress()).append("\n")
                .append("rssi: ").append(cDevice.getRssi()).append("\n");
        mTv_conInfo.setText(sb_btInfo.toString());
    }

    private void bt_writeData_init(DeviceMirror deviceMirror){
        Log.d("ble_Status=","write data start init");
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_WRITE)
                .setServiceUUID(UUID.fromString(service_uuid))
                .setCharacteristicUUID(UUID.fromString(write_uudi))
               // .setDescriptorUUID(UUID.fromString(descriptors_uuid))
                .builder();
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.d("ble_Status=","write success!!");
               // String str_write =  HexUtil.encodeHexStr(data);
               // Log.d("ble_Status=","len:"+data.length+"  Data:"+ str_write);
            }

            @Override
            public void onFailure(BleException exception) {
                Log.d("ble_Status=","write  init failure!!"+exception);
            }
        }, bluetoothGattChannel);
    }
    private void bt_readData_init(DeviceMirror deviceMirror, String serviceUUID, String readUUID){
        Log.d("ble_Status=","read data start init");
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_READ)
                .setServiceUUID(UUID.fromString(serviceUUID))
                .setCharacteristicUUID(UUID.fromString(readUUID))
                .builder();
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                if(bluetoothGattChannel.getCharacteristicUUID().equals(UUID.fromString(battery_read_uuid))){
                    Log.d("ble_Status=","battery data[ "+data[0]+"% ]");
                }else {
                    String str_read =  HexUtil.encodeHexStr(data);
                    Log.d("ble_Status=","signature data[ " + "len:"+data.length + "  Data:"+ str_read+" ]");
                }
            }
            @Override
            public void onFailure(BleException exception) {
                Log.d("ble_Status=","read init failure!!"+exception);
            }
        }, bluetoothGattChannel);
        deviceMirror.readData();
    }

    private void bt_notifyData_init(final DeviceMirror deviceMirror){
        Log.d("ble_Status=","notify  start init");
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
                Log.d("ble_Status=","notify success");
                deviceMirror.setNotifyListener(bluetoothGattChannel.getGattInfoKey(), new IBleCallback() {
                    @Override
                    public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                        String str_notify =  HexUtil.encodeHexStr(data);
                        Log.d("ble_Status=","len:"+data.length+"  Data:"+ str_notify);
                        Log.d("thread", "setNotifyListener onSuccess  current Thread's name:" +  Thread.currentThread().getName());
                    }

                    @Override
                    public void onFailure(BleException exception) {

                    }
                });
            }

            @Override
            public void onFailure(BleException exception) {
                Log.d("ble_Status=","notify init fail"+exception);
            }
        }, bluetoothGattChannel);
       // deviceMirror.registerNotify(true);
    }

    private void bt_writeData(DeviceMirror deviceMirror,byte[] data){
        if(deviceMirror != null) {
            Log.d("ble_Status", "bt write data" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            deviceMirror.writeData(data);
           // BluetoothDeviceManager.getInstance().write(mDevice,data);
        }
    }
    private void bt_sendFileData(final DeviceMirror deviceMirror, final byte[] data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int btSendLenth = 20;
                int writePackage = data.length / btSendLenth;
                int dataAddr = 0;
                List<byte[]> fileList = new ArrayList<byte[]>();

                if(deviceMirror != null){
                    Log.d("ble_Status", "bt write file data" + HexUtil.encodeHexStr(data) + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    for(int i = 0; i< writePackage; i++){
                        byte[] fileByte = new byte[btSendLenth];
                       System.arraycopy(data,dataAddr,fileByte,0,btSendLenth);
                       // Log.d("ble_Status","dataAddr" + dataAddr + "   copy_data:  " + HexUtil.encodeHexStr(fileByte) + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                       dataAddr += btSendLenth;
                       fileList.add(fileByte);
//                        for(int j = 0; j< fileList.size(); j++){
//                            Log.d("ble_Status", "fileList_2:  "+ HexUtil.encodeHexStr(fileList.get(j)) + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
//                        }
                    }

                    if((data.length % btSendLenth) > 0){
                        byte[] fileByte = new byte[data.length % btSendLenth];
                        System.arraycopy(data,dataAddr,fileByte,0,data.length % btSendLenth);
                        fileList.add(fileByte);
                        writePackage++;
                    }
                    Log.d("ble_Status", "writePackage= " + writePackage + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    for(int i = 0; i< writePackage; i++){
                        Log.d("ble_Status", "len:"+ fileList.get(i).length +"  write data:  "+ HexUtil.encodeHexStr(fileList.get(i)) + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                        deviceMirror.writeData(fileList.get(i));
                        try {
                            Log.d("ble_Status", "thread sleep  " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Log.d("ble_Status", "thread sleep catch  " + e + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                            e.printStackTrace();
                        }
                    }
                }

            }
        }).start();

    }
    private void bt_readData(DeviceMirror deviceMirror,String serviceUUID, String readUUID){
        if(deviceMirror != null) {
            bt_readData_init(deviceMirror,serviceUUID, readUUID);
        }
    }

    private int bt_readBatteryEnergy(){
        int battery = 0;
        bt_readData(mDeviceMirror,battery_service_uuid, battery_read_uuid);
        return battery;
    }
    private int bt_readSignatureData(){
        int battery = 0;
        bt_readData(mDeviceMirror,service_uuid, read_uuid);
        return battery;
    }

    private void bt_change_notify_static( DeviceMirror deviceMirror ,boolean n_static){
        if(n_static){
            Log.d("ble_Status=","notify open");
            deviceMirror.registerNotify(true);
        }else{
            Log.d("ble_Status=","notify close");
            deviceMirror.unregisterNotify(true);
        }
    }

    private void displayGattServices(final List<BluetoothGattService> gattServices) {
        if(gattServices == null) return  ;
        String uuid;
        final String unknownServiceString = getResources().getString(R.string.unknown_service);
        final String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        final List<Map<String,String>> gattServiceData = new ArrayList<>();
        final List<List<Map<String,String>>> gattCharacteristicData = new ArrayList<>();
        mGattServices = new ArrayList<>();
        mGattCharacteristics = new ArrayList<>();
        // service
        for(final BluetoothGattService gattService : gattServices){
            final Map<String,String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
            if(!uuid.equalsIgnoreCase(service_uuid)){
                continue;
            }
            Log.d("ble_Status=","service name: " + GattAttributeResolver.getAttributeName(uuid,unknownServiceString));
            Log.d("ble_Status=","service uuid: " + uuid);

            currentServiceData.put(LIST_NAME, GattAttributeResolver.getAttributeName(uuid,unknownServiceString));
            currentServiceData.put(LIST_UUID,uuid);
            gattServiceData.add(currentServiceData);


            final List<Map<String,String>> gattCharacteristicGroupData = new ArrayList<>();
            final List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            final List<BluetoothGattCharacteristic> charas = new ArrayList<>();
           // final List<BluetoothGattDescriptor> gattDescriptors = gattService.getCharacteristic().getDescriptors()

            for(final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics ){
                charas.add(gattCharacteristic);
                final Map<String,String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                if(uuid.equalsIgnoreCase(notify_uuid)){
                    final List<BluetoothGattDescriptor> gattDescriptors  =  gattCharacteristic.getDescriptors();
                    for(final BluetoothGattDescriptor gattDescriptor : gattDescriptors){
                        String des_uuid = gattDescriptor.getUuid().toString();
                        Log.d("ble_Status=","descriptors uuid: " + des_uuid);
                    }
                }
                currentCharaData.put(LIST_NAME, GattAttributeResolver.getAttributeName(uuid,unknownCharaString));
                currentCharaData.put(LIST_UUID,uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                Log.d("ble_Status=","char name: " + GattAttributeResolver.getAttributeName(uuid,unknownCharaString));
                Log.d("ble_Status=","char uuid: " + uuid);
            }
            mGattServices.add(gattService);
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }
    private void connectDevice( BluetoothLeDevice cDevice){
        ViseBle.getInstance().connect(cDevice, new IConnectCallback() {
            @Override
            public void onConnectSuccess(DeviceMirror deviceMirror) {
                Log.d("ble_Status=","connect success");
               // displayGattServices(deviceMirror.getBluetoothGatt().getServices());
                mDeviceMirror = deviceMirror;
                bt_writeData_init(mDeviceMirror);
                //bt_notifyData_init(mDeviceMirror);
//                Message message = new Message();
//                message.what = CONNECT_SUCCESS_MSG;
//                cHandler.sendMessage(message);
                cHandler.obtainMessage(CONNECT_SUCCESS_MSG).sendToTarget();
            }

            @Override
            public void onConnectFailure(BleException exception) {
                Log.d("ble_Status=","connect fail");
            }

            @Override
            public void onDisconnect(boolean isActive) {
                Log.d("ble_Status=","disconnect");
//                Message message = new Message();
//                message.what = DISCONNECT_MSG;
//                cHandler.sendMessage(message);
                cHandler.obtainMessage(DISCONNECT_MSG).sendToTarget();
            }
        });
    }

    private void disconnectDevice(BluetoothLeDevice cDdvice){
        ViseBle.getInstance().disconnect(cDdvice);
    }


    private Handler cHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case CONNECT_SUCCESS_MSG:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ConnectActivity.this, "connect success",Toast.LENGTH_SHORT).show();
                            if(mBt_connect.getText().equals("CONNECT")){
                                mBt_connect.setText("DISCONNECT");
                                showConnectInfo(mDevice);
                            }
                        }
                    });
                    break;
                case DISCONNECT_MSG:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ConnectActivity.this, "disconnect",Toast.LENGTH_SHORT).show();
                            if(mBt_connect.getText().equals("DISCONNECT")){
                                mBt_connect.setText("CONNECT");
                            }
                            mTv_conInfo.setText("");
                        }
                    });
                    break;
                case FILE_DATA_MSG:
                    Bundle bundle = message.getData();
                    byte[] getFileData = bundle.getByteArray(FILEDATAKEY);
                    String str_read = HexUtil.encodeHexStr(getFileData);
                  //  Log.d("ble_Status", "get file data length: "+ getFileData.length + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    Log.d("ble_Status", "length" + getFileData.length +"get file data: "+ str_read + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                  //  Log.d("ble_Status", "start write file data: " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    bt_sendFileData(mDeviceMirror, getFileData);

                    break;
            }
            return true;
        }
    });
    @Override
    public void onClick(View view) {
        String defaultPath = "";
        switch (view.getId()){
            case R.id.bt_connect:
                if(mBt_connect.getText().equals("CONNECT")){
                    Log.d("ble_Status", "connect button click" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                   if(mDevice != null){
                       connectDevice(mDevice);
                   }
                }else{
                    Log.d("ble_Status", "disconnect button click" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    if(mDevice != null){
                        disconnectDevice(mDevice);
                    }
                }
                break;

            case R.id.bt_browse:
                Log.d("ble_Status", "browse button click" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                if(MyFileHandle.isSdCardExist()){
                    Log.d("ble_Status", "sdcard is on" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    String sdPath = MyFileHandle.getSdCardPath();
                    Log.d("ble_Status", "sd card path: " + sdPath+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                }else {
                    Log.d("ble_Status", "sdcard is off" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                }

                if(MyFileHandle.getDefaultFilePath() != null){
                    defaultPath = MyFileHandle.getDefaultFilePath();
                    Log.d("ble_Status", "default file path: " +defaultPath+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                }else {
                    Log.d("ble_Status", "default file is not exist: "+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                }
                Log.d("ble_Status", "get file stream!" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                FileInputStream fileStream = MyFileHandle.getFileInputStream();
                if(fileStream == null){
                    Log.d("ble_Status", "error: fileStream == null!" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    return;
                }
                byte[] fileData = null;
                try {
                    mEt_path.setText(defaultPath);
                    Log.d("ble_Status", "fileData!" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    fileData = new byte[fileStream.available()];
                    Log.d("ble_Status", "fileStream read!" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    fileStream.read(fileData);
                    Log.d("ble_Status", "HexUtil!" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    String str_read = HexUtil.encodeHexStr(fileData);
                    Log.d("ble_Status", "file data length: "+ fileData.length + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    Log.d("ble_Status", "file data1: "+ str_read + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    Message message = new Message();
                    message.what = FILE_DATA_MSG;
                    Bundle bundle = new Bundle();
                    bundle.putByteArray(FILEDATAKEY,fileData);
                    message.setData(bundle);
                    cHandler.sendMessage(message);

                    Log.d("ble_Status", "cHandler sendMessage " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                } catch (IOException e) {
                    Log.d("ble_Status", "error!" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                    e.printStackTrace();
                }
                break;

            case R.id.bt_write:
                Log.d("ble_Status=","onClick_bt_write");
                bt_writeData(mDeviceMirror,write_data);
                break;

            case R.id.bt_read:
                Log.d("ble_Status=","onClick_bt_read");
                bt_readSignatureData();
                break;

            case R.id.bt_notify:
                Log.d("ble_Status=","onClick_bt_notify");
                if(mBt_notify.getText().equals("N_OPEN")){
                    mBt_notify.setText("N_CLOSE");
                    bt_change_notify_static(mDeviceMirror,true);
                }else{
                    mBt_notify.setText("N_OPEN");
                    bt_change_notify_static(mDeviceMirror,false);
                }
                break;

            case R.id.bt_battery:
                Log.d("ble_Status=","onClick_bt_battery");
                bt_readBatteryEnergy();
                break;
        }
    }




}
