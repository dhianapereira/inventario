package io.github.dhianapereira.inventario.data.backup

import android.content.Context
import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import androidx.core.net.toUri
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.dhianapereira.inventario.data.category.CategoryEntity
import io.github.dhianapereira.inventario.data.database.InventarioDatabase
import io.github.dhianapereira.inventario.data.item.ItemEntity
import io.github.dhianapereira.inventario.data.itemclosure.ItemClosureEntity
import io.github.dhianapereira.inventario.data.itemupdate.ItemUpdateEntity
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class JsonBackupRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: InventarioDatabase,
) : BackupRepository {
    override suspend fun exportTo(uri: String): BackupSummary = withContext(Dispatchers.IO) {
        val payload = database.withTransaction {
            BackupPayload(
                database.categoryDao().getAllForBackup(),
                database.itemDao().getAllForBackup(),
                database.itemUpdateDao().getAllForBackup(),
                database.itemClosureDao().getAllForBackup(),
            )
        }
        try {
            val output = context.contentResolver.openOutputStream(uri.toUri(), "wt")
                ?: throw BackupException.CannotOpenFile()
            output.use { stream ->
                JsonWriter(OutputStreamWriter(BufferedOutputStream(stream), Charsets.UTF_8)).use { writer ->
                    writer.setIndent("  ")
                    writeBackup(writer, payload)
                }
            }
            payload.summary()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: BackupException) {
            throw exception
        } catch (exception: IOException) {
            throw BackupException.CannotOpenFile(exception)
        } catch (exception: SecurityException) {
            throw BackupException.CannotOpenFile(exception)
        }
    }

    override suspend fun restoreFrom(uri: String): BackupSummary = withContext(Dispatchers.IO) {
        val payload = try {
            val input = context.contentResolver.openInputStream(uri.toUri())
                ?: throw BackupException.CannotOpenFile()
            input.use { stream ->
                val reader = JsonReader(InputStreamReader(BufferedInputStream(stream), Charsets.UTF_8))
                try {
                    readBackup(reader)
                } finally {
                    reader.close()
                }
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: BackupException) {
            throw exception
        } catch (exception: SecurityException) {
            throw BackupException.CannotOpenFile(exception)
        } catch (exception: Exception) {
            throw BackupException.InvalidFile(exception)
        }
        validate(payload)
        coroutineContext.ensureActive()
        database.withTransaction {
            database.itemClosureDao().deleteAll()
            database.itemUpdateDao().deleteAll()
            database.itemDao().deleteAll()
            database.categoryDao().deleteAll()
            database.categoryDao().insertAll(payload.categories)
            database.itemDao().insertAll(payload.items)
            database.itemUpdateDao().insertAll(payload.updates)
            database.itemClosureDao().upsertAll(payload.closures)
        }
        payload.summary()
    }

    private suspend fun writeBackup(writer: JsonWriter, payload: BackupPayload) {
        writer.beginObject()
        writer.name("format").value(FORMAT)
        writer.name("version").value(VERSION.toLong())
        writer.name("exportedAt").value(Instant.now().toString())
        writer.name("categories").beginArray()
        payload.categories.forEach { value ->
            coroutineContext.ensureActive()
            writer.beginObject()
            writer.name("id").value(value.id)
            writer.name("name").value(value.name)
            writer.name("createdAt").value(value.createdAt)
            writer.name("updatedAt").value(value.updatedAt)
            writer.endObject()
        }
        writer.endArray()
        writer.name("items").beginArray()
        payload.items.forEach { value ->
            coroutineContext.ensureActive()
            writer.beginObject()
            writer.name("id").value(value.id)
            writer.name("name").value(value.name)
            writer.name("categoryId").value(value.categoryId)
            writer.name("arrivalDateEpochDay").value(value.arrivalDateEpochDay)
            writer.name("purchasePriceInCents").value(value.purchasePriceInCents)
            writer.name("currencyCode").value(value.currencyCode)
            writer.name("description").value(value.description)
            writer.name("createdAt").value(value.createdAt)
            writer.name("updatedAt").value(value.updatedAt)
            writer.endObject()
        }
        writer.endArray()
        writer.name("updates").beginArray()
        payload.updates.forEach { value ->
            coroutineContext.ensureActive()
            writer.beginObject()
            writer.name("id").value(value.id)
            writer.name("itemId").value(value.itemId)
            writer.name("dateEpochDay").value(value.dateEpochDay)
            writer.name("description").value(value.description)
            writer.name("costInCents").value(value.costInCents)
            writer.name("createdAt").value(value.createdAtEpochMillis)
            writer.name("updatedAt").value(value.updatedAtEpochMillis)
            writer.endObject()
        }
        writer.endArray()
        writer.name("closures").beginArray()
        payload.closures.forEach { value ->
            coroutineContext.ensureActive()
            writer.beginObject()
            writer.name("itemId").value(value.itemId)
            writer.name("dateEpochDay").value(value.dateEpochDay)
            writer.name("reason").value(value.reason)
            writer.name("note").value(value.note)
            writer.name("recoveredValueInCents").value(value.recoveredValueInCents)
            writer.name("createdAt").value(value.createdAt)
            writer.name("updatedAt").value(value.updatedAt)
            writer.endObject()
        }
        writer.endArray()
        writer.endObject()
    }

    private suspend fun readBackup(reader: JsonReader): BackupPayload {
        var format: String? = null
        var version: Int? = null
        var categories: List<CategoryEntity>? = null
        var items: List<ItemEntity>? = null
        var updates: List<ItemUpdateEntity>? = null
        var closures: List<ItemClosureEntity>? = null
        reader.beginObject()
        while (reader.hasNext()) {
            coroutineContext.ensureActive()
            when (reader.nextName()) {
                "format" -> format = reader.nextString()
                "version" -> version = reader.nextInt()
                "categories" -> categories = readArray(reader, ::readCategory)
                "items" -> items = readArray(reader, ::readItem)
                "updates" -> updates = readArray(reader, ::readUpdate)
                "closures" -> closures = readArray(reader, ::readClosure)
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        if (reader.peek() != JsonToken.END_DOCUMENT || format != FORMAT) throw BackupException.InvalidFile()
        if (version != VERSION) throw BackupException.UnsupportedVersion()
        return BackupPayload(
            categories ?: throw BackupException.InvalidFile(),
            items ?: throw BackupException.InvalidFile(),
            updates ?: throw BackupException.InvalidFile(),
            closures ?: throw BackupException.InvalidFile(),
        )
    }

    private suspend fun <T> readArray(reader: JsonReader, read: (JsonReader) -> T): List<T> {
        val values = mutableListOf<T>()
        reader.beginArray()
        while (reader.hasNext()) {
            coroutineContext.ensureActive()
            if (values.size >= MAX_RECORDS_PER_COLLECTION) throw BackupException.TooManyRecords()
            values += read(reader)
        }
        reader.endArray()
        return values
    }

    private fun readCategory(reader: JsonReader): CategoryEntity {
        var id: String? = null; var name: String? = null; var createdAt: Long? = null; var updatedAt: Long? = null
        reader.readObject { field -> when (field) {
            "id" -> id = reader.nextString(); "name" -> name = reader.nextString()
            "createdAt" -> createdAt = reader.nextLong(); "updatedAt" -> updatedAt = reader.nextLong()
            else -> reader.skipValue()
        } }
        return CategoryEntity(id.required(), name.required(), createdAt.required(), updatedAt.required())
    }

    private fun readItem(reader: JsonReader): ItemEntity {
        var id: String? = null; var name: String? = null; var categoryId: String? = null
        var date: Long? = null; var price: Long? = null; var currency: String? = null; var description: String? = null
        var createdAt: Long? = null; var updatedAt: Long? = null
        reader.readObject { field -> when (field) {
            "id" -> id = reader.nextString(); "name" -> name = reader.nextString(); "categoryId" -> categoryId = reader.nextString()
            "arrivalDateEpochDay" -> date = reader.nextLong(); "purchasePriceInCents" -> price = reader.nextLong()
            "currencyCode" -> currency = reader.nextString(); "description" -> description = reader.nextNullableString()
            "createdAt" -> createdAt = reader.nextLong(); "updatedAt" -> updatedAt = reader.nextLong()
            else -> reader.skipValue()
        } }
        return ItemEntity(id.required(), name.required(), categoryId.required(), date.required(), price.required(), currency.required(), description, createdAt.required(), updatedAt.required())
    }

    private fun readUpdate(reader: JsonReader): ItemUpdateEntity {
        var id: String? = null; var itemId: String? = null; var date: Long? = null; var description: String? = null
        var cost: Long? = null; var costPresent = false; var createdAt: Long? = null; var updatedAt: Long? = null
        reader.readObject { field -> when (field) {
            "id" -> id = reader.nextString(); "itemId" -> itemId = reader.nextString(); "dateEpochDay" -> date = reader.nextLong()
            "description" -> description = reader.nextString(); "costInCents" -> { costPresent = true; cost = reader.nextNullableLong() }
            "createdAt" -> createdAt = reader.nextLong(); "updatedAt" -> updatedAt = reader.nextLong()
            else -> reader.skipValue()
        } }
        if (!costPresent) throw BackupException.InvalidFile()
        return ItemUpdateEntity(id.required(), itemId.required(), date.required(), description.required(), cost, createdAt.required(), updatedAt.required())
    }

    private fun readClosure(reader: JsonReader): ItemClosureEntity {
        var itemId: String? = null; var date: Long? = null; var reason: String? = null; var note: String? = null
        var recovered: Long? = null; var recoveredPresent = false; var createdAt: Long? = null; var updatedAt: Long? = null
        reader.readObject { field -> when (field) {
            "itemId" -> itemId = reader.nextString(); "dateEpochDay" -> date = reader.nextLong(); "reason" -> reason = reader.nextString()
            "note" -> note = reader.nextNullableString(); "recoveredValueInCents" -> { recoveredPresent = true; recovered = reader.nextNullableLong() }
            "createdAt" -> createdAt = reader.nextLong(); "updatedAt" -> updatedAt = reader.nextLong()
            else -> reader.skipValue()
        } }
        if (!recoveredPresent) throw BackupException.InvalidFile()
        return ItemClosureEntity(itemId.required(), date.required(), reason.required(), note, recovered, createdAt.required(), updatedAt.required())
    }

    private fun validate(payload: BackupPayload) {
        BackupValidator.validate(payload.categories, payload.items, payload.updates, payload.closures)
    }
    private fun <T : Any> T?.required(): T = this ?: throw BackupException.InvalidFile()
    private fun JsonReader.readObject(readField: (String) -> Unit) { beginObject(); while (hasNext()) readField(nextName()); endObject() }
    private fun JsonReader.nextNullableString(): String? = if (peek() == JsonToken.NULL) { nextNull(); null } else nextString()
    private fun JsonReader.nextNullableLong(): Long? = if (peek() == JsonToken.NULL) { nextNull(); null } else nextLong()

    private data class BackupPayload(
        val categories: List<CategoryEntity>, val items: List<ItemEntity>,
        val updates: List<ItemUpdateEntity>, val closures: List<ItemClosureEntity>,
    ) {
        fun summary() = BackupSummary(categories.size, items.size, updates.size, closures.size)
    }

    private companion object {
        const val FORMAT = "inventario-backup"
        const val VERSION = 1
        const val MAX_RECORDS_PER_COLLECTION = 1_000_000
    }
}
