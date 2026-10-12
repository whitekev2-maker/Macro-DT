package com.example.model

data class UserMacroGoals(
    val baseCalorieTarget: Int = 2000,
    val targetProteinGrams: Float = 140f,
    val targetCarbsGrams: Float = 200f,
    val targetFatGrams: Float = 60f,
    val currentWeightKg: Float = 72.5f,
    val targetWeightKg: Float = 68.0f,
    val useLbs: Boolean = false,
    val darkModeSetting: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val isEncryptionEnabled: Boolean = true,
    val isWearableAutoSyncEnabled: Boolean = true,
    val connectedWearableName: String = "Health Connect / Wearable Active"
) {
    // Dynamic recommendation based on active calories burned from wearables
    fun calculateRecommendedCalories(activeCaloriesBurned: Int): Int {
        return baseCalorieTarget + activeCaloriesBurned
    }
}
