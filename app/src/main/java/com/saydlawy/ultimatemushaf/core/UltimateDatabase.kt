package com.saydlawy.ultimatemushaf.core

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UltimateDatabase(context: Context) :
    SQLiteOpenHelper(context, "ultimate_mushaf.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE reading_position(id INTEGER PRIMARY KEY CHECK(id=1), page INTEGER NOT NULL, verse TEXT, line INTEGER, updated_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE bookmarks(id INTEGER PRIMARY KEY AUTOINCREMENT, verse TEXT, page INTEGER NOT NULL, title TEXT NOT NULL, folder TEXT NOT NULL, tags TEXT NOT NULL, created_at INTEGER NOT NULL, permanent INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE annotations(id INTEGER PRIMARY KEY AUTOINCREMENT, page INTEGER NOT NULL, verse TEXT, type TEXT NOT NULL, payload TEXT NOT NULL, x REAL NOT NULL, y REAL NOT NULL, width REAL NOT NULL, height REAL NOT NULL, z_index INTEGER NOT NULL, created_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE hifz(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, start_verse TEXT NOT NULL, end_verse TEXT NOT NULL, repetitions INTEGER NOT NULL, current_verse TEXT, started_at INTEGER NOT NULL, completed INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE khatma(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, start_page INTEGER NOT NULL, end_page INTEGER NOT NULL, target_days INTEGER NOT NULL, current_page INTEGER NOT NULL, created_at INTEGER NOT NULL, completed INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE downloads(id TEXT PRIMARY KEY, url TEXT NOT NULL, destination TEXT NOT NULL, bytes INTEGER NOT NULL, total_bytes INTEGER NOT NULL, state TEXT NOT NULL, sha256 TEXT, pack_version TEXT NOT NULL, license TEXT NOT NULL)")
        db.execSQL("CREATE INDEX idx_bookmarks_page ON bookmarks(page)")
        db.execSQL("CREATE INDEX idx_annotations_page ON annotations(page)")
        db.execSQL("CREATE INDEX idx_downloads_state ON downloads(state)")
        db.insert("reading_position", null, ContentValues().apply {
            put("id", 1); put("page", 1); put("updated_at", System.currentTimeMillis())
        })
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun savePosition(position: ReadingPosition) {
        writableDatabase.update("reading_position", ContentValues().apply {
            put("page", position.page); put("verse", position.verseKey); put("line", position.line)
            put("updated_at", position.updatedAt)
        }, "id=1", null)
    }

    fun loadPosition(): ReadingPosition {
        readableDatabase.rawQuery("SELECT page,verse,line,updated_at FROM reading_position WHERE id=1", null).use { c ->
            if (!c.moveToFirst()) return ReadingPosition()
            return ReadingPosition(c.getInt(0), c.getStringOrNull(1), c.getIntOrNull(2), c.getLong(3))
        }
    }

    fun addBookmark(b: Bookmark): Long = writableDatabase.insert("bookmarks", null, ContentValues().apply {
        put("verse", b.verseKey); put("page", b.page); put("title", b.title)
        put("folder", b.folder); put("tags", b.tags.joinToString("|")); put("created_at", b.createdAt)
        put("permanent", if (b.permanent) 1 else 0)
    })

    fun bookmarks(page: Int? = null): List<Bookmark> {
        val where = page?.let { "page=?" }
        val args = page?.let { arrayOf(it.toString()) }
        readableDatabase.query("bookmarks", null, where, args, null, null, "created_at DESC").use { c ->
            val out = mutableListOf<Bookmark>()
            while (c.moveToNext()) out += Bookmark(c.getLong(0), c.getStringOrNull(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5).split("|").filter(String::isNotBlank), c.getLong(6), c.getInt(7) != 0)
            return out
        }
    }

    fun deleteBookmark(id: Long) { writableDatabase.delete("bookmarks", "id=?", arrayOf(id.toString())) }
    fun deleteAnnotation(id: Long) { writableDatabase.delete("annotations", "id=?", arrayOf(id.toString())) }

    fun addAnnotation(a: Annotation): Long = writableDatabase.insert("annotations", null, ContentValues().apply {
        put("page", a.page); put("verse", a.verseKey); put("type", a.type); put("payload", a.payload)
        put("x", a.x); put("y", a.y); put("width", a.width); put("height", a.height); put("z_index", a.zIndex); put("created_at", a.createdAt)
    })

    
    fun addHifz(session: HifzSession): Long = writableDatabase.insert("hifz", null, ContentValues().apply {
        put("name", session.name); put("start_verse", session.startVerse); put("end_verse", session.endVerse)
        put("repetitions", session.repetitions); put("current_verse", session.currentVerse)
        put("started_at", session.startedAt); put("completed", if (session.completed) 1 else 0)
    })

    fun addKhatma(plan: KhatmaPlan): Long = writableDatabase.insert("khatma", null, ContentValues().apply {
        put("name", plan.name); put("start_page", plan.startPage); put("end_page", plan.endPage)
        put("target_days", plan.targetDays); put("current_page", plan.currentPage)
        put("created_at", plan.createdAt); put("completed", if (plan.completed) 1 else 0)
    })

    fun upsertDownload(item: DownloadItem) {
        writableDatabase.insertWithOnConflict("downloads", null, ContentValues().apply {
            put("id", item.id); put("url", item.url); put("destination", item.destination)
            put("bytes", item.bytes); put("total_bytes", item.totalBytes); put("state", item.state)
            put("sha256", item.sha256); put("pack_version", item.packVersion); put("license", item.license)
        }, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun downloads(): List<DownloadItem> {
        readableDatabase.query("downloads", null, null, null, null, null, "id ASC").use { c ->
            val out = mutableListOf<DownloadItem>()
            while (c.moveToNext()) out += DownloadItem(
                id = c.getString(0), url = c.getString(1), destination = c.getString(2),
                bytes = c.getLong(3), totalBytes = c.getLong(4), state = c.getString(5),
                sha256 = c.getStringOrNull(6), packVersion = c.getString(7), license = c.getString(8)
            )
            return out
        }
    }

    fun annotations(page: Int): List<Annotation> {
        readableDatabase.query("annotations", null, "page=?", arrayOf(page.toString()), null, null, "z_index ASC,id ASC").use { c ->
            val out = mutableListOf<Annotation>()
            while (c.moveToNext()) out += Annotation(c.getLong(0), c.getInt(1), c.getStringOrNull(2), c.getString(3), c.getString(4), c.getFloat(5), c.getFloat(6), c.getFloat(7), c.getFloat(8), c.getInt(9), c.getLong(10))
            return out
        }
    }
}

private fun Cursor.getStringOrNull(index: Int): String? = if (isNull(index)) null else getString(index)
private fun Cursor.getIntOrNull(index: Int): Int? = if (isNull(index)) null else getInt(index)
