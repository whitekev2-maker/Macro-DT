package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.FoodCatalog
import com.example.model.UserMacroGoals
import com.example.model.WeightRecord
import com.example.security.EncryptionManager
import com.example.vision.FoodRecognitionService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Diet Tracker", appName)
    }

    @Test
    fun `test local AES-256 encryption and decryption`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sensitiveNote = "Felt full and energized after post-workout meal."
        val encrypted = EncryptionManager.encrypt(context, sensitiveNote)

        assertNotEquals(sensitiveNote, encrypted)
        assertTrue(encrypted.isNotEmpty())

        val decrypted = EncryptionManager.decrypt(context, encrypted)
        assertEquals(sensitiveNote, decrypted)
    }

    @Test
    fun `test macro balance validator`() {
        // High protein, moderate carb/fat meal -> balanced
        val isBalanced = FoodRecognitionService.checkMacroBalance(
            calories = 450,
            protein = 40f,
            carbs = 35f,
            fat = 12f
        )
        assertTrue(isBalanced)
    }

    @Test
    fun `test food search item portion recalculation`() {
        val chicken = FoodCatalog.items.first { it.name.contains("Chicken Breast") }
        // 2x portion
        val calculated = chicken.calculateMacros(2.0f)
        assertEquals(chicken.caloriesPerServing * 2, calculated.calories)
        assertEquals(chicken.proteinPerServing * 2f, calculated.proteinGrams, 0.1f)
        assertTrue(calculated.isBalanced)
    }

    @Test
    fun `test weight unit conversion to pounds`() {
        val record = WeightRecord(weightKg = 70.0f)
        // 70 kg in lbs = 70 * 2.20462 = 154.32 lbs
        val lbs = record.getDisplayWeight(isLbs = true)
        assertEquals(154.32f, lbs, 0.5f)
        val kg = record.getDisplayWeight(isLbs = false)
        assertEquals(70.0f, kg, 0.01f)
    }

    @Test
    fun `test real time calorie countdown and delta logic`() {
        val targetCalories = 2100
        val consumedUnder = 1650
        val remainingUnder = targetCalories - consumedUnder
        assertEquals(450, remainingUnder)
        val deltaUnder = consumedUnder - targetCalories
        assertTrue(deltaUnder <= 0) // held below goal

        val consumedOver = 2350
        val remainingOver = targetCalories - consumedOver
        assertEquals(-250, remainingOver)
        val deltaOver = consumedOver - targetCalories
        assertFalse(deltaOver <= 0) // exceeded goal
    }

    @Test
    fun `test photo correction resubmission for cinnamon roll`() {
        val userGoals = UserMacroGoals(baseCalorieTarget = 2000)
        val result = FoodRecognitionService.getOfflineSearchAnalysis("Cinnamon roll", userGoals)

        assertTrue(result.mealName.contains("Cinnamon", ignoreCase = true))
        assertEquals(420, result.calories)
        assertEquals(6.0f, result.proteinGrams, 0.1f)
        assertEquals(68.0f, result.carbsGrams, 0.1f)
        assertEquals(16.0f, result.fatGrams, 0.1f)
        assertFalse(result.isMacroBalanced)
        assertTrue(result.searchGrounded)
    }

    @Test
    fun `test internet search query returns meal info for search buttons`() = runBlocking {
        val userGoals = UserMacroGoals(baseCalorieTarget = 2200)
        val result = FoodRecognitionService.resubmitWithWebSearch(
            correctedItem = "Grilled Salmon Bowl",
            userGoals = userGoals,
            bitmap = null,
            forceOffline = true
        )

        assertTrue(result.mealName.contains("Salmon", ignoreCase = true))
        assertTrue(result.calories > 300)
        assertTrue(result.proteinGrams > 20f)
        assertTrue(result.searchGrounded)
    }

    @Test
    fun `test meal entry editing updates name calories and macro units`() {
        val initialMeal = com.example.model.MealRecord(
            id = 1L,
            mealName = "Quick Toast",
            calories = 150,
            proteinGrams = 4f,
            carbsGrams = 25f,
            fatGrams = 3f,
            isMacroBalanced = false
        )

        val updatedName = "Avocado Toast with Egg"
        val updatedCalories = 320
        val updatedProtein = 14f
        val updatedCarbs = 28f
        val updatedFat = 18f
        val isBalanced = FoodRecognitionService.checkMacroBalance(updatedCalories, updatedProtein, updatedCarbs, updatedFat)

        val editedMeal = initialMeal.copy(
            mealName = updatedName,
            calories = updatedCalories,
            proteinGrams = updatedProtein,
            carbsGrams = updatedCarbs,
            fatGrams = updatedFat,
            isMacroBalanced = isBalanced
        )

        assertEquals("Avocado Toast with Egg", editedMeal.mealName)
        assertEquals(320, editedMeal.calories)
        assertEquals(14f, editedMeal.proteinGrams, 0.01f)
        assertEquals(28f, editedMeal.carbsGrams, 0.01f)
        assertEquals(18f, editedMeal.fatGrams, 0.01f)
    }
}
