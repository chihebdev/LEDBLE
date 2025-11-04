This app is designed to interface with the Clockwise firmware and simulate its configuration structure over Bluetooth Low Energy (BLE).
The BLE configuration layer is not yet implemented in the current Clockwise firmware this app serves primarily as a testing and development tool to validate the communication model, UUID mapping, and user interface for future firmware integration.


Reference file: ClockwiseWebServer.h
(Used as the source for defining configuration parameters, but currently only implements HTTP-based control. The BLE equivalent is under development.)


The app mirrors the same configuration fields defined in the firmware’s HTTP interface, allowing developers to test how BLE-based configuration will behave once implemented in the ESP32 firmware.




 | Category           | Parameter                                          | Description                  | Access     |
| ------------------ | -------------------------------------------------- | ---------------------------- | ---------- |
| **Display**        | `displayBright`                                    | Screen brightness            | Read/Write |
|                    | `autoBrightMin`, `autoBrightMax`                   | Auto brightness range        | Read/Write |
|                    | `swapBlueGreen`, `swapBlueRed`                     | Color swap flags             | Read/Write |
|                    | `use24hFormat`                                     | 24-hour clock toggle         | Read/Write |
|                    | `displayRotation`, `driver`, `i2cSpeed`, `E_pin`   | Display driver parameters    | Read/Write |
| **Network**        | `wifiSsid`, `wifiPwd`                              | Wi-Fi credentials            | Read/Write |
|                    | `timeZone`, `ntpServer`, `manualPosix`             | Time synchronization         | Read/Write |
| **Display Server** | `canvasFile`, `canvasServer`                       | Remote content configuration | Read/Write |
| **Device Info**    | `firmwareVersion`, `firmwareName`, `clockfaceName` | Firmware metadata            | Read Only  |
| **System**         | `restart`                                          | Triggers device restart      | Write      |



📦 Installation & Permissions

You can download and install the latest test build of the app directly from the repository:

Download: [Download app-debug.apk](https://github.com/chihebdev/LEDBLE/raw/master/app-debug.apk)


To install manually:

Download the .apk file to your Android device.

Open it from your Downloads folder or file manager.

If prompted, allow “Install unknown apps” for your browser or file manager.

Once installed, open Clockwise BLE Config from your app drawer.

Required Permissions

The app requires Bluetooth and Location permissions to scan and connect to BLE devices.
If the app doesn’t automatically prompt for them, go to:

Settings → Apps → Clockwise BLE Config → Permissions

and ensure the following are allowed:

Nearby devices (Bluetooth)

Location access (required for BLE scanning on Android)

Without these permissions, the app will not be able to discover or connect to your ESP32 device.
