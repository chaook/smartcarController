package com.chao.smartcar;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity implements View.OnClickListener
{
    private BluetoothReceiver mReceive;
    private Button enableButton, connectButton;
    private SeekBar seekBar1, seekBar2;
    private BluetoothAdapter bluetoothAdapter;
    private Connected connected;
    private BluetoothSocket mmSocket;
    private byte[] bytes = {'m', 0, 0, 0, 0, '\n'};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "该设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }

        enableButton = findViewById(R.id.enable);
        connectButton = findViewById(R.id.connect);
        seekBar1 = findViewById(R.id.seekbar1);
        seekBar2 = findViewById(R.id.seekbar2);

    }

    protected void onStart() {
        super.onStart();
        if (bluetoothAdapter.isEnabled()){
            enableButton.setText("蓝牙已打开");
        }
        registerBluetoothReceiver();
        enableButton.setOnClickListener(this);
        connectButton.setOnClickListener(this);
        seekBar1.setProgress(50);
        seekBar2.setProgress(50);

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress<50){
                    bytes[3] = '-';
                    bytes[4] = (byte) -(progress-50);
                }
                else{
                    bytes[3] = '+';
                    bytes[4] = (byte) (progress-50);
                }
                if (connected!=null) {
                    connected.write(bytes);
                }
                else
                    Toast.makeText(MainActivity.this, "请先连接设备", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar1.setProgress(50);
                if (connected!=null){
                    bytes[3] = 0;
                    bytes[4] = 0;
                    connected.write(bytes);
                }
            }
        });

        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress<50){
                    bytes[1] = '-';
                    bytes[2] = (byte) -(progress-50);
                }
                else{
                    bytes[1] = '+';
                    bytes[2] = (byte) (progress-50);
                }
                if (connected!=null){
                    connected.write(bytes);
                }else
                    Toast.makeText(MainActivity.this, "请先连接设备", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar2.setProgress(50);
                if (connected!=null){
                    bytes[1] = 0;
                    bytes[2] = 0;
                    connected.write(bytes);
                }
            }
        });

    }

    private void registerBluetoothReceiver() {
        if (mReceive == null) {
            mReceive = new BluetoothReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");
        registerReceiver(mReceive, intentFilter);
    }

    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);
        }
    }

    private void connectBluetooth() {
        if (!bluetoothAdapter.isEnabled()){
            Toast.makeText(this, "请先打开蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        if (connected!=null){
            Toast.makeText(MainActivity.this, "已连接", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> pairedDevicesName = new ArrayList<>();
        final List<BluetoothDevice> pairedDevices = new ArrayList<>();
        Set<BluetoothDevice> pairedDevicesSet = bluetoothAdapter.getBondedDevices();
        if (pairedDevicesSet.size() > 0)
            for (BluetoothDevice device : pairedDevicesSet) {
                pairedDevicesName.add(device.getName() + "\n"
                        + device.getAddress());
                pairedDevices.add(device);
            }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("已配对设备:");
        String[] devices = pairedDevicesName.toArray(new String[0]);
        dialogBuilder.setItems(devices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (bluetoothAdapter.isDiscovering())
                            bluetoothAdapter.cancelDiscovery();
                        try {
                            UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                            mmSocket = pairedDevices.get(which).createRfcommSocketToServiceRecord (MY_UUID);
                            mmSocket.connect();
                            connected = new Connected(mmSocket);
                            new Thread(connected).start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        dialogBuilder.create();
        dialogBuilder.show();
        Toast.makeText(this, "提示：若想连接新设备请先前往设置配对", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.connect:
                connectBluetooth();
                break;
            case R.id.enable:
                enableBluetooth();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connected!=null)
            connected.close();
        unregisterReceiver(mReceive);
    }

}
