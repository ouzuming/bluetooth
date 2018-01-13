package com.vise.bledemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import com.vise.baseble.ViseBle;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.xsnow.ui.adapter.helper.HelperAdapter;
import com.vise.xsnow.ui.adapter.helper.HelperViewHolder;
import com.vise.bledemo.R;

/**
 * Created by 3ivr on 2018/1/8.
 */

public class AutoConnectAdapter extends HelperAdapter<BluetoothLeDevice>{
    public AutoConnectAdapter(Context context) {
        super(context, R.layout.item_auto_connect_layout);
    }

    @Override
    public void HelpConvert(HelperViewHolder viewHolder, int position, BluetoothLeDevice device) {
        TextView tv_deviceName = viewHolder.getView(R.id.tv_device_name);
        TextView tv_deviceMac = viewHolder.getView(R.id.tv_device_mac);
        TextView tv_deviceRssi = viewHolder.getView(R.id.tv_device_rssi);
        TextView tv_deviceConnect = viewHolder.getView(R.id.tv_device_connect);
        if(device != null && device.getDevice() != null){
            String deviceName = device.getDevice().getName();
            if(deviceName != null && !deviceName.isEmpty()){
                tv_deviceName.setText(deviceName);
            }else {
                tv_deviceName.setText("unknow_device");
            }

            tv_deviceMac.setText(device.getDevice().getAddress());
            tv_deviceRssi.setText("RSSI:"+device.getRssi()+"dB");
            if(ViseBle.getInstance().isConnect(device)){
                tv_deviceConnect.setText(R.string.static_connected);
                tv_deviceConnect.setTextColor(Color.BLUE);
                tv_deviceMac.setTextColor(Color.BLUE);
                tv_deviceRssi.setTextColor(Color.BLUE);
            }else {
                tv_deviceConnect.setText(R.string.static_adversting);
                tv_deviceConnect.setTextColor(Color.BLACK);
                tv_deviceMac.setTextColor(Color.BLACK);
                tv_deviceRssi.setTextColor(Color.BLACK);
            }
        }
    }
}
