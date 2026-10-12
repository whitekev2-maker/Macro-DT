package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.BiometricWearableData
import com.example.model.MealRecord
import com.example.model.WeightRecord

@Database(
    entities = [
        MealRecord::class,
        WeightRecord::class,
        BiometricWearableData::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DietDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun weightDao(): WeightDao
    abstract fun wearableDao(): WearableDao

    companion object {
        @Volatile
        private var INSTANCE: DietDatabase? = null

        fun getDatabase(context: Context): DietDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DietDatabase::class.java,
                    "diet_tracker_encrypted_db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
