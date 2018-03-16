/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.usbenum

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView

import java.text.ParseException

private const val TAG = "UsbEnumerator"

class UsbActivity : Activity() {

    /* USB system service */
    private lateinit var usbManager: UsbManager

    /* UI elements */
    private lateinit var statusView: TextView
    private lateinit var resultView: TextView

    /**
     * Broadcast receiver to handle USB disconnect events.
     */
    private var usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                device?.let {
                    printStatus(getString(R.string.status_removed))
                    printDeviceDescription(it)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usb)

        statusView = findViewById(R.id.text_status)
        resultView = findViewById(R.id.text_result)

        usbManager = getSystemService(UsbManager::class.java)

        // Detach events are sent as a system-wide broadcast
        val filter = IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(usbReceiver, filter)

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    /**
     * Determine whether to list all devices or query a specific device from
     * the provided intent.
     * @param intent Intent to query.
     */
    private fun handleIntent(intent: Intent) {
        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
        if (device != null) {
            printStatus(getString(R.string.status_added))
            printDeviceDetails(device)
        } else {
            // List all devices connected to USB host on startup
            printStatus(getString(R.string.status_list))
            printDeviceList()
        }
    }

    /**
     * Print the list of currently visible USB devices.
     */
    private fun printDeviceList() {
        val connectedDevices = usbManager.deviceList

        if (connectedDevices.isEmpty()) {
            printResult("No Devices Currently Connected")
        } else {
            val builder = buildString {
                append("Connected Device Count: ")
                append(connectedDevices.size)
                append("\n\n")
                for (device in connectedDevices.values) {
                    //Use the last device detected (if multiple) to open
                    append(device.getDescription())
                    append("\n\n")
                }
            }

            printResult(builder)
        }
    }

    /**
     * Print a basic description about a specific USB device.
     * @param device USB device to query.
     */
    private fun printDeviceDescription(device: UsbDevice) {
        val result = device.getDescription() + "\n\n"
        printResult(result)
    }

    /**
     * Initiate a control transfer to request the device information
     * from its descriptors.
     *
     * @param device USB device to query.
     */
    private fun printDeviceDetails(device: UsbDevice) {
        val connection = usbManager.openDevice(device)

        val deviceDescriptor = try {
            //Parse the raw device descriptor
            connection.readDeviceDescriptor()
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Invalid device descriptor", e)
            null
        }

        val configDescriptor = try {
            //Parse the raw configuration descriptor
            connection.readConfigurationDescriptor()
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Invalid config descriptor", e)
            null
        } catch (e: ParseException) {
            Log.w(TAG, "Unable to parse config descriptor", e)
            null
        }

        printResult("$deviceDescriptor\n\n$configDescriptor")
        connection.close()
    }

    /* Helpers to display user content */

    private fun printStatus(status: String) {
        statusView.text = status
        Log.i(TAG, status)
    }

    private fun printResult(result: String) {
        resultView.text = result
        Log.i(TAG, result)
    }
}
