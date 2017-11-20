Parse on Buddy Android SDK Alternative Data Collection and Upload
=================================================================

The Parse on Buddy SDK collects various sorts of device data. Device data collection and uploading are intiated by the reception of notifcations from the device, and by periodic timers created and managed by the SDK.

The SDK registers for location, networking, and power change notifications, and is notifed of changes to any of these areas. Separately, timers fire periodically to collect telephony and battery information, the latter collected for diagnostic purposes.

The SDK reacts to notifications and timers for both data collection and uploading. These are the notifications that the SDK registers to receive:

* Intent.ACTION_POWER_CONNECTED
* Intent.ACTION_POWER_DISCONNECTED
* Intent.ACTION_BATTERY_LOW
* Intent.ACTION_BATTERY_OKAY
* WifiManager.NETWORK_STATE_CHANGED_ACTION
* ConnectivityManager.CONNECTIVITY_ACTION

Depending on the version of the Android OS installed on the device, one or more of these notifications may not fire.


Data Collection
---------------

The Parse on Buddy Android SDK collects different sorts of device data. It uses the following Android OS namespaces/APIs to collect information:

### Device Build Information

* android.os.Build.BRAND
* android.os.Build.MODEL
* android.os.Build.BOARD
* android.os.Build.DEVICE
* android.os.Build.DISPLAY
* android.os.Build.FINGERPRINT
* android.os.Build.BOOTLOADER
* android.os.Build.HARDWARE
* android.os.Build.HOST
* android.os.Build.MANUFACTURER
* android.os.Build.PRODUCT
* android.os.Build.VERSION.RELEASE
* android.os.Build.VERSION.SDK_INT

### Device Telephony Information

* a*ndroid.telephony.CellIdentityCdma
* android.telephony.CellIdentityGsm
* android.telephony.CellIdentityLte
* android.telephony.CellIdentityWcdma
* android.telephony.CellInfo
* android.telephony.CellInfoCdma
* android.telephony.CellInfoGsm
* android.telephony.CellInfoLte
* android.telephony.CellInfoWcdma
* android.telephony.CellLocation
* android.telephony.CellSignalStrengthCdma
* android.telephony.CellSignalStrengthGsm
* android.telephony.CellSignalStrengthLte
* android.telephony.CellSignalStrengthWcdma
* android.telephony.TelephonyManager
* android.telephony.cdma.CdmaCellLocation
* android.telephony.gsm.GsmCellLocation

### Device Identification Information

Settings.Secure.ANDROID_ID

### Location Information

The Parse on Buddy SDK registers to receive periodic location information. This information is of two types: location and activity. Location information consists of the following:

* Latitude
* Longitude
* Accuracy
* Altitude
* Bearing
* Bearing Accuracy
* Speed
* Speed Accuracy
* Vertical Accuracy

Activity information is the Android OS's best-guess as to the kind of motion the device is engaged in. The Android OS sends the following possible activities:

* DetectedActivity.IN_VEHICLE
* DetectedActivity.ON_BICYCLE
* DetectedActivity.ON_FOOT
* DetectedActivity.RUNNING
* DetectedActivity.STILL
* DetectedActivity.TILTING
* DetectedActivity.WALKING
* DetectedActivity.UNKNOWN


Data Upload
-----------

Uploading is optimized for battery life. The SDK uses the following set of heuristics to determine the best time to upload:

Upon any location, networking or power change notifications, check if the device is under power (plugged in) and on Wi-Fi. If so, upload. If not, continue to listen to notifications. If a maximum of 6 hours occurs while not powered and not on Wi-Fi, attempt upload, unless the battery level is below 20%. After a successful upload, reset the waiting period to 6 hours and start the process over again. 6 hours is the current default, which may change in the future.


Collection and Upload configuration
-----------------------------------

All the operations of collection and upload can be remotely configured in a granular fashion. This enables Parse on Buddy to optimize specific scenarios if needed. The configuration file is located at https://cdn.parse.buddy.com/sdk/config.json.