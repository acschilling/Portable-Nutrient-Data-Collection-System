package com.ndca.nutrientdatacollectionapp;

/**
 * Created by Anthony on 4/22/2016.
 */
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Dave Smith
 * Date: 11/13/14
 * DeviceProfile
 * Service/Characteristic constant for our custom peripheral
 */
public class DeviceProfile {


    //Service UUID to expose our time characteristics
    public static UUID SERVICE_UUID = UUID.fromString("26622248-b20d-4e6d-a316-0d831afb7f07");
    //Read-only characteristic indicating to start the PNDCS system
    public static UUID CHARACTERISTIC_START_UUID = UUID.fromString("a721680f-526a-4d2c-b864-9ab43eb98197");
    //PNDCS device data transfer service
    public static UUID PNDCS_SERVICE_UUID = UUID.fromString("d988645c-4e4b-a4b2-de4d-1ce8b4d08815");
    //PNDCS device data transfer characteristic
    public static UUID CHARACTERISTIC_DATA_UUID = UUID.fromString("0869DDE9-1C90-670A-D634-D6E4123EFE96");


    public static String getStateDescription(int state) {
        switch (state) {
            case BluetoothProfile.STATE_CONNECTED:
                return "Connected";
            case BluetoothProfile.STATE_CONNECTING:
                return "Connecting";
            case BluetoothProfile.STATE_DISCONNECTED:
                return "Disconnected";
            case BluetoothProfile.STATE_DISCONNECTING:
                return "Disconnecting";
            default:
                return "Unknown State "+state;
        }
    }

    public static String getStatusDescription(int status) {
        switch (status) {
            case BluetoothGatt.GATT_SUCCESS:
                return "SUCCESS";
            default:
                return "Unknown Status "+status;
        }
    }

    public static byte[] getShiftedTimeValue(int timeOffset) {
        int value = Math.max(0,
                (int)(System.currentTimeMillis()/1000) - timeOffset);
        return bytesFromInt(value);
    }

    public static int unsignedIntFromBytes(byte[] raw) {
        if (raw.length < 4) throw new IllegalArgumentException("Cannot convert raw data to int");

        return ((raw[0] & 0xFF)
                + ((raw[1] & 0xFF) << 8)
                + ((raw[2] & 0xFF) << 16)
                + ((raw[3] & 0xFF) << 24));
    }

    public static byte[] bytesFromInt(int value) {
        //Convert result into raw bytes. GATT APIs expect LE order
        return ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array();
    }
}
