package com.nightlynexus.simplesilenttoggle

import android.content.SharedPreferences
import androidx.core.content.edit

internal class WidgetDataSaver(
  private val sharedPreferences: SharedPreferences
) {
  fun getRingerStates(appWidgetId: Int): Map<RingerState, RingerState> {
    val encodedKey = encodeKey(appWidgetId)
    val encodedRingerStates = sharedPreferences.getString(encodedKey, "")!!
    if (encodedRingerStates.isEmpty()) {
      return defaultRingerStateMap()
    }
    val decodedRingerStates = decodeRingerStates(encodedRingerStates)
    if (decodedRingerStates == null) {
      // The user manually mangled his saved data.
      return defaultRingerStateMap()
    }
    return ringerStatesMap(decodedRingerStates)
  }

  fun saveRingerStates(
    appWidgetId: Int,
    ringerStates: List<RingerState>
  ) {
    val encodedKey = encodeKey(appWidgetId)
    val encodedRingerStates = encodeRingerStates(ringerStates)
    sharedPreferences.edit {
      putString(encodedKey, encodedRingerStates)
    }
  }

  fun delete(appWidgetId: Int) {
    val encodedKey = encodeKey(appWidgetId)
    sharedPreferences.edit {
      remove(encodedKey)
    }
  }

  fun restore(oldAppWidgetIds: IntArray, newAppWidgetIds: IntArray) {
    require(oldAppWidgetIds.size == newAppWidgetIds.size) {
      "${oldAppWidgetIds.size} != ${newAppWidgetIds.size}"
    }

    val values = ArrayList<String?>(oldAppWidgetIds.size)
    sharedPreferences.edit {
      for (oldAppWidgetId in oldAppWidgetIds) {
        val key = encodeKey(oldAppWidgetId)
        val value = sharedPreferences.getString(key, null)
        values += value
        remove(key)
      }
      for (i in newAppWidgetIds.indices) {
        val newAppWidgetId = newAppWidgetIds[i]
        val key = encodeKey(newAppWidgetId)
        val value = values[i]
        putString(key, value)
      }
    }
  }

  private fun encodeKey(appWidgetId: Int): String {
    return appWidgetId.toString()
  }

  private fun encodeRingerStates(ringerStates: List<RingerState>): String {
    return ringerStates.joinToString(",")
  }

  private fun decodeRingerStates(encodedRingerStates: String): List<RingerState>? {
    return encodedRingerStates.split(",").map {
      try {
        RingerState.valueOf(it)
      } catch (e: IllegalArgumentException) {
        // The user manually mangled his saved data.
        return null
      }
    }
  }

  private fun ringerStatesMap(ringerStates: List<RingerState>): Map<RingerState, RingerState> {
    val size = ringerStates.size
    require(size >= 2)
    val map = LinkedHashMap<RingerState, RingerState>(size)
    for (i in ringerStates.indices) {
      val ringerState = ringerStates[i]
      val nextRingerState = ringerStates[if (i == ringerStates.lastIndex) 0 else i + 1]
      map[ringerState] = nextRingerState
    }
    return map
  }

  private fun defaultRingerStateMap(): Map<RingerState, RingerState> {
    val defaultRingerStateMap = LinkedHashMap<RingerState, RingerState>(2)
    defaultRingerStateMap[RingerState.Normal] = RingerState.Silent
    defaultRingerStateMap[RingerState.Silent] = RingerState.Normal
    return defaultRingerStateMap
  }
}
