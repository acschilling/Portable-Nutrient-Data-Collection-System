package com.ndca.nutrientdatacollectionapp;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
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
 *      devunwired https://github.com/devunwired/accessory-samples/blob/master/BluetoothGattPeripheral/src/main/java/com/example/android/bluetoothgattperipheral/DeviceProfile.java
 */

@TargetApi(21)
public class bt{
    private static final String TAG = "May1633";
    private BluetoothAdapter mBluetoothAdapter;
    private String deviceName = "PNDCS";
    private BluetoothGatt mConnectedGatt;
    private BluetoothGattServer mGattServer;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;

    private Handler mhandler;
    private boolean isOn = false;

    private Context mContext;
    public bt(Context mContext) {
        this.mContext = mContext;
    }

    //onCreate
    public void initialize()
   {
       //default bluetooth adapter
       mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

       // Initializes Bluetooth adapter.
       final BluetoothManager bluetoothManager =
               (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
       mBluetoothAdapter = bluetoothManager.getAdapter();
       mhandler = new Handler();
       mGattServer = bluetoothManager.openGattServer(mContext, mGattServerCallback);
   }

    //onResume
    public void btOn()
    {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.startActivity(enableBtIntent);
            ((Activity)mContext).finish();
            return;
        }

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mContext, "BLE not supported", Toast.LENGTH_SHORT).show();
            ((Activity)mContext).finish();
            return;
        }

        initServer();

        //set up scanner for PNDCS only
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();

        ScanFilter filter1 = new ScanFilter.Builder().setDeviceAddress("F8:F0:05:F3:4A:A3").build();
        filters = new ArrayList<ScanFilter>();
        filters.add(filter1);

        //start scanning
        startScan();
    }

    public void initServer() {
        BluetoothGattService service =new BluetoothGattService(DeviceProfile.SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_SECONDARY);

        BluetoothGattCharacteristic startCharacteristic =
                new BluetoothGattCharacteristic(DeviceProfile.CHARACTERISTIC_START_UUID,
                        //Read-only characteristic, supports notifications
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_READ);

        if(service.addCharacteristic(startCharacteristic));
        {
            Log.i("char_add","Characteristic added");
        }

        mGattServer.addService(service);
    }

    private void shutdownServer() {
        mhandler.removeCallbacks(mNotifyRunnable);

        if (mGattServer == null) return;

        mGattServer.close();
    }

    private Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            notifyConnectedDevices();
            mhandler.postDelayed(this, 2000);
        }
    };

    //onStop
   public void btOff()
   {
       //Disconnect from any active tag connection
       if (mConnectedGatt != null) {
           mConnectedGatt.disconnect();
           mConnectedGatt = null;
       }
   }

    public boolean isBTOn()
    {
        return isOn;
    }

    //connect to found bluetooth device
    private void btConnect(BluetoothDevice device)
    {
        if(mConnectedGatt == null)
        {
            Log.i(TAG, "Connecting to " + device.getName());
            mConnectedGatt = device.connectGatt(mContext,false,mGattCallback); //change boolean for connecting automatically to device when found
            mhandler.removeCallbacks(mStopRunnable);
            mhandler.removeCallbacks(mStartRunnable);
        }
        else
        {
            mConnectedGatt.connect();
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
    public void stopScan()
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


    //Bluetooth Central, Client
    //callbacks for connection state change, services found, and reading characteristics
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    isOn = true;
                    gatt.discoverServices();
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(mContext, "Connected to " + deviceName, Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    isOn = false;
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(mContext, "Disconnected from " + deviceName, Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattService service = mConnectedGatt.getService(DeviceProfile.PNDCS_SERVICE_UUID);
            Log.i("onServicesDiscovered", service.toString());
            gatt.readCharacteristic(service.getCharacteristic(DeviceProfile.CHARACTERISTIC_DATA_UUID));

            mConnectedGatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    characteristic.getUuid());                                      //need some characteristic uuid
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mConnectedGatt.writeDescriptor(descriptor);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            gatt.disconnect();
        }

        @Override
        // Characteristic notification
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            //TODO do something with the characteristic data received
            Log.i("notify", characteristic.toString());
        }

    };


    //Bluetooth, Server
    /*
     * Callback for Server
     */
    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.i(TAG, "onConnectionStateChange "
                    +DeviceProfile.getStatusDescription(status)+" "
                    +DeviceProfile.getStateDescription(newState));

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("server callback","Device connected");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("server callback","Device disconnected");
            }
        }
    };

    //use to notify PNDCS device to start
    public void notifyConnectedDevices() {
            BluetoothGattCharacteristic readCharacteristic = mGattServer.getService(DeviceProfile.SERVICE_UUID)
                    .getCharacteristic(DeviceProfile.CHARACTERISTIC_START_UUID);
            readCharacteristic.setValue(DeviceProfile.bytesFromInt(1));
            mGattServer.notifyCharacteristicChanged(mConnectedGatt.getDevice(), readCharacteristic, false);
        Log.i("notify", "Characteristic notified");
    }

}