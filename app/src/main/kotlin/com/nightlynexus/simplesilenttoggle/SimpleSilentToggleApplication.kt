package com.nightlynexus.simplesilenttoggle

import android.app.Application

class SimpleSilentToggleApplication : Application() {
  internal lateinit var widgetDataSaver: WidgetDataSaver
  internal lateinit var ringerPermissionGrantedReceiverRegister: RingerPermissionGrantedReceiverRegister

  override fun onCreate() {
    super.onCreate()
    widgetDataSaver = WidgetDataSaver(
      getSharedPreferences("widget", MODE_PRIVATE)
    )
    ringerPermissionGrantedReceiverRegister = RingerPermissionGrantedReceiverRegister(
      this
    )
  }
}
