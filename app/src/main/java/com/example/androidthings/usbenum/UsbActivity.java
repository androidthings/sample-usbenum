package com.example.androidthings.usbenum;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;

public class UsbActivity extends Activity {
    private static final String TAG = "UsbEnumerator";

    /* USB system service */
    private UsbManager mUsbManager;

    /* UI elements */
    private TextView mStatusView, mResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);

        mStatusView = (TextView) findViewById(R.id.text_status);
        mResultView = (TextView) findViewById(R.id.text_result);

        mUsbManager = getSystemService(UsbManager.class);

        // Detach events are sent as a system-wide broadcast
        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
    }

    /**
     * Broadcast receiver to handle USB disconnect events.
     */
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    printStatus(getString(R.string.status_removed));
                    printDeviceDescription(device);
                }
            }
        }
    };

    /**
     * Determine whether to list all devices or query a specific device from
     * the provided intent.
     * @param intent Intent to query.
     */
    private void handleIntent(Intent intent) {
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device != null) {
            printStatus(getString(R.string.status_added));
            printDeviceDetails(device);
        } else {
            // List all devices connected to USB host on startup
            printStatus(getString(R.string.status_list));
            printDeviceList();
        }
    }

    /**
     * Print the list of currently visible USB devices.
     */
    private void printDeviceList() {
        HashMap<String, UsbDevice> connectedDevices = mUsbManager.getDeviceList();

        if (connectedDevices.isEmpty()) {
            printResult("No Devices Currently Connected");
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("Connected Device Count: ");
            builder.append(connectedDevices.size());
            builder.append("\n\n");
            for (UsbDevice device : connectedDevices.values()) {
                //Use the last device detected (if multiple) to open
                builder.append(UsbHelper.readDevice(device));
                builder.append("\n\n");
            }
            printResult(builder.toString());
        }
    }

    /**
     * Print a basic description about a specific USB device.
     * @param device USB device to query.
     */
    private void printDeviceDescription(UsbDevice device) {
        String result = UsbHelper.readDevice(device) + "\n\n";
        printResult(result);
    }

    /**
     * Initiate a control transfer to request the device information
     * from its descriptors.
     *
     * @param device USB device to query.
     */
    private void printDeviceDetails(UsbDevice device) {
        byte[] buffer;
        UsbDeviceConnection connection = mUsbManager.openDevice(device);

        //Parse the raw device descriptor
        buffer = UsbHelper.DeviceDescriptor.executeRequest(connection);
        String deviceDescription = UsbHelper.DeviceDescriptor.parseResponse(buffer);

        //Parse the raw configuration descriptor
        buffer = UsbHelper.ConfigurationDescriptor.executeRequest(connection);
        String configDescription = UsbHelper.ConfigurationDescriptor.parseResponse(buffer);

        printResult(deviceDescription + "\n\n" + configDescription);

        connection.close();
    }

    /* Helpers to display user content */

    private void printStatus(String status) {
        mStatusView.setText(status);
        Log.i(TAG, status);
    }

    private void printResult(String result) {
        mResultView.setText(result);
        Log.i(TAG, result);
    }
}
