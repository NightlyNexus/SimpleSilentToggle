package com.nightlynexus.simplesilenttoggle

import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

internal fun requestRingerPermission(
  context: Context
) {
  Toast.makeText(
    context,
    R.string.permission_toast,
    Toast.LENGTH_SHORT
  ).show()
  val permissionIntent = Intent(
    Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
  ).addFlags(
    Intent.FLAG_ACTIVITY_NEW_TASK or
      Intent.FLAG_ACTIVITY_CLEAR_TOP or
      Intent.FLAG_ACTIVITY_SINGLE_TOP
  )
  context.startActivity(permissionIntent)
}

internal fun updateAllAppWidgetIds(
  context: Context
) {
  val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
  val allAppWidgetIds = allAppWidgetIds(context, appWidgetManager)
  updateAppWidgetIds(
    context,
    appWidgetManager,
    allAppWidgetIds
  )
}

private fun updateAppWidgetIds(
  context: Context,
  appWidgetManager: AppWidgetManager,
  appWidgetIds: IntArray
) {
  val audioManager = context.getSystemService(AudioManager::class.java)
  val ringerMode = audioManager.ringerMode
  val currentRingerState: RingerState
  @DrawableRes val currentIconId: Int
  @StringRes val currentIconContentDescriptionId: Int
  when (ringerMode) {
    AudioManager.RINGER_MODE_NORMAL -> {
      currentRingerState = RingerState.Normal
      currentIconId = R.drawable.ringer_mode_normal
      currentIconContentDescriptionId = R.string.content_description_normal
    }

    AudioManager.RINGER_MODE_VIBRATE -> {
      currentRingerState = RingerState.Vibrate
      currentIconId = R.drawable.ringer_mode_vibrate
      currentIconContentDescriptionId = R.string.content_description_vibrate
    }

    AudioManager.RINGER_MODE_SILENT -> {
      currentRingerState = RingerState.Silent
      currentIconId = R.drawable.ringer_mode_silent
      currentIconContentDescriptionId = R.string.content_description_silent
    }

    else -> {
      currentRingerState = RingerState.Normal
      currentIconId = R.drawable.ringer_mode_normal
      currentIconContentDescriptionId = R.string.content_description_normal
    }
  }
  updateAppWidgetIds(
    context,
    appWidgetManager,
    appWidgetIds,
    currentIconId,
    currentIconContentDescriptionId,
    currentRingerState
  )
}

private fun updateAppWidgetIds(
  context: Context,
  appWidgetManager: AppWidgetManager,
  appWidgetIds: IntArray,
  @DrawableRes currentIconId: Int,
  @StringRes currentIconContentDescriptionId: Int,
  currentRingerState: RingerState
) {
  for (appWidgetId in appWidgetIds) {
    updateAppWidget(
      context,
      appWidgetManager,
      appWidgetId,
      currentIconId,
      currentIconContentDescriptionId,
      currentRingerState
    )
  }
}

private fun updateAppWidget(
  context: Context,
  appWidgetManager: AppWidgetManager,
  appWidgetId: Int,
  @DrawableRes currentIconId: Int,
  @StringRes currentIconContentDescriptionId: Int,
  currentRingerState: RingerState
) {
  val views = RemoteViews(context.packageName, R.layout.widget)

  views.setImageViewResource(
    android.R.id.background,
    currentIconId
  )
  views.setContentDescription(
    android.R.id.background,
    context.getText(currentIconContentDescriptionId)
  )
  val pendingIntent = widgetClickedPendingIntent(
    context,
    currentRingerState,
    appWidgetId
  )
  views.setOnClickPendingIntent(android.R.id.background, pendingIntent)
  appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun widgetClickedPendingIntent(
  context: Context,
  currentRingerState: RingerState,
  appWidgetId: Int
): PendingIntent {
  val intent = Intent(context, ToggleClickedReceiver::class.java)
  intent.putExtra(EXTRA_LAST_RINGER_STATE, currentRingerState.name)
  intent.putExtra(EXTRA_APP_WIDGET_ID, appWidgetId)

  val requestCode = appWidgetId
  intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
  val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
    PendingIntent.FLAG_IMMUTABLE
  val pendingIntent = PendingIntent.getBroadcast(
    context,
    requestCode,
    intent,
    pendingIntentFlags
  )
  return pendingIntent
}

private fun allAppWidgetIds(
  context: Context,
  appWidgetManager: AppWidgetManager
): IntArray {
  return appWidgetManager.getAppWidgetIds(
    ComponentName(
      context,
      SimpleSilentToggleAppWidgetProvider::class.java
    )
  )
}

private const val EXTRA_LAST_RINGER_STATE = "EXTRA_LAST_RINGER_STATE"
private const val EXTRA_APP_WIDGET_ID = "EXTRA_APP_WIDGET_ID"

class ToggleClickedReceiver : BroadcastReceiver() {
  private lateinit var widgetDataSaver: WidgetDataSaver
  private lateinit var ringerPermissionGrantedReceiverRegister: RingerPermissionGrantedReceiverRegister

  override fun onReceive(context: Context, intent: Intent) {
    val app = context.applicationContext as SimpleSilentToggleApplication
    widgetDataSaver = app.widgetDataSaver
    ringerPermissionGrantedReceiverRegister = app.ringerPermissionGrantedReceiverRegister

    val lastRingerState = RingerState.valueOf(
      intent.getStringExtra(
        EXTRA_LAST_RINGER_STATE
      )!!
    )
    val appWidgetId = intent.getIntExtra(
      EXTRA_APP_WIDGET_ID,
      AppWidgetManager.INVALID_APPWIDGET_ID
    )
    check(appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID)

    val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
    val allAppWidgetIds = allAppWidgetIds(context, appWidgetManager)
    val audioManager = context.getSystemService(AudioManager::class.java)
    val ringerMode = audioManager.ringerMode

    val currentRingerState: RingerState
    @DrawableRes val currentIconId: Int
    @StringRes val currentIconContentDescriptionId: Int
    when (ringerMode) {
      AudioManager.RINGER_MODE_NORMAL -> {
        currentRingerState = RingerState.Normal
        currentIconId = R.drawable.ringer_mode_normal
        currentIconContentDescriptionId = R.string.content_description_normal
      }

      AudioManager.RINGER_MODE_VIBRATE -> {
        currentRingerState = RingerState.Vibrate
        currentIconId = R.drawable.ringer_mode_vibrate
        currentIconContentDescriptionId = R.string.content_description_vibrate
      }

      AudioManager.RINGER_MODE_SILENT -> {
        currentRingerState = RingerState.Silent
        currentIconId = R.drawable.ringer_mode_silent
        currentIconContentDescriptionId = R.string.content_description_silent
      }

      else -> {
        currentRingerState = RingerState.Normal
        currentIconId = R.drawable.ringer_mode_normal
        currentIconContentDescriptionId = R.string.content_description_normal
      }
    }

    if (lastRingerState === currentRingerState) {
      val notificationManager = context.getSystemService(NotificationManager::class.java)
      if (notificationManager.isNotificationPolicyAccessGranted) {
        val ringerStates = widgetDataSaver.getRingerStates(appWidgetId)
        var nextRingerState = ringerStates[currentRingerState]
        if (nextRingerState == null) {
          // The current ringer state is not a ringer state of this widget.
          // Set the ringer to the first ringer state of this widget.
          nextRingerState = ringerStates.iterator().next().key
        }

        @DrawableRes val nextIconId: Int
        @StringRes val nextIconContentDescriptionId: Int
        val nextRingerMode: Int
        when (nextRingerState) {
          RingerState.Normal -> {
            nextIconId = R.drawable.ringer_mode_normal
            nextIconContentDescriptionId = R.string.content_description_normal
            nextRingerMode = AudioManager.RINGER_MODE_NORMAL
          }

          RingerState.Vibrate -> {
            nextIconId = R.drawable.ringer_mode_vibrate
            nextIconContentDescriptionId = R.string.content_description_vibrate
            nextRingerMode = AudioManager.RINGER_MODE_VIBRATE
          }

          RingerState.Silent -> {
            nextIconId = R.drawable.ringer_mode_silent
            nextIconContentDescriptionId = R.string.content_description_silent
            nextRingerMode = AudioManager.RINGER_MODE_SILENT
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
          }
        }

        audioManager.ringerMode = nextRingerMode
        updateAppWidgetIds(
          context,
          appWidgetManager,
          allAppWidgetIds,
          nextIconId,
          nextIconContentDescriptionId,
          nextRingerState
        )
      } else {
        ringerPermissionGrantedReceiverRegister.unregisterRingerPermissionGrantedReceiver()
        requestRingerPermission(context)
      }
    } else {
      // The last ringer state we recorded is not the current ringer state.
      // Update all the widgets, do not make a ringer state change, and instead notify the user.
      updateAppWidgetIds(
        context,
        appWidgetManager,
        allAppWidgetIds,
        currentIconId,
        currentIconContentDescriptionId,
        currentRingerState
      )
      notifyNoChange(context)
    }
  }

  private fun notifyNoChange(
    context: Context
  ) {
    Toast.makeText(
      context,
      R.string.widget_no_change_toast,
      Toast.LENGTH_SHORT
    ).show()
  }
}

class SimpleSilentToggleAppWidgetProvider : AppWidgetProvider() {
  private lateinit var widgetDataSaver: WidgetDataSaver

  override fun onReceive(context: Context, intent: Intent) {
    val app = context.applicationContext as SimpleSilentToggleApplication
    widgetDataSaver = app.widgetDataSaver
    super.onReceive(context, intent)
  }

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    updateAppWidgetIds(
      context,
      appWidgetManager,
      appWidgetIds
    )
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    for (appWidgetId in appWidgetIds) {
      widgetDataSaver.delete(appWidgetId)
    }
  }

  override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
    widgetDataSaver.restore(oldWidgetIds, newWidgetIds)
  }
}
