package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.WeightRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_records ORDER BY timestamp ASC")
    fun getAllWeightsAsc(): Flow<List<WeightRecord>>

    @Query("SELECT * FROM weight_records ORDER BY timestamp DESC")
    fun getAllWeightsDesc(): Flow<List<WeightRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: WeightRecord): Long

    @Delete
    suspend fun deleteWeight(weight: WeightRecord)

    @Query("SELECT * FROM weight_records ORDER BY timestamp DESC LIMIT 1")
    fun getLatestWeight(): Flow<WeightRecord?>
}
