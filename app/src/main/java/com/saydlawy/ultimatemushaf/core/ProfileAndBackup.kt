package com.saydlawy.ultimatemushaf.core

import android.content.Context
import org.json.JSONObject
import java.io.File

data class LocalProfile(
    val id: String,
    val name: String,
    val isChild: Boolean = false,
    val isTeacher: Boolean = false
)

class ProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("ultimate_profiles", Context.MODE_PRIVATE)

    fun active(): LocalProfile {
        val id = prefs.getString("active_id", "default") ?: "default"
        val name = prefs.getString("name_" + id, "المستخدم") ?: "المستخدم"
        return LocalProfile(id, name, prefs.getBoolean("child_" + id, false), prefs.getBoolean("teacher_" + id, false))
    }

    fun save(profile: LocalProfile) {
        prefs.edit()
            .putString("active_id", profile.id)
            .putString("name_" + profile.id, profile.name)
            .putBoolean("child_" + profile.id, profile.isChild)
            .putBoolean("teacher_" + profile.id, profile.isTeacher)
            .apply()
    }
}

class BackupManager(private val context: Context, private val db: UltimateDatabase) {
    fun export(): File {
        val target = File(context.cacheDir, "ultimate-mushaf-backup.json")
        val root = JSONObject()
        val position = db.loadPosition()
        root.put("schema_version", 1)
        root.put("reading_position", JSONObject().apply {
            put("page", position.page)
            put("verse", position.verseKey)
            put("line", position.line)
            put("updated_at", position.updatedAt)
        })
        val bookmarks = org.json.JSONArray()
        db.bookmarks().forEach {
            bookmarks.put(JSONObject().apply {
                put("id", it.id); put("verse", it.verseKey); put("page", it.page)
                put("title", it.title); put("folder", it.folder); put("tags", it.tags.joinToString("|"))
                put("created_at", it.createdAt); put("permanent", it.permanent)
            })
        }
        root.put("bookmarks", bookmarks)
        target.writeText(root.toString())
        return target
    }
}
