package com.veio.browser

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local storage manager for Veio browsing history.
 *
 * Keeps the existing SharedPreferences name and JSON format
 * so previously saved history remains compatible.
 */
class HistoryStore(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(
            "veio_browser",
            Context.MODE_PRIVATE
        )

    data class Entry(
        val url: String,
        val title: String
    )

    /**
     * Saves a visited page at the beginning of the history.
     * Duplicate URLs are removed and the history is limited
     * to the latest 100 entries.
     */
    fun save(url: String, title: String) {
        val normalizedUrl = url.trim()
        if (normalizedUrl.isEmpty()) return

        val oldEntries = readEntries()
        val updatedEntries = mutableListOf<Entry>()

        updatedEntries.add(
            Entry(
                url = normalizedUrl,
                title = title.trim().ifEmpty { normalizedUrl }
            )
        )

        for (entry in oldEntries) {
            if (entry.url.equals(normalizedUrl, ignoreCase = true)) {
                continue
            }

            updatedEntries.add(entry)

            if (updatedEntries.size >= MAX_ENTRIES) {
                break
            }
        }

        writeEntries(updatedEntries)
    }

    /**
     * Returns history entries from newest to oldest.
     */
    fun getAll(): List<Entry> = readEntries()

    /**
     * Deletes every history entry matching the given URL.
     */
    fun delete(url: String) {
        val normalizedUrl = url.trim()
        if (normalizedUrl.isEmpty()) return

        writeEntries(
            readEntries().filterNot {
                it.url.equals(normalizedUrl, ignoreCase = true)
            }
        )
    }

    /**
     * Clears the entire browsing history.
     */
    fun clear() {
        preferences.edit()
            .putString(HISTORY_KEY, "[]")
            .apply()
    }

    /**
     * Reads the existing JSON history format safely.
     */
    private fun readEntries(): List<Entry> {
        val history = try {
            JSONArray(
                preferences.getString(HISTORY_KEY, "[]")
            )
        } catch (_: Exception) {
            JSONArray()
        }

        val entries = mutableListOf<Entry>()

        for (index in 0 until history.length()) {
            val item = history.optJSONObject(index) ?: continue
            val url = item.optString("url").trim()

            if (url.isEmpty()) continue

            val title = item.optString("title")
                .trim()
                .ifEmpty { url }

            entries.add(
                Entry(
                    url = url,
                    title = title
                )
            )
        }

        return entries
    }

    /**
     * Writes entries using the same JSON keys as the old code.
     */
    private fun writeEntries(entries: List<Entry>) {
        val json = JSONArray()

        entries.take(MAX_ENTRIES).forEach { entry ->
            json.put(
                JSONObject().apply {
                    put("url", entry.url)
                    put("title", entry.title)
                }
            )
        }

        preferences.edit()
            .putString(HISTORY_KEY, json.toString())
            .apply()
    }

    private companion object {
        const val HISTORY_KEY = "history"
        const val MAX_ENTRIES = 100
    }
}
