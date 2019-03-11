# Goal

This app deactivate ADB, the development mode, revoke ADB authorisation and remove itself.

The goal is to secure an android device before it goes in production.

# How to Build

You need to sign your app with the platform signature of your device. To do so
set environment variables or put them in the local.properties:

```properties
KEYSTORE_FILE=/myuser/mykeystores/platform.keystore
KEYSTORE_PASSWORD=xxxxxx
KEYSTORE_ALIAS=yyyyyy
ALIAS_PASSWORD=zzzzzz
```

Then

```bash
./gradlew assembleRelease
```


# How to work with hidden/system APIs

This project shows how to use system / hidden API in Android.

For most API you need to sign your application with the platform certificate (keystore) of your device:

For AOSP,  you can find the platform certificates here : 

[AOSP repository](https://github.com/aosp-mirror/platform_build/tree/master/target/product/security)
You can transform platform.pk8 and platform.x509.pem in a keystore by doing:
```bash
openssl pkcs8 -inform DER -nocrypt -in platform.pk8 -out key.pem

openssl pkcs12 -export -in platform.x509.pem -inkey key.pem -out platform.p12 -password pass:my_key_password -name aosp_platform_key

keytool -importkeystore -deststorepass my_keystore_password -destkeystore platform.keystore -srckeystore platform.p12 -srcstoretype PKCS12 -srcstorepass my_key_password

keytool -list -v -keystore platform.keystore
```

To know more about those certificate see:

[Android security part 1: application signatures & permissions](https://boundarydevices.com/android-security-part-1-application-signatures-permissions/)

Because most of the API are hidden and not expose in the official SDK you need to either call those API in reflexion [1], use AIDL [2], create a stub [3] or hack the Android SDK [4] with for example :

[https://github.com/anggrayudi/android-hidden-api](https://github.com/anggrayudi/android-hidden-api)

What I'm used to do usually, when I'm working locally, to ease the find of API, is to look into the Settings App for the API I need :

[https://github.com/aosp-mirror/platform_packages_apps_settings](https://github.com/aosp-mirror/platform_packages_apps_settings)

And then I hack the SDK [4] to test the functionnality I want. 

```kotlin
val activityManager : ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
activityManager.forceStopPackage("com.android.settings")

val usbManager = IUsbManager.Stub.asInterface(ServiceManager.getService(Context.USB_SERVICE))
usbManager.clearUsbDebuggingKeys()
```

Then for production I use both reflexion [1], AIDL [2] and stub [3]


```kotlin
val activityManager : ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
// Reflexion calls
val forceStopAppMethod = activityManager.javaClass.getMethod("forceStopPackage", String::class.java)
forceStopAppMethod.invoke(activityManager,"com.android.settings")


// Use of ServiceManager.getService stub
val usbService = ServiceManager.getService(Context.USB_SERVICE)
// Use of IUsbManager AIDL
val usbManager = IUsbManager.Stub.asInterface()
usbManager.clearUsbDebuggingKeys()
```





