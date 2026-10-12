package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.MealRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealRecord>>

    @Query("SELECT * FROM meals WHERE timestamp BETWEEN :startMillis AND :endMillis ORDER BY timestamp ASC")
    fun getMealsBetween(startMillis: Long, endMillis: Long): Flow<List<MealRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealRecord): Long

    @Update
    suspend fun updateMeal(meal: MealRecord)

    @Delete
    suspend fun deleteMeal(meal: MealRecord)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMealById(id: Long)

    @Query("SELECT COUNT(*) FROM meals")
    suspend fun getMealCount(): Int
}
