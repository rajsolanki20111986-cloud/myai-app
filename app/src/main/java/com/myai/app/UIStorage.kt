package com.myai.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object UIStorage {

    private const val PREFS_NAME = "myai_prefs"
    private const val UI_KEY = "saved_uis"

    fun save(context: Context, items: List<SavedUI>) {
        val array = JSONArray()

        items.forEach { ui ->
            val obj = JSONObject()
            obj.put("id", ui.id)
            obj.put("name", ui.name)
            obj.put("url", ui.url)
            array.put(obj)
        }

        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(UI_KEY, array.toString())
            .apply()
    }

    fun load(context: Context): MutableList<SavedUI> {
        val result = mutableListOf<SavedUI>()

        val data = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .getString(UI_KEY, null)
            ?: return result

        return try {
            val array = JSONArray(data)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                result.add(
                    SavedUI(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        url = obj.getString("url")
                    )
                )
            }

            result
        } catch (_: Exception) {
            mutableListOf()
        }
    }
}
