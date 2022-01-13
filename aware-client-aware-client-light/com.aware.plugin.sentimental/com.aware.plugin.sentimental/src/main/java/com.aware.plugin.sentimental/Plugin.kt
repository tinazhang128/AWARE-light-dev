package com.aware.plugin.sentimental

import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.aware.Applications
import com.aware.Aware
import com.aware.Aware_Preferences
import com.aware.providers.Applications_Provider
import com.aware.providers.Keyboard_Provider
import com.aware.utils.Aware_Plugin

open class Plugin : Aware_Plugin() {

    companion object {
        const val PACKAGE_NAME = "packageName"
        const val TYPED_TEXT = "typedText"

        interface AWARESensorObserver {
            fun onTextContextChanged(data: ContentValues)
        }

        var awareSensor: AWARESensorObserver ?= null

        fun setSensorObserver(observer: AWARESensorObserver) {
            awareSensor = observer
        }

        fun getSensorObserver(): AWARESensorObserver {
            return awareSensor!!
        }
    }

    /**
     * The package name of where the keyboard was interesting to track
     */
    var keyboardInApp = ""

    /**
     * List that contains the device's installed keyboard methods
     */
    var installedKeyboards = ""

    /**
     * Where we keep the buffer of written text
     */
    var textBuffer = ""

    override fun onCreate() {
        super.onCreate()
        TAG = "AWARE: Sentimental"

        val usingInput = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        installedKeyboards = usingInput.enabledInputMethodList.toString()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (PERMISSIONS_OK) {

            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true")
            Aware.setSetting(this, Settings.STATUS_PLUGIN_SENTIMENTAL, true)

            if (Applications.isAccessibilityServiceActive(this)) {
                Aware.setSetting(this, Aware_Preferences.STATUS_APPLICATIONS, true)
                Aware.startKeyboard(this)
                Applications.setSensorObserver(object : Applications.AWARESensorObserver {
                    override fun onCrash(data: ContentValues?) {}
                    override fun onNotification(data: ContentValues?) {}
                    override fun onBackground(data: ContentValues?) {}
                    override fun onKeyboard(data: ContentValues?) {
                        val packagesOfInterest = Aware.getSetting(applicationContext, Settings.PLUGIN_SENTIMENTAL_PACKAGES).split(",")
                        if (packagesOfInterest.contains(data!!.getAsString(Keyboard_Provider.Keyboard_Data.PACKAGE_NAME))) {
                            keyboardInApp = data!!.getAsString(Keyboard_Provider.Keyboard_Data.PACKAGE_NAME)
                            textBuffer =textBuffer.plus(". ").plus(data.getAsString(Keyboard_Provider.Keyboard_Data.CURRENT_TEXT))
                        }
                    }

                    override fun onTouch(data: ContentValues?) {}
                    override fun onForeground(data: ContentValues?) {

                        val currentApp = data!!.getAsString(Applications_Provider.Applications_Foreground.PACKAGE_NAME)
                        if (installedKeyboards.contains(currentApp)) return //we ignore foreground package of keyboard input

                        if (!textBuffer.isEmpty() && currentApp != keyboardInApp) { //we were using an app of interest and changed app

                            val contentValues = ContentValues()
                            contentValues.put(Plugin.PACKAGE_NAME, keyboardInApp)
                            contentValues.put(Plugin.TYPED_TEXT, textBuffer)

                            Plugin.awareSensor?.onTextContextChanged(contentValues)

                            textBuffer = ""
                            keyboardInApp = ""
                        }
                    }
                })
            }
        }

        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Aware.setSetting(this, Aware_Preferences.STATUS_APPLICATIONS, false)
        Aware.setSetting(this, Settings.STATUS_PLUGIN_SENTIMENTAL, false)
        Aware.stopKeyboard(this)
    }
}
