USB Enumerator Sample
=====================
This application demonstrates accessing the `UsbManager` Android API from within
and Android Things application. The sample application iterates over all the
USB devices discovered by the host and prints their interfaces and endpoints.

Pre-requisites
--------------

- Android Things compatible board
- Android Studio 2.2+

Getting Started
---------------

Import the project using Android Studio and deploy it to your device. The
application prints the list of connected USB devices. Connect any USB peripheral
to one of the host ports on your device. The application connects to that
specific device, parses its descriptors, and prints out more detailed information
about its configuration.

Behavior Differences
--------------------

The Android [USB Host](https://developer.android.com/guide/topics/connectivity/usb/host.html)
APIs behave differently on Android Things because user-granted permission dialogs
are not enabled. This sample takes these changes into account, and does not
request device access permissions that would otherwise be required on an Android
mobile device.

License
-------

Copyright 2017 The Android Open Source Project, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
