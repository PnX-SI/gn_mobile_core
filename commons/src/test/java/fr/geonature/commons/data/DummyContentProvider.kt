package fr.geonature.commons.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

class DummyContentProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        return true
    }

    override fun getType(uri: Uri): String {
        return "$VND_TYPE_ITEM_PREFIX/fr.geonature.sync.provider.DummyTable"
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri {
        return uri
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw NotImplementedError("'update' operation not implemented")
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw NotImplementedError("'delete' operation not implemented")
    }

    companion object {
        const val VND_TYPE_ITEM_PREFIX = "vnd.android.cursor.item"
    }
}