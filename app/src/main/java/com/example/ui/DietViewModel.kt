package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DietRepository
import com.example.model.BiometricWearableData
import com.example.model.MealRecord
import com.example.model.MealType
import com.example.model.UserMacroGoals
import com.example.model.WeightRecord
import com.example.vision.FoodRecognitionService
import com.example.vision.MealAnalysisResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DailyProgressSummary(
    val totalCalories: Int = 0,
    val targetCalories: Int = 2000,
    val totalProtein: Float = 0f,
    val targetProtein: Float = 140f,
    val totalCarbs: Float = 0f,
    val targetCarbs: Float = 210f,
    val totalFat: Float = 0f,
    val targetFat: Float = 60f,
    val mealsCount: Int = 0,
    val balancedMealsCount: Int = 0,
    val streakDays: Int = 7
)

data class OverlayDashboardConfig(
    val timeWindowDays: Int = 14, // 7, 14, 30
    val showWeight: Boolean = true,
    val showCalories: Boolean = true,
    val showProtein: Boolean = true,
    val showCarbs: Boolean = false,
    val showFat: Boolean = false
)

enum class AppTab(val title: String) {
    SCANNER("Scan Meal"),
    DAILY_LOG("Daily Log"),
    TRENDS("Overlay Trends"),
    WEARABLE("Wearable"),
    SOCIAL("Social & Privacy")
}

class DietViewModel(application: Application) : AndroidViewModel(application) {
    val repository = DietRepository(application.applicationContext)

    val allMeals = repository.allMeals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWeightsAsc = repository.allWeightsAsc
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestWeight = repository.latestWeight
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestWearable = repository.latestWearableSync
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userGoals = repository.userGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserMacroGoals())

    // Active Tab
    private val _currentTab = MutableStateFlow(AppTab.SCANNER)
    val currentTab = _currentTab.asStateFlow()

    // Scanner / Camera State
    private val _currentBitmap = MutableStateFlow<Bitmap?>(null)
    val currentBitmap = _currentBitmap.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _analysisResult = MutableStateFlow<MealAnalysisResult?>(null)
    val analysisResult = _analysisResult.asStateFlow()

    private val _scanMealType = MutableStateFlow(MealType.LUNCH)
    val scanMealType = _scanMealType.asStateFlow()

    private val _scanNotes = MutableStateFlow("")
    val scanNotes = _scanNotes.asStateFlow()

    private val _isOfflineForced = MutableStateFlow(false)
    val isOfflineForced = _isOfflineForced.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    // Dashboard Overlay Config
    private val _overlayConfig = MutableStateFlow(OverlayDashboardConfig())
    val overlayConfig = _overlayConfig.asStateFlow()

    // Food Search & Manual Food Logging State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFoodCategory = MutableStateFlow("All")
    val selectedFoodCategory = _selectedFoodCategory.asStateFlow()

    private val _isSearchDialogOpen = MutableStateFlow(false)
    val isSearchDialogOpen = _isSearchDialogOpen.asStateFlow()

    // Combine today's meals & totals
    val todaySummary: StateFlow<DailyProgressSummary> = combine(
        allMeals,
        userGoals,
        latestWearable
    ) { meals, goals, wearable ->
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis

        val todayMeals = meals.filter { it.timestamp >= startOfToday }
        val totCals = todayMeals.sumOf { it.calories }
        val totProt = todayMeals.sumOf { it.proteinGrams.toDouble() }.toFloat()
        val totCarb = todayMeals.sumOf { it.carbsGrams.toDouble() }.toFloat()
        val totFat = todayMeals.sumOf { it.fatGrams.toDouble() }.toFloat()
        val balancedCount = todayMeals.count { it.isMacroBalanced }

        // Dynamic target adjusted by wearable active calories
        val dynamicCalTarget = goals.calculateRecommendedCalories(wearable?.activeCalories ?: 0)

        DailyProgressSummary(
            totalCalories = totCals,
            targetCalories = dynamicCalTarget,
            totalProtein = totProt,
            targetProtein = goals.targetProteinGrams,
            totalCarbs = totCarb,
            targetCarbs = goals.targetCarbsGrams,
            totalFat = totFat,
            targetFat = goals.targetFatGrams,
            mealsCount = todayMeals.size,
            balancedMealsCount = balancedCount,
            streakDays = 7
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyProgressSummary())

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setCapturedBitmap(bitmap: Bitmap?) {
        _currentBitmap.value = bitmap
        if (bitmap != null) {
            analyzeCapturedMeal(bitmap)
        }
    }

    fun setOfflineMode(forced: Boolean) {
        _isOfflineForced.value = forced
        showToast(if (forced) "Offline Mode Enabled: Using Local Heuristics" else "Online Mode Enabled")
    }

    fun analyzeCapturedMeal(bitmap: Bitmap?) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val result = FoodRecognitionService.analyzeMeal(
                    bitmap = bitmap,
                    userGoals = userGoals.value,
                    forceOffline = _isOfflineForced.value
                )
                _analysisResult.value = result
            } catch (e: Exception) {
                val fallback = FoodRecognitionService.getSampleMeals().first()
                _analysisResult.value = fallback
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun loadSampleMeal(index: Int) {
        val samples = FoodRecognitionService.getSampleMeals()
        val sample = samples.getOrElse(index) { samples.first() }
        _analysisResult.value = sample
    }

    fun resubmitWithCorrectedFood(correctedName: String) {
        if (correctedName.isBlank()) return
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val result = FoodRecognitionService.resubmitWithWebSearch(
                    correctedItem = correctedName.trim(),
                    userGoals = userGoals.value,
                    bitmap = _currentBitmap.value,
                    forceOffline = _isOfflineForced.value
                )
                _analysisResult.value = result
                showToast("Web Search Grounded: Updated macros for '${result.mealName}'")
            } catch (e: Exception) {
                showToast("Could not complete web search: ${e.message}")
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun setScanMealType(type: MealType) {
        _scanMealType.value = type
    }

    fun setScanNotes(notes: String) {
        _scanNotes.value = notes
    }

    fun updateAnalysisMacros(protein: Float, carbs: Float, fat: Float, calories: Int) {
        val current = _analysisResult.value ?: return
        val isBalanced = FoodRecognitionService.checkMacroBalance(calories, protein, carbs, fat)
        _analysisResult.value = current.copy(
            proteinGrams = protein,
            carbsGrams = carbs,
            fatGrams = fat,
            calories = calories,
            isMacroBalanced = isBalanced,
            macroFeedback = if (isBalanced) "Adjusted: Right macros (Balanced protein & energy ratio)." else "Adjusted: Out of target macro balance."
        )
    }

    fun logCurrentMeal() {
        val result = _analysisResult.value ?: return
        viewModelScope.launch {
            repository.addMeal(
                mealName = result.mealName,
                mealType = _scanMealType.value.name,
                calories = result.calories,
                protein = result.proteinGrams,
                carbs = result.carbsGrams,
                fat = result.fatGrams,
                isMacroBalanced = result.isMacroBalanced,
                macroFeedback = result.macroFeedback,
                imageUri = null,
                plainNote = _scanNotes.value
            )
            showToast("Logged ${result.mealName} (${result.calories} kcal) with Encrypted Privacy!")
            _scanNotes.value = ""
            // Switch to Daily Log so user immediately sees their log
            _currentTab.value = AppTab.DAILY_LOG
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFoodCategory(category: String) {
        _selectedFoodCategory.value = category
    }

    fun setSearchDialogOpen(isOpen: Boolean) {
        _isSearchDialogOpen.value = isOpen
    }

    fun toggleWeightUnit() {
        val current = userGoals.value
        val newUseLbs = !current.useLbs
        updateGoals(
            calories = current.baseCalorieTarget,
            protein = current.targetProteinGrams,
            carbs = current.targetCarbsGrams,
            fat = current.targetFatGrams,
            targetWeight = current.targetWeightKg,
            useLbs = newUseLbs
        )
        showToast(if (newUseLbs) "Units changed to pounds (lb)" else "Units changed to kilograms (kg)")
    }

    fun setWeightUnit(useLbs: Boolean) {
        val current = userGoals.value
        if (current.useLbs == useLbs) return
        updateGoals(
            calories = current.baseCalorieTarget,
            protein = current.targetProteinGrams,
            carbs = current.targetCarbsGrams,
            fat = current.targetFatGrams,
            targetWeight = current.targetWeightKg,
            useLbs = useLbs
        )
        showToast(if (useLbs) "Units set to pounds (lb)" else "Units set to kilograms (kg)")
    }

    fun selectFoodFromSearch(
        item: com.example.model.FoodDatabaseItem,
        portions: Float,
        mealType: MealType = _scanMealType.value,
        note: String = ""
    ) {
        val macros = item.calculateMacros(portions)
        val isWeb = item.category.equals("Internet Search", ignoreCase = true)
        val result = MealAnalysisResult(
            mealName = "${item.name} (${macros.portionDescription})",
            calories = macros.calories,
            proteinGrams = macros.proteinGrams,
            carbsGrams = macros.carbsGrams,
            fatGrams = macros.fatGrams,
            isMacroBalanced = macros.isBalanced,
            macroFeedback = if (macros.isBalanced) "Right macros: Balanced protein & energy profile." else "Out of target balance: High fat or carbohydrate proportion.",
            isOfflineResult = !isWeb,
            searchGrounded = isWeb,
            searchSource = if (isWeb) "Google Search Grounded" else null,
            userCorrection = if (isWeb) item.name else null
        )
        _analysisResult.value = result
        _scanMealType.value = mealType
        if (note.isNotBlank()) {
            _scanNotes.value = note
        }
        _isSearchDialogOpen.value = false
        _currentTab.value = AppTab.SCANNER
        showToast("Loaded ${item.name} into Macro Indicator Window!")
    }

    fun addFoodItemAndLog(
        item: com.example.model.FoodDatabaseItem,
        portions: Float,
        mealType: MealType = _scanMealType.value,
        note: String = ""
    ) {
        val macros = item.calculateMacros(portions)
        val isWeb = item.category.equals("Internet Search", ignoreCase = true)
        viewModelScope.launch {
            repository.addMeal(
                mealName = "${item.name} (${macros.portionDescription})",
                mealType = mealType.name,
                calories = macros.calories,
                protein = macros.proteinGrams,
                carbs = macros.carbsGrams,
                fat = macros.fatGrams,
                isMacroBalanced = macros.isBalanced,
                macroFeedback = if (macros.isBalanced) "Right macros: Balanced protein & energy ratio." else "Out of target balance: High fat or carbohydrate proportion.",
                imageUri = null,
                plainNote = if (isWeb && note.isNotBlank()) "$note [Web Search Grounded]" else if (isWeb) "[Web Search Grounded]" else note
            )
            showToast("Logged ${item.name} (${macros.calories} kcal) to ${mealType.name.lowercase().replaceFirstChar { it.uppercase() }}!")
        }
    }

    fun addCustomManualFoodAndLog(
        name: String,
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float,
        mealType: MealType,
        note: String
    ) {
        val isBalanced = FoodRecognitionService.checkMacroBalance(calories, protein, carbs, fat)
        viewModelScope.launch {
            repository.addMeal(
                mealName = name,
                mealType = mealType.name,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                isMacroBalanced = isBalanced,
                macroFeedback = if (isBalanced) "Manually Added: Right macros (Meets target nutritional ratios)." else "Manually Added: Out of target macro balance.",
                imageUri = null,
                plainNote = note
            )
            showToast("Added $name ($calories kcal) to Daily Log!")
            _isSearchDialogOpen.value = false
            _currentTab.value = AppTab.DAILY_LOG
        }
    }

    fun updateMeal(
        meal: MealRecord,
        updatedName: String,
        updatedCalories: Int,
        updatedProtein: Float,
        updatedCarbs: Float,
        updatedFat: Float
    ) {
        viewModelScope.launch {
            val isBalanced = FoodRecognitionService.checkMacroBalance(updatedCalories, updatedProtein, updatedCarbs, updatedFat)
            val updated = meal.copy(
                mealName = updatedName.trim(),
                calories = updatedCalories,
                proteinGrams = updatedProtein,
                carbsGrams = updatedCarbs,
                fatGrams = updatedFat,
                isMacroBalanced = isBalanced,
                macroFeedback = if (isBalanced) "Edited: Right macros (Balanced protein & energy ratio)." else "Edited: Out of target macro balance."
            )
            repository.updateMeal(updated)
            showToast("Updated ${updated.mealName} (${updated.calories} kcal)")
        }
    }

    fun deleteMeal(meal: MealRecord) {
        viewModelScope.launch {
            repository.deleteMeal(meal)
            showToast("Removed ${meal.mealName}")
        }
    }

    fun logWeight(weightVal: Float, isEnteredInLbs: Boolean, note: String) {
        viewModelScope.launch {
            val finalKg = if (isEnteredInLbs) weightVal / 2.20462f else weightVal
            val displayUnit = if (isEnteredInLbs) "lb" else "kg"
            repository.addWeightRecord(finalKg, note)
            showToast("Recorded weight: ${String.format("%.1f", weightVal)} $displayUnit")
        }
    }

    fun deleteWeight(weight: WeightRecord) {
        viewModelScope.launch {
            repository.deleteWeightRecord(weight)
            showToast("Deleted weight entry")
        }
    }

    fun syncWearable() {
        viewModelScope.launch {
            repository.syncWearableData(
                deviceName = userGoals.value.connectedWearableName,
                steps = (8200..12800).random(),
                activeCalories = (420..640).random(),
                heartRate = (58..68).random(),
                sleepHours = (70..88).random() / 10f
            )
            showToast("Biometric Sync Complete: Activity & Calorie targets updated!")
        }
    }

    fun updateOverlayConfig(
        days: Int? = null,
        weight: Boolean? = null,
        calories: Boolean? = null,
        protein: Boolean? = null,
        carbs: Boolean? = null,
        fat: Boolean? = null
    ) {
        val curr = _overlayConfig.value
        _overlayConfig.value = curr.copy(
            timeWindowDays = days ?: curr.timeWindowDays,
            showWeight = weight ?: curr.showWeight,
            showCalories = calories ?: curr.showCalories,
            showProtein = protein ?: curr.showProtein,
            showCarbs = carbs ?: curr.showCarbs,
            showFat = fat ?: curr.showFat
        )
    }

    fun updateDarkMode(mode: String) {
        viewModelScope.launch {
            repository.saveUserGoals(userGoals.value.copy(darkModeSetting = mode))
            showToast("Theme updated to $mode")
        }
    }

    fun updateGoals(
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float,
        targetWeight: Float,
        useLbs: Boolean
    ) {
        viewModelScope.launch {
            val updated = userGoals.value.copy(
                baseCalorieTarget = calories,
                targetProteinGrams = protein,
                targetCarbsGrams = carbs,
                targetFatGrams = fat,
                targetWeightKg = targetWeight,
                useLbs = useLbs
            )
            repository.saveUserGoals(updated)
            showToast("Goals updated successfully!")
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun getDecryptedNote(encrypted: String): String {
        return repository.decryptNote(encrypted)
    }
}
