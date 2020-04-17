package com.grad.wave;

import android.content.Context;
import android.widget.Toast;

import java.io.InputStream;

public class DataReceivingThread extends Thread {
    private Context context = null;

    public DataReceivingThread(Context c) {
        try {
            context = c;
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean is_received = false;
    public byte[] dat = new byte[1023];

    public void run() {
        try {
            dat = AppManager.NetIO.RecvDat();
            is_received = true;
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}