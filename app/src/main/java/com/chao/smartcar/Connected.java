package com.chao.smartcar;


import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;

public class Connected implements Runnable {
    private OutputStream outputStream;
    private BluetoothSocket bluetoothSocket;

    Connected(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
        try {
            outputStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            close();
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //持续监听 InputStream
        while (true){

        }
    }

    void write(byte[] dat) {
        try {
            outputStream.write(dat);
            outputStream.flush();
            System.out.println(dat[1]+" "+dat[2]+"   "+dat[3]+" "+dat[4]);
        } catch (IOException e) {
            close();
            e.printStackTrace();
        }
    }

    void close(){
        try {
            if (bluetoothSocket.isConnected())
                bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

