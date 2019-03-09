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

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbInterface

import java.util.Locale

/**
 * Extension to simplify parsing bytes into ints
 */
fun Byte.toPositiveInt() = this.toInt() and 0xFF

/**
 * Human readable description of a USB device
 */
fun UsbDevice.getDescription(): String {
    return buildString {
        append("Device Name: $deviceName\n")
        append(String.format(
                "Device Class: %s -> Subclass: 0x%02x -> Protocol: 0x%02x\n",
                UsbHelper.nameForClass(deviceClass),
                deviceSubclass, deviceProtocol))

        for (i in 0 until interfaceCount) {
            val intf = getInterface(i)
            append(intf.getDescription())
        }
    }
}

/**
 * Human readable description of a USB Interface
 */
fun UsbInterface.getDescription(): String {
    return buildString {
        append(String.format(Locale.US,
                "-- Interface %d Class: %s -> Subclass: 0x%02x -> Protocol: 0x%02x\n",
                id,
                UsbHelper.nameForClass(interfaceClass),
                interfaceSubclass,
                interfaceProtocol))

        append(String.format(Locale.US, "   -- Endpoint Count: %d\n",
                endpointCount))
    }
}

/**
 * Parsing utility for USB device configuration info.
 */
object UsbHelper {

    /* Descriptor header sizes */
    const val DESC_SIZE_DEVICE: Byte = 18
    const val DESC_SIZE_CONFIG: Byte = 9
    const val DESC_SIZE_INTERFACE: Byte = 9
    const val DESC_SIZE_ENDPOINT: Byte = 7
    /* USB descriptor type values */
    const val DESC_TYPE_DEVICE: Byte = 0x01
    const val DESC_TYPE_CONFIG: Byte = 0x02
    const val DESC_TYPE_INTERFACE: Byte = 0x04
    const val DESC_TYPE_ENDPOINT: Byte = 0x05
    /* USB power attribute flags */
    const val ATTR_BUSPOWER = 0x80
    const val ATTR_SELFPOWER = 0x40
    const val ATTR_REMOTEWAKE = 0x20

    /* Helper Methods to Provide Readable Names for USB Constants */

    fun nameForClass(classType: Int): String = when (classType) {
        UsbConstants.USB_CLASS_APP_SPEC -> String.format("Application Specific 0x%02x", classType)
        UsbConstants.USB_CLASS_AUDIO -> "Audio"
        UsbConstants.USB_CLASS_CDC_DATA -> "CDC Control"
        UsbConstants.USB_CLASS_COMM -> "Communications"
        UsbConstants.USB_CLASS_CONTENT_SEC -> "Content Security"
        UsbConstants.USB_CLASS_CSCID -> "Content Smart Card"
        UsbConstants.USB_CLASS_HID -> "Human Interface Device"
        UsbConstants.USB_CLASS_HUB -> "Hub"
        UsbConstants.USB_CLASS_MASS_STORAGE -> "Mass Storage"
        UsbConstants.USB_CLASS_MISC -> "Wireless Miscellaneous"
        UsbConstants.USB_CLASS_PER_INTERFACE -> "(Defined Per Interface)"
        UsbConstants.USB_CLASS_PHYSICA -> "Physical"
        UsbConstants.USB_CLASS_PRINTER -> "Printer"
        UsbConstants.USB_CLASS_STILL_IMAGE -> "Still Image"
        UsbConstants.USB_CLASS_VENDOR_SPEC -> String.format("Vendor Specific 0x%02x", classType)
        UsbConstants.USB_CLASS_VIDEO -> "Video"
        UsbConstants.USB_CLASS_WIRELESS_CONTROLLER -> "Wireless Controller"
        else -> String.format("0x%02x", classType)
    }

    fun nameForEndpointType(type: Int): String = when (type) {
        UsbConstants.USB_ENDPOINT_XFER_BULK -> "Bulk"
        UsbConstants.USB_ENDPOINT_XFER_CONTROL -> "Control"
        UsbConstants.USB_ENDPOINT_XFER_INT -> "Interrupt"
        UsbConstants.USB_ENDPOINT_XFER_ISOC -> "Isochronous"
        else -> "Unknown Type"
    }

    fun nameForDirection(direction: Int): String = when (direction) {
        UsbConstants.USB_DIR_IN -> "IN"
        UsbConstants.USB_DIR_OUT -> "OUT"
        else -> "Unknown Direction"
    }
}
