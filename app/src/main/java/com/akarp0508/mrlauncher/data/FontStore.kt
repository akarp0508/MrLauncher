package com.akarp0508.mrlauncher.data

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import java.io.File

/** Keeps a user-imported font file in app storage. */
object FontStore {

    /** Copies the font at [uri] into app storage; returns its path, or null if it isn't a usable font. */
    fun import(context: Context, uri: Uri): String? {
        val dir = File(context.filesDir, "fonts").apply { mkdirs() }
        // Unique name per import so the theme notices the change and reloads the typeface.
        val file = File(dir, "custom-${System.currentTimeMillis()}")
        val copied = runCatching {
            context.contentResolver.openInputStream(uri)!!.use { input ->
                file.outputStream().use { input.copyTo(it) }
            }
        }.isSuccess
        if (!copied || load(file.path) == null) {
            file.delete()
            return null
        }
        return file.path
    }

    fun load(path: String): Typeface? =
        runCatching { Typeface.createFromFile(path) }.getOrNull()?.takeIf { it != Typeface.DEFAULT }

    fun delete(path: String) {
        File(path).delete()
    }
}
