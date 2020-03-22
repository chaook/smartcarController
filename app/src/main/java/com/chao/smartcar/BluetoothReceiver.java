package com.chao.smartcar;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

public class BluetoothReceiver extends BroadcastReceiver {

    private Button enableButton, connectButton;

    @SuppressLint("SetTextI18n")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (action==null)
            action = "";
        switch (action){
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                if (enableButton == null)
                    enableButton = ((MainActivity) context).findViewById(R.id.enable);
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)==BluetoothAdapter.STATE_OFF)
                    enableButton.setText("打开蓝牙");
                else
                    enableButton.setText("蓝牙已打开");
                break;
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                if (connectButton==null)
                    connectButton = ((MainActivity) context).findViewById(R.id.connect);
                assert bluetoothDevice != null;
                connectButton.setText("已连接设备 "+bluetoothDevice.getName());
                Toast.makeText(context, "设备："+bluetoothDevice.getName()+"已连接", Toast.LENGTH_SHORT).show();
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                if (connectButton==null)
                    connectButton = ((MainActivity) context).findViewById(R.id.connect);
                connectButton.setText("连接设备");
                assert bluetoothDevice != null;
                Toast.makeText(context, "设备："+bluetoothDevice.getName()+"已断开", Toast.LENGTH_SHORT).show();
        }
    }
}
