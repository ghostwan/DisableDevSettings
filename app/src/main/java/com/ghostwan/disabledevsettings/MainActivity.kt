package com.ghostwan.disabledevsettings

import android.app.ActivityManager
import android.content.Context
import android.content.pm.IPackageDataObserver
import android.content.pm.IPackageDeleteObserver
import android.hardware.usb.IUsbManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.ServiceManager
import android.provider.Settings
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.Global.putInt(contentResolver, Settings.Global.ADB_ENABLED, 0)
        Settings.Global.putInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        val clearAppMethod = packageManager.javaClass.getMethod("clearApplicationUserData", String::class.java, IPackageDataObserver::class.java)
        clearAppMethod.invoke(packageManager,"com.android.settings", object : IPackageDataObserver {
            override fun onRemoveCompleted(packageName: String?, succeeded: Boolean) {
            }

            override fun asBinder(): IBinder {
                return Binder()
            }
        })
        val activityManager : ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val forceStopAppMethod = activityManager.javaClass.getMethod("forceStopPackage", String::class.java)
        forceStopAppMethod.invoke(activityManager,"com.android.settings")

        val usbManager = IUsbManager.Stub.asInterface(ServiceManager.getService(Context.USB_SERVICE))
        usbManager.clearUsbDebuggingKeys()

        val deletePackage = packageManager.javaClass.getMethod("deletePackage", String::class.java, IPackageDeleteObserver::class.java, Int::class.java)
        deletePackage.invoke(packageManager, BuildConfig.APPLICATION_ID, object : IPackageDeleteObserver {
            override fun packageDeleted(p0: String?, p1: Int) {
            }

            override fun asBinder(): IBinder {
                return Binder()
            }

        }, 0x00000004)
    }
}