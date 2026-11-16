package com.gruop11.yepapp.yep.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gruop11.yepapp.yep.model.AppLanguage
import com.gruop11.yepapp.yep.model.YepStateSnapshot
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.yepDataStore: DataStore<Preferences> by preferencesDataStore(name = "yep_prefs")

@Entity(tableName = "yep_snapshot")
data class YepSnapshotEntity(
    @PrimaryKey val id: Int = 1,
    val payloadJson: String,
    val updatedAt: Long,
)

@Dao
interface YepSnapshotDao {
    @Query("SELECT * FROM yep_snapshot WHERE id = 1 LIMIT 1")
    suspend fun getSnapshot(): YepSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSnapshot(entity: YepSnapshotEntity)
}

@Database(entities = [YepSnapshotEntity::class], version = 1, exportSchema = false)
abstract class YepRoomDatabase : RoomDatabase() {
    abstract fun snapshotDao(): YepSnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: YepRoomDatabase? = null

        fun getInstance(context: Context): YepRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    YepRoomDatabase::class.java,
                    "yep_platform_db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}

class YepLocalRepository(private val context: Context) {
    private val dao = YepRoomDatabase.getInstance(context).snapshotDao()
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(YepStateSnapshot::class.java)

    private object PrefKeys {
        val ACTIVE_USER_ID = stringPreferencesKey("active_user_id")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val OFFLINE_DATA_SAVER = booleanPreferencesKey("offline_data_saver")
        val SYNC_CLIENT_ID = stringPreferencesKey("sync_client_id")
    }

    val activeUserIdFlow: Flow<String?> = context.yepDataStore.data.map { prefs ->
        prefs[PrefKeys.ACTIVE_USER_ID]?.takeIf { it.isNotBlank() }
    }

    val languageFlow: Flow<AppLanguage> = context.yepDataStore.data.map { prefs ->
        val code = prefs[PrefKeys.SELECTED_LANGUAGE] ?: AppLanguage.EN.name
        AppLanguage.entries.firstOrNull { it.name == code } ?: AppLanguage.EN
    }

    val offlineDataSaverFlow: Flow<Boolean> = context.yepDataStore.data.map { prefs ->
        prefs[PrefKeys.OFFLINE_DATA_SAVER] ?: false
    }

    suspend fun loadOrSeedSnapshot(): YepStateSnapshot {
        val existing = dao.getSnapshot()
        if (existing != null) {
            runCatching { adapter.fromJson(existing.payloadJson) }.getOrNull()?.let { return it }
        }
        val seeded = SeedData.createInitialSnapshot()
        saveSnapshot(seeded)
        return seeded
    }

    suspend fun saveSnapshot(snapshot: YepStateSnapshot) {
        val json = adapter.toJson(snapshot)
        dao.saveSnapshot(
            YepSnapshotEntity(
                id = 1,
                payloadJson = json,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun setActiveUserId(userId: String?) {
        context.yepDataStore.edit { prefs ->
            if (userId == null) {
                prefs.remove(PrefKeys.ACTIVE_USER_ID)
            } else {
                prefs[PrefKeys.ACTIVE_USER_ID] = userId
            }
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.yepDataStore.edit { prefs ->
            prefs[PrefKeys.SELECTED_LANGUAGE] = language.name
        }
    }

    suspend fun setOfflineDataSaver(enabled: Boolean) {
        context.yepDataStore.edit { prefs ->
            prefs[PrefKeys.OFFLINE_DATA_SAVER] = enabled
        }
    }

    suspend fun getOrCreateClientId(): String {
        val existing = context.yepDataStore.data.first()[PrefKeys.SYNC_CLIENT_ID]
        if (!existing.isNullOrBlank()) return existing
        val created = "android_${java.util.UUID.randomUUID().toString().take(12)}"
        context.yepDataStore.edit { prefs ->
            prefs[PrefKeys.SYNC_CLIENT_ID] = created
        }
        return created
    }

    suspend fun readInitialPrefs(): Triple<String?, AppLanguage, Boolean> {
        val prefs = context.yepDataStore.data.first()
        val uid = prefs[PrefKeys.ACTIVE_USER_ID]?.takeIf { it.isNotBlank() }
        val lang = AppLanguage.entries.firstOrNull {
            it.name == (prefs[PrefKeys.SELECTED_LANGUAGE] ?: AppLanguage.EN.name)
        } ?: AppLanguage.EN
        val offline = prefs[PrefKeys.OFFLINE_DATA_SAVER] ?: false
        return Triple(uid, lang, offline)
    }
}
