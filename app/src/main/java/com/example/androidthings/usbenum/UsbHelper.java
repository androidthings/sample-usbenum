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

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;

import java.util.Locale;

/**
 * Parsing utility for USB device configuration info.
 */
public class UsbHelper {

    /* Descriptor header sizes */
    public static final int DESC_SIZE_DEVICE = 18;
    public static final int DESC_SIZE_CONFIG = 9;
    public static final int DESC_SIZE_INTERFACE = 9;
    public static final int DESC_SIZE_ENDPOINT = 7;
    /* USB descriptor type values */
    public static final int DESC_TYPE_DEVICE = 0x01;
    public static final int DESC_TYPE_CONFIG = 0x02;
    public static final int DESC_TYPE_INTERFACE = 0x04;
    public static final int DESC_TYPE_ENDPOINT = 0x05;
    /* USB power attribute flags */
    public static final int ATTR_BUSPOWER = 0x80;
    public static final int ATTR_SELFPOWER = 0x40;
    public static final int ATTR_REMOTEWAKE = 0x20;

    /**
     * Enumerate the endpoints and interfaces on the connected device.
     *
     * @param device Device to query.
     * @return String description of the device configuration.
     */
    public static String readDevice(UsbDevice device) {
        StringBuilder sb = new StringBuilder();
        sb.append("Device Name: " + device.getDeviceName() + "\n");
        sb.append(String.format(
                "Device Class: %s -> Subclass: 0x%02x -> Protocol: 0x%02x\n",
                nameForClass(device.getDeviceClass()),
                device.getDeviceSubclass(), device.getDeviceProtocol()));

        for (int i = 0; i < device.getInterfaceCount(); i++) {
            UsbInterface intf = device.getInterface(i);
            sb.append(String.format(Locale.US,
                    "-- Interface %d Class: %s -> Subclass: 0x%02x -> Protocol: 0x%02x\n",
                    intf.getId(),
                    nameForClass(intf.getInterfaceClass()),
                    intf.getInterfaceSubclass(),
                    intf.getInterfaceProtocol()));

            sb.append(String.format(Locale.US, "   -- Endpoint Count: %d\n",
                    intf.getEndpointCount()));
        }

        return sb.toString();
    }

    /* Helper Methods to Provide Readable Names for USB Constants */

    public static String nameForClass(int classType) {
        switch (classType) {
            case UsbConstants.USB_CLASS_APP_SPEC:
                return String.format("Application Specific 0x%02x", classType);
            case UsbConstants.USB_CLASS_AUDIO:
                return "Audio";
            case UsbConstants.USB_CLASS_CDC_DATA:
                return "CDC Control";
            case UsbConstants.USB_CLASS_COMM:
                return "Communications";
            case UsbConstants.USB_CLASS_CONTENT_SEC:
                return "Content Security";
            case UsbConstants.USB_CLASS_CSCID:
                return "Content Smart Card";
            case UsbConstants.USB_CLASS_HID:
                return "Human Interface Device";
            case UsbConstants.USB_CLASS_HUB:
                return "Hub";
            case UsbConstants.USB_CLASS_MASS_STORAGE:
                return "Mass Storage";
            case UsbConstants.USB_CLASS_MISC:
                return "Wireless Miscellaneous";
            case UsbConstants.USB_CLASS_PER_INTERFACE:
                return "(Defined Per Interface)";
            case UsbConstants.USB_CLASS_PHYSICA:
                return "Physical";
            case UsbConstants.USB_CLASS_PRINTER:
                return "Printer";
            case UsbConstants.USB_CLASS_STILL_IMAGE:
                return "Still Image";
            case UsbConstants.USB_CLASS_VENDOR_SPEC:
                return String.format("Vendor Specific 0x%02x", classType);
            case UsbConstants.USB_CLASS_VIDEO:
                return "Video";
            case UsbConstants.USB_CLASS_WIRELESS_CONTROLLER:
                return "Wireless Controller";
            default:
                return String.format("0x%02x", classType);
        }
    }

    public static String nameForEndpointType(int type) {
        switch (type) {
            case UsbConstants.USB_ENDPOINT_XFER_BULK:
                return "Bulk";
            case UsbConstants.USB_ENDPOINT_XFER_CONTROL:
                return "Control";
            case UsbConstants.USB_ENDPOINT_XFER_INT:
                return "Interrupt";
            case UsbConstants.USB_ENDPOINT_XFER_ISOC:
                return "Isochronous";
            default:
                return "Unknown Type";
        }
    }

    public static String nameForDirection(int direction) {
        switch (direction) {
            case UsbConstants.USB_DIR_IN:
                return "IN";
            case UsbConstants.USB_DIR_OUT:
                return "OUT";
            default:
                return "Unknown Direction";
        }
    }
}
