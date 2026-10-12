package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wearable_sync")
data class BiometricWearableData(
    @PrimaryKey
    val dateKey: String, // YYYY-MM-DD
    val deviceName: String = "Pixel Watch (Biometric Sync)",
    val steps: Int = 8450,
    val activeCalories: Int = 420,
    val restingHeartRate: Int = 62,
    val sleepHours: Float = 7.5f,
    val lastSyncedMillis: Long = System.currentTimeMillis(),
    val isDeviceConnected: Boolean = true,
    val encryptedBiometricsPayload: String = ""
)
