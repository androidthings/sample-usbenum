/*
 * Copyright 2017, The Android Open Source Project
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

package com.example.androidthings.usbenum;

import android.hardware.usb.UsbDeviceConnection;

/**
 * Structured response for a USB device descriptor.
 * Defined in section 9.5 of the USB 2.0 spec: http://www.usb.org/developers/docs/usb20_docs/
 */
public class DeviceDescriptor {
    private static final String TAG = DeviceDescriptor.class.getSimpleName();

    // Type: Indicates whether this is a read or write
    //  Matches USB_ENDPOINT_DIR_MASK for either IN or OUT
    private static final int REQUEST_TYPE = 0x80;
    // Request: GET_DESCRIPTOR = 0x06
    private static final int REQUEST = 0x06;
    // Value: Descriptor Type (High) and Index (Low)
    //  Device Descriptor = 0x1
    //  Descriptor Index = 0x0
    private static final int REQ_VALUE = 0x100;
    private static final int REQ_INDEX = 0x00;
    private static final int LENGTH = 32;
    private static final int TIMEOUT = 2000;

    /**
     * Request the device descriptor through the USB device connection.
     */
    public static DeviceDescriptor fromDeviceConnection(UsbDeviceConnection connection) {
        byte[] buffer = new byte[LENGTH];

        connection.controlTransfer(REQUEST_TYPE, REQUEST, REQ_VALUE, REQ_INDEX,
                buffer, LENGTH, TIMEOUT);

        return parseResponse(buffer);
    }

    /**
     * Parse the USB device descriptor response per the USB specification.
     *
     * @param buffer Raw configuration descriptor from the device.
     */
    private static DeviceDescriptor parseResponse(byte[] buffer) {
        if (buffer[0] != UsbHelper.DESC_SIZE_DEVICE || buffer[1] != UsbHelper.DESC_TYPE_DEVICE) {
            throw new IllegalArgumentException("Invalid device descriptor");
        }

        // Parse device descriptor header
        int deviceClass = (buffer[4] & 0xFF);
        int deviceSubclass = (buffer[5] & 0xFF);
        int deviceProtocol = (buffer[6] & 0xFF);
        // Maximum packet size for control transfers
        int maxPacketSize = (buffer[7] & 0xFF);
        // USB.org assigned vendor id
        int vendorId = (buffer[9] & 0xFF) << 8;
        vendorId += (buffer[8] & 0xFF);
        // Manufacturer assigned product id
        int productId = (buffer[11] & 0xFF) << 8;
        productId += (buffer[10] & 0xFF);

        return new DeviceDescriptor(deviceClass, deviceSubclass, deviceProtocol,
                maxPacketSize, vendorId, productId);
    }

    // Descriptor fields
    public final int deviceClass;
    public final int deviceSubclass;
    public final int deviceProtocol;
    public final int maxPacketSize;
    public final int vendorId;
    public final int productId;

    public DeviceDescriptor(int deviceClass, int deviceSubclass, int deviceProtocol,
                            int maxPacketSize, int vendorId, int productId) {
        this.deviceClass = deviceClass;
        this.deviceSubclass = deviceSubclass;
        this.deviceProtocol = deviceProtocol;
        this.maxPacketSize = maxPacketSize;
        this.vendorId = vendorId;
        this.productId = productId;
    }

    /**
     * Return a printable description of the connected device.
     */
    @Override
    public String toString() {
        return String.format(
                "Device Descriptor:\n"
                + "Device Class: %s -> Subclass: 0x%02x -> Protocol: 0x%02x\n"
                + "Max Packet Size: %d\n"
                + "VID: 0x%04x\nPID: 0x%04x\n",
                UsbHelper.nameForClass(deviceClass), deviceSubclass, deviceProtocol,
                maxPacketSize, vendorId, productId
        );
    }
}
