package com.vise.bledemo.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    private byte[] char_descriptors = {0x02,0x29};
    private  byte[] write_data = {0x01,0x02,0x03,0x04,0x05};
    public static final String CONNECT_DEVICE = "connect_device";
    private BluetoothLeDevice mDevice;
    private TextView mTv_conInfo;
    private StringBuilder sb_btInfo;
    private Button mBt_connect,mBt_disconnect,mBt_write,mBt_read,mBt_notify,mBt_battery;
    private String devName = "i3vr";
    private List<BluetoothGattService> mGattServices = new ArrayList<>();
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    private DeviceMirror mDeviceMirror;
    private SimpleExpandableListAdapter simpleExpandableListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        widget_init();
        showConnectInfo(mDevice);
    }

    private void widget_init(){
        mDevice = getIntent().getParcelableExtra(CONNECT_DEVICE);
        mTv_conInfo = findViewById(R.id.tv_conInfo);
        sb_btInfo = new StringBuilder();
        mBt_connect = findViewById(R.id.bt_connect);
        mBt_disconnect = findViewById(R.id.bt_disconnect);
        mBt_write = findViewById(R.id.bt_write);
        mBt_read = findViewById(R.id.bt_read);
        mBt_notify = findViewById(R.id.bt_notify);
        mBt_battery = findViewById(R.id.bt_battery);
        mBt_connect.setOnClickListener(this);
        mBt_disconnect.setOnClickListener(this);
        mBt_write.setOnClickListener(this);
        mBt_read.setOnClickListener(this);
        mBt_notify.setOnClickListener(this);
        mBt_battery.setOnClickListener(this);
    }
    private void showConnectInfo(BluetoothLeDevice cDevice){
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
                String str_write =  HexUtil.encodeHexStr(data);
                Log.d("ble_Status=","len:"+data.length+"  Data:"+ str_write);
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
               // .setDescriptorUUID(UUID.fromString(descriptors_uuid))
                .builder();
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                if(bluetoothGattChannel.getCharacteristicUUID().equals(UUID.fromString(battery_read_uuid))){
                    Log.d("ble_Status=","battery data[ "+data[0]+"% ]");
                }else {
                    String str_read =  HexUtil.encodeHexStr(data);
                    Log.d("ble_Status=","signature data[ "+"len:"+data.length+"  Data:"+ str_read+" ]");
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
                .setDescriptorUUID(UUID.nameUUIDFromBytes(char_descriptors))
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
            Log.d("ble_Status=","write data");
            deviceMirror.writeData(data);
            //BluetoothDeviceManager.getInstance().write(mDevice,data);
        }
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
               // bt_readData_init(mDeviceMirror);
                bt_notifyData_init(mDeviceMirror);
            }

            @Override
            public void onConnectFailure(BleException exception) {
                Log.d("ble_Status=","connect fail");
            }

            @Override
            public void onDisconnect(boolean isActive) {
                Log.d("ble_Status=","disconnect");
            }
        });
    }

    private void disconnectDevice(BluetoothLeDevice cDdvice){
        ViseBle.getInstance().disconnect(cDdvice);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_connect:
                Log.d("ble_Status=","onClick_bt_connect");
                Log.d("ble_Status=","connecting...");
                connectDevice(mDevice);
                mTv_conInfo.setText(sb_btInfo.toString());
                break;

            case R.id.bt_disconnect:
                Log.d("ble_Status=","onClick_bt_disconnect");
                disconnectDevice(mDevice);
                finish();
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
