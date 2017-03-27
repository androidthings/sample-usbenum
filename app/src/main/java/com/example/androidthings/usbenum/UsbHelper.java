package com.example.androidthings.usbenum;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;

import java.util.Locale;

/**
 * Parsing utility for USB device configuration info.
 */
public class UsbHelper {

    /* Descriptor header sizes */
    private static final int DESC_SIZE_DEVICE = 18;
    private static final int DESC_SIZE_CONFIG = 9;
    /* USB descriptor type values */
    private static final int DESC_TYPE_DEVICE = 0x01;
    private static final int DESC_TYPE_CONFIG = 0x02;
    private static final int DESC_TYPE_INTERFACE = 0x04;
    private static final int DESC_TYPE_ENDPOINT = 0x05;
    /* USB power attribute flags */
    private static final int ATTR_BUSPOWER = 0x80;
    private static final int ATTR_SELFPOWER = 0x40;
    private static final int ATTR_REMOTEWAKE = 0x20;

    /**
     * Request the active configuration descriptor through the USB device connection.
     */
    public static class ConfigurationDescriptor {
        // Type: Indicates whether this is a read or write
        //  Matches USB_ENDPOINT_DIR_MASK for either IN or OUT
        private static final int REQUEST_TYPE = 0x80;
        // Request: GET_DESCRIPTOR = 0x06
        private static final int REQUEST = 0x06;
        // Value: Descriptor Type (High) and Index (Low)
        //  Configuration Descriptor = 0x2
        //  Index = 0x0 (First configuration)
        private static final int REQ_VALUE = 0x200;
        private static final int REQ_INDEX = 0x00;
        private static final int LENGTH = 64;

        public static byte[] executeRequest(UsbDeviceConnection connection) {
            //Create a sufficiently large buffer for incoming data
            byte[] buffer = new byte[LENGTH];

            connection.controlTransfer(REQUEST_TYPE, REQUEST, REQ_VALUE, REQ_INDEX,
                    buffer, LENGTH, 2000);

            return buffer;
        }

        /**
         * Parse the USB configuration descriptor response per the USB Specification.
         * Return a printable description of the connected device.
         *
         * @param buffer Raw configuration descriptor from the device.
         * @return String description of the device configuration.
         */
        public static String parseResponse(byte[] buffer) {
            if (buffer[0] != DESC_SIZE_CONFIG || buffer[1] != DESC_TYPE_CONFIG) {
                throw new IllegalArgumentException("Invalid configuration descriptor");
            }

            StringBuilder sb = new StringBuilder();
            // Parse configuration descriptor header
            int totalLength = (buffer[3] & 0xFF) << 8;
            totalLength += (buffer[2] & 0xFF);
            // Interface count
            int numInterfaces = (buffer[5] & 0xFF);
            // Configuration attributes
            int attributes = (buffer[7] & 0xFF);
            // Power is given in 2mA increments
            int maxPower = (buffer[8] & 0xFF) * 2;

            sb.append("Configuration Descriptor:\n");
            sb.append("Length: ")
                    .append(totalLength)
                    .append(" bytes\n");
            sb.append(numInterfaces)
                    .append(" Interfaces\n");
            sb.append(String.format("Attributes:%s%s%s\n",
                    (attributes & ATTR_BUSPOWER) == ATTR_BUSPOWER ? " BusPowered" : "",
                    (attributes & ATTR_SELFPOWER) == ATTR_SELFPOWER ? " SelfPowered" : "",
                    (attributes & ATTR_REMOTEWAKE) == ATTR_REMOTEWAKE ? " RemoteWakeup" : ""));
            sb.append("Max Power: ")
                    .append(maxPower)
                    .append("mA\n");

            // The rest of the descriptor is interfaces and endpoints
            int index = DESC_SIZE_CONFIG;
            while (index < totalLength) {
                //Read length and type
                int len = (buffer[index] & 0xFF);
                int type = (buffer[index + 1] & 0xFF);
                switch (type) {
                    case DESC_TYPE_INTERFACE:
                        int intfNumber = (buffer[index + 2] & 0xFF);
                        int numEndpoints = (buffer[index + 4] & 0xFF);
                        int intfClass = (buffer[index + 5] & 0xFF);

                        sb.append(String.format(Locale.US, "-- Interface %d, %s, %d Endpoints\n",
                                intfNumber, nameForClass(intfClass), numEndpoints));
                        break;
                    case DESC_TYPE_ENDPOINT:
                        int endpointAddr = ((buffer[index + 2] & 0xFF));
                        //Number is lower 4 bits
                        int endpointNum = (endpointAddr & 0x0F);
                        //Direction is high bit
                        int direction = (endpointAddr & 0x80);

                        int endpointAttrs = (buffer[index + 3] & 0xFF);
                        //Type is the lower two bits
                        int endpointType = (endpointAttrs & 0x3);

                        sb.append(String.format(Locale.US, "   -- Endpoint %d, %s %s\n",
                                endpointNum,
                                nameForEndpointType(endpointType),
                                nameForDirection(direction)));
                        break;
                }
                //Advance to next descriptor
                index += len;
            }

            return sb.toString();
        }
    }

    /**
     * Request the device descriptor through the USB device connection.
     */
    public static class DeviceDescriptor {
        // Type: Indicates whether this is a read or write
        //  Matches USB_ENDPOINT_DIR_MASK for either IN or OUT
        private static final int REQUEST_TYPE = 0x80;
        // Request: GET_DESCRIPTOR = 0x06
        private static final int REQUEST = 0x06;
        // Value: Descriptor Type (High) and Index (Low)
        //  Device Descriptor = 0x1
        //  Index = 0x0
        private static final int REQ_VALUE = 0x100;
        private static final int REQ_INDEX = 0x00;
        private static final int LENGTH = 32;

        public static byte[] executeRequest(UsbDeviceConnection connection) {
            byte[] buffer = new byte[LENGTH];

            connection.controlTransfer(REQUEST_TYPE, REQUEST, REQ_VALUE, REQ_INDEX,
                    buffer, LENGTH, 2000);

            return buffer;
        }

        /**
         * Parse the USB device descriptor response per the USB specification.
         * Return a printable description of the connected device.
         */
        public static String parseResponse(byte[] buffer) {
            if (buffer[0] != DESC_SIZE_DEVICE || buffer[1] != DESC_TYPE_DEVICE) {
                throw new IllegalArgumentException("Invalid device descriptor");
            }

            StringBuilder sb = new StringBuilder();
            // Parse device descriptor header
            int deviceClass = (buffer[4] & 0xFF);
            int deviceSublass = (buffer[5] & 0xFF);
            int deviceProtocol = (buffer[6] & 0xFF);
            // Maximum packet size for control transfers
            int maxPacketSize = (buffer[7] & 0xFF);
            // USB.org assigned vendor id
            int vendorId = (buffer[9] & 0xFF) << 8;
            vendorId += (buffer[8] & 0xFF);
            // Manufacturer assigned product id
            int productId = (buffer[11] & 0xFF) << 8;
            productId += (buffer[10] & 0xFF);

            sb.append("Device Descriptor:\n");
            sb.append(String.format(
                    "Device Class: %s -> Subclass: 0x%02x -> Protocol: 0x%02x\n",
                    nameForClass(deviceClass), deviceSublass, deviceProtocol));
            sb.append("Max Packet Size: ")
                    .append(maxPacketSize)
                    .append("\n");
            sb.append(String.format("VID: 0x%04x\nPID: 0x%04x\n", vendorId, productId));

            return sb.toString();
        }
    }

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

    private static String nameForClass(int classType) {
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

    private static String nameForEndpointType(int type) {
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

    private static String nameForDirection(int direction) {
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
