package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_records")
data class WeightRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weightKg: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val encryptedNote: String = "",
    val bodyFatPct: Float? = null
) {
    fun getDisplayWeight(isLbs: Boolean): Float {
        return if (isLbs) weightKg * 2.20462f else weightKg
    }
}
