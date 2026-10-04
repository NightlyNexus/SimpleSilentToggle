package com.nightlynexus.simplesilenttoggle

import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity

// TODO: Support ordering the ringer states.
class ConfigurationActivity : AppCompatActivity() {
  private lateinit var widgetDataSaver: WidgetDataSaver
  private lateinit var ringerPermissionGrantedReceiverRegister: RingerPermissionGrantedReceiverRegister

  override fun onCreate(savedInstanceState: Bundle?) {
    val app = application as SimpleSilentToggleApplication
    widgetDataSaver = app.widgetDataSaver
    ringerPermissionGrantedReceiverRegister = app.ringerPermissionGrantedReceiverRegister
    super.onCreate(savedInstanceState)

    val extras = intent.extras
    if (extras == null) {
      // This happened on a 2025 Moto G running SDK 36.
      // There is nothing to do without the appwidget id from the intent extras, though.
      finish()
      return
    }
    val appWidgetId = extras.getInt(
      AppWidgetManager.EXTRA_APPWIDGET_ID,
      AppWidgetManager.INVALID_APPWIDGET_ID
    )
    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      // This happens in production on some Chinese devices.
      // When does this happen? How do we get the appWidgetId? What behavior does this now have?
      // Hopefully, these devices try launching the configuration activity again.
      finish()
      return
    }
    val resultValue = Intent().putExtra(
      AppWidgetManager.EXTRA_APPWIDGET_ID,
      appWidgetId
    )
    setResult(RESULT_CANCELED, resultValue)

    setContentView(R.layout.activity_configuration)
    val optionNormal = findViewById<CheckBox>(R.id.option_normal)
    val optionVibrate = findViewById<CheckBox>(R.id.option_vibrate)
    val optionSilent = findViewById<CheckBox>(R.id.option_silent)
    val addWidgetButton = findViewById<View>(R.id.add_widget)

    val notificationManager = getSystemService(NotificationManager::class.java)
    if (!notificationManager.isNotificationPolicyAccessGranted) {
      ringerPermissionGrantedReceiverRegister.registerRingerPermissionGrantedReceiver(appWidgetId)
      requestRingerPermission(this)
    }

    addWidgetButton.isEnabled = false
    var optionsChecked = 0
    val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        optionsChecked++
      } else {
        optionsChecked--
      }
      addWidgetButton.isEnabled = optionsChecked >= 2
    }
    optionNormal.setOnCheckedChangeListener(onCheckedChangeListener)
    optionVibrate.setOnCheckedChangeListener(onCheckedChangeListener)
    optionSilent.setOnCheckedChangeListener(onCheckedChangeListener)

    val ringerStates = widgetDataSaver.getRingerStates(appWidgetId)
    for (ringerState in ringerStates) {
      val option = when (ringerState.key) {
        RingerState.Normal -> optionNormal
        RingerState.Vibrate -> optionVibrate
        RingerState.Silent -> optionSilent
      }
      option.isChecked = true
    }

    addWidgetButton.setOnClickListener {
      val ringerStates = ArrayList<RingerState>(3)
      if (optionNormal.isChecked) {
        ringerStates += RingerState.Normal
      }
      if (optionVibrate.isChecked) {
        ringerStates += RingerState.Vibrate
      }
      if (optionSilent.isChecked) {
        ringerStates += RingerState.Silent
      }
      check(ringerStates.size >= 2)
      widgetDataSaver.saveRingerStates(
        appWidgetId,
        ringerStates
      )

      updateAllAppWidgetIds(this)

      setResult(RESULT_OK, resultValue)
      finish()
    }
  }
}
