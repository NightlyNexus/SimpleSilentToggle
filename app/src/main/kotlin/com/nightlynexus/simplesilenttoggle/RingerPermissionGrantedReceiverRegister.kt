package com.nightlynexus.simplesilenttoggle

import android.app.Application
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

// New Android versions seem mostly never to send this broadcast to my app.
internal class RingerPermissionGrantedReceiverRegister(
  private val application: Application
) {
  private val intentFilter = IntentFilter(
    NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED
  )
  private var registeredReceiver: BroadcastReceiver? = null

  fun registerRingerPermissionGrantedReceiver(
    appWidgetId: Int
  ) {
    unregisterRingerPermissionGrantedReceiver()

    registeredReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        if (notificationManager.isNotificationPolicyAccessGranted) {
          val returnToConfigurationIntent = Intent(
            context,
            ConfigurationActivity::class.java)
            .addFlags(
              Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
          context.startActivity(returnToConfigurationIntent)

          unregisterRingerPermissionGrantedReceiver()
        }
      }
    }
    application.registerReceiver(
      registeredReceiver,
      intentFilter
    )
  }

  fun unregisterRingerPermissionGrantedReceiver() {
    if (registeredReceiver != null) {
      application.unregisterReceiver(registeredReceiver)
      registeredReceiver = null
    }
  }
}
