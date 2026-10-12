package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack")
}

@Entity(tableName = "meals")
data class MealRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mealName: String,
    val mealType: String = MealType.LUNCH.name,
    val timestamp: Long = System.currentTimeMillis(),
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val isMacroBalanced: Boolean,
    val macroFeedback: String = "",
    val imageUri: String? = null,
    val encryptedNotes: String = "" // AES-256 encrypted local record notes
) {
    // Calculated macro caloric ratios
    val proteinCalories: Float get() = proteinGrams * 4f
    val carbsCalories: Float get() = carbsGrams * 4f
    val fatCalories: Float get() = fatGrams * 9f
    val totalMacroCalories: Float get() = (proteinCalories + carbsCalories + fatCalories).coerceAtLeast(1f)

    val proteinPct: Int get() = ((proteinCalories / totalMacroCalories) * 100).toInt()
    val carbsPct: Int get() = ((carbsCalories / totalMacroCalories) * 100).toInt()
    val fatPct: Int get() = (100 - proteinPct - carbsPct).coerceAtLeast(0)
}
