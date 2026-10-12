package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.BiometricWearableData
import kotlinx.coroutines.flow.Flow

@Dao
interface WearableDao {
    @Query("SELECT * FROM wearable_sync WHERE dateKey = :dateKey LIMIT 1")
    fun getWearableForDate(dateKey: String): Flow<BiometricWearableData?>

    @Query("SELECT * FROM wearable_sync ORDER BY lastSyncedMillis DESC LIMIT 1")
    fun getLatestWearableSync(): Flow<BiometricWearableData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWearable(data: BiometricWearableData)
}
