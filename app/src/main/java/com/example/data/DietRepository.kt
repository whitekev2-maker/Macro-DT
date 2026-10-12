package com.example.data

import android.content.Context
import com.example.data.local.DietDatabase
import com.example.model.BiometricWearableData
import com.example.model.MealRecord
import com.example.model.UserMacroGoals
import com.example.model.WeightRecord
import com.example.security.EncryptionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DietRepository(private val context: Context) {
    private val database = DietDatabase.getDatabase(context)
    private val mealDao = database.mealDao()
    private val weightDao = database.weightDao()
    private val wearableDao = database.wearableDao()

    private val prefs = context.getSharedPreferences("diet_user_goals", Context.MODE_PRIVATE)

    private val _userGoals = MutableStateFlow(loadUserGoals())
    val userGoals = _userGoals.asStateFlow()

    val allMeals: Flow<List<MealRecord>> = mealDao.getAllMeals()
    val allWeightsAsc: Flow<List<WeightRecord>> = weightDao.getAllWeightsAsc()
    val allWeightsDesc: Flow<List<WeightRecord>> = weightDao.getAllWeightsDesc()
    val latestWeight: Flow<WeightRecord?> = weightDao.getLatestWeight()
    val latestWearableSync: Flow<BiometricWearableData?> = wearableDao.getLatestWearableSync()

    private fun loadUserGoals(): UserMacroGoals {
        return UserMacroGoals(
            baseCalorieTarget = prefs.getInt("base_calories", 2000),
            targetProteinGrams = prefs.getFloat("target_protein", 135f),
            targetCarbsGrams = prefs.getFloat("target_carbs", 210f),
            targetFatGrams = prefs.getFloat("target_fat", 60f),
            currentWeightKg = prefs.getFloat("current_weight", 72.0f),
            targetWeightKg = prefs.getFloat("target_weight", 68.0f),
            useLbs = prefs.getBoolean("use_lbs", false),
            darkModeSetting = prefs.getString("dark_mode", "SYSTEM") ?: "SYSTEM",
            isEncryptionEnabled = prefs.getBoolean("encryption_enabled", true)
        )
    }

    suspend fun saveUserGoals(goals: UserMacroGoals) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putInt("base_calories", goals.baseCalorieTarget)
            .putFloat("target_protein", goals.targetProteinGrams)
            .putFloat("target_carbs", goals.targetCarbsGrams)
            .putFloat("target_fat", goals.targetFatGrams)
            .putFloat("current_weight", goals.currentWeightKg)
            .putFloat("target_weight", goals.targetWeightKg)
            .putBoolean("use_lbs", goals.useLbs)
            .putString("dark_mode", goals.darkModeSetting)
            .putBoolean("encryption_enabled", goals.isEncryptionEnabled)
            .apply()
        _userGoals.value = goals
    }

    suspend fun addMeal(
        mealName: String,
        mealType: String,
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float,
        isMacroBalanced: Boolean,
        macroFeedback: String,
        imageUri: String?,
        plainNote: String
    ): Long = withContext(Dispatchers.IO) {
        val encryptedNote = if (plainNote.isNotBlank()) {
            EncryptionManager.encrypt(context, plainNote)
        } else ""

        val meal = MealRecord(
            mealName = mealName,
            mealType = mealType,
            timestamp = System.currentTimeMillis(),
            calories = calories,
            proteinGrams = protein,
            carbsGrams = carbs,
            fatGrams = fat,
            isMacroBalanced = isMacroBalanced,
            macroFeedback = macroFeedback,
            imageUri = imageUri,
            encryptedNotes = encryptedNote
        )
        mealDao.insertMeal(meal)
    }

    suspend fun updateMeal(meal: MealRecord) = withContext(Dispatchers.IO) {
        mealDao.updateMeal(meal)
    }

    suspend fun deleteMeal(meal: MealRecord) = withContext(Dispatchers.IO) {
        mealDao.deleteMeal(meal)
    }

    suspend fun addWeightRecord(weightKg: Float, plainNote: String = "") = withContext(Dispatchers.IO) {
        val encryptedNote = if (plainNote.isNotBlank()) {
            EncryptionManager.encrypt(context, plainNote)
        } else ""

        val record = WeightRecord(
            weightKg = weightKg,
            timestamp = System.currentTimeMillis(),
            encryptedNote = encryptedNote
        )
        val id = weightDao.insertWeight(record)
        // update current weight
        val currentGoals = _userGoals.value.copy(currentWeightKg = weightKg)
        saveUserGoals(currentGoals)
        id
    }

    suspend fun deleteWeightRecord(weight: WeightRecord) = withContext(Dispatchers.IO) {
        weightDao.deleteWeight(weight)
    }

    suspend fun syncWearableData(
        deviceName: String = "Pixel Watch 3 (Biometric Sync)",
        steps: Int = (7500..11500).random(),
        activeCalories: Int = (380..580).random(),
        heartRate: Int = (58..72).random(),
        sleepHours: Float = ((65..85).random() / 10f)
    ) = withContext(Dispatchers.IO) {
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sensitiveBiometrics = "HR_AVG:$heartRate;SLEEP:$sleepHours;CAL_BURN:$activeCalories"
        val encryptedBiometrics = EncryptionManager.encrypt(context, sensitiveBiometrics)

        val wearableData = BiometricWearableData(
            dateKey = dateKey,
            deviceName = deviceName,
            steps = steps,
            activeCalories = activeCalories,
            restingHeartRate = heartRate,
            sleepHours = sleepHours,
            lastSyncedMillis = System.currentTimeMillis(),
            isDeviceConnected = true,
            encryptedBiometricsPayload = encryptedBiometrics
        )
        wearableDao.insertOrUpdateWearable(wearableData)
    }

    fun decryptNote(encryptedText: String): String {
        return EncryptionManager.decrypt(context, encryptedText)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        if (mealDao.getMealCount() == 0) {
            val now = System.currentTimeMillis()
            val dayMillis = 86400000L

            // Seed sample past 4 days of realistic logs
            val sampleMeals = listOf(
                MealRecord(
                    mealName = "Greek Yogurt Parfait & Berries",
                    mealType = "BREAKFAST",
                    timestamp = now - (dayMillis * 3) + 28800000L,
                    calories = 360,
                    proteinGrams = 28f,
                    carbsGrams = 38f,
                    fatGrams = 9f,
                    isMacroBalanced = true,
                    macroFeedback = "Optimal breakfast protein density with antioxidant-rich berries."
                ),
                MealRecord(
                    mealName = "Grilled Salmon Quinoa Bowl",
                    mealType = "LUNCH",
                    timestamp = now - (dayMillis * 3) + 46800000L,
                    calories = 580,
                    proteinGrams = 42f,
                    carbsGrams = 46f,
                    fatGrams = 18f,
                    isMacroBalanced = true,
                    macroFeedback = "Excellent omega-3 healthy fats and complex carbs."
                ),
                MealRecord(
                    mealName = "Oatmeal with Whey & Banana",
                    mealType = "BREAKFAST",
                    timestamp = now - (dayMillis * 2) + 28800000L,
                    calories = 420,
                    proteinGrams = 32f,
                    carbsGrams = 54f,
                    fatGrams = 8f,
                    isMacroBalanced = true,
                    macroFeedback = "Clean sustained energy carbs and fast-absorbing protein."
                ),
                MealRecord(
                    mealName = "Double Bacon Cheeseburger & Fries",
                    mealType = "DINNER",
                    timestamp = now - (dayMillis * 2) + 68400000L,
                    calories = 980,
                    proteinGrams = 34f,
                    carbsGrams = 82f,
                    fatGrams = 56f,
                    isMacroBalanced = false,
                    macroFeedback = "High saturated fats (51% calories) and refined carbs out of target ratio."
                ),
                MealRecord(
                    mealName = "Scrambled Eggs & Avocado Toast",
                    mealType = "BREAKFAST",
                    timestamp = now - dayMillis + 28800000L,
                    calories = 440,
                    proteinGrams = 24f,
                    carbsGrams = 30f,
                    fatGrams = 24f,
                    isMacroBalanced = true,
                    macroFeedback = "Balanced monounsaturated fats with quality egg protein."
                ),
                MealRecord(
                    mealName = "Herb Chicken Breast & Sweet Potato",
                    mealType = "DINNER",
                    timestamp = now - dayMillis + 68400000L,
                    calories = 510,
                    proteinGrams = 48f,
                    carbsGrams = 45f,
                    fatGrams = 12f,
                    isMacroBalanced = true,
                    macroFeedback = "High lean protein (38%) and nutrient-rich slow-digesting carbs."
                ),
                MealRecord(
                    mealName = "Loaded Pepperoni Pizza Slices",
                    mealType = "LUNCH",
                    timestamp = now - (dayMillis / 2),
                    calories = 780,
                    proteinGrams = 22f,
                    carbsGrams = 78f,
                    fatGrams = 42f,
                    isMacroBalanced = false,
                    macroFeedback = "Fat and refined carbohydrates dominate; lacks target lean protein density."
                )
            )

            sampleMeals.forEach { mealDao.insertMeal(it) }

            // Seed weight records for trend
            val sampleWeights = listOf(
                WeightRecord(weightKg = 73.8f, timestamp = now - (dayMillis * 18)),
                WeightRecord(weightKg = 73.4f, timestamp = now - (dayMillis * 14)),
                WeightRecord(weightKg = 73.0f, timestamp = now - (dayMillis * 10)),
                WeightRecord(weightKg = 72.6f, timestamp = now - (dayMillis * 7)),
                WeightRecord(weightKg = 72.3f, timestamp = now - (dayMillis * 4)),
                WeightRecord(weightKg = 72.0f, timestamp = now - (dayMillis * 1))
            )
            sampleWeights.forEach { weightDao.insertWeight(it) }

            // Seed initial wearable sync
            syncWearableData(
                deviceName = "Pixel Watch 3 (Biometric Sync)",
                steps = 9420,
                activeCalories = 460,
                heartRate = 64,
                sleepHours = 7.6f
            )
        }
    }
}
