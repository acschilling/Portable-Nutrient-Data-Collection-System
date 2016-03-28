package com.ndca.nutrientdatacollectionapp;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Written by Anthony Schilling
 * May1633 "Portable Nutrient Data Collection System"
 * sources:
 *      Dave Smith https://github.com/devunwired/accessory-samples/blob/master/BluetoothGatt/src/com/example/bluetoothgatt/MainActivity.java
 *      truiton http://www.truiton.com/2015/04/android-bluetooth-low-energy-ble-example/
 *      Google http://developer.android.com/guide/topics/connectivity/bluetooth-le.html
 */

public class bt extends HomeActivity{
    private static final String TAG = "Portable Nutrient Data Collection System";
    private BluetoothAdapter mBluetoothAdapter;
    private String deviceName = "PNDCS";
    private BluetoothGatt mConnectedGatt;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;

    private Handler mhandler;

    //onCreate
    public void initialize()
   {
       //default bluetooth adapter
       mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

       // Initializes Bluetooth adapter.
       final BluetoothManager bluetoothManager =
               (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
       mBluetoothAdapter = bluetoothManager.getAdapter();
       mhandler = new Handler();
   }

    //onResume
    public void btOn()
    {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //set up scanner for PNDCS only
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();

        //setUUID also for specific device
        ScanFilter filter1 = new ScanFilter.Builder().setDeviceName(deviceName).build();
        filters = new ArrayList<ScanFilter>();
        filters.add(filter1);

        //start scanning
        startScan();
    }

    //onStop
   public void btOff()
   {
       //Disconnect from any active tag connection
       if (mConnectedGatt != null) {
           mConnectedGatt.disconnect();
           mConnectedGatt = null;
       }
   }

    //connect to found bluetooth device
    public void btConnect(BluetoothDevice device)
    {
        if(mConnectedGatt == null)
        {
            Log.i(TAG, "Connecting to " + device.getName());
            mConnectedGatt = device.connectGatt(this,false,mGattCallback); //change boolean for connecting automatically to device when found
            mhandler.removeCallbacks(mStopRunnable);
            mhandler.removeCallbacks(mStartRunnable);
        }
    }

    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    //scan for PNDCS
    private void startScan() {
        mBluetoothLeScanner.startScan(filters,settings,mScanCallBack);

        //wait for 10 seconds and then stop scan
        mhandler.postDelayed(mStopRunnable, 10000);
    }

    //stop scanning for devices
    private void stopScan()
    {
        mBluetoothLeScanner.stopScan(mScanCallBack);
    }

    private ScanCallback mScanCallBack = new ScanCallback() {
        //when PNDCS is found connect
        @Override
        public void onScanResult(int CallBackType, ScanResult result) {
            //once PNDCS device is found connect
            Log.i("callbackType", String.valueOf(CallBackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();

            //change to show device and click on it first?
            btConnect(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };


    //state machine for how the device communicates with device
    //PNDCS protocol
    //TODO
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
        }
    };











    }