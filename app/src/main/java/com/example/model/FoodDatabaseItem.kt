package com.example.model

data class FoodDatabaseItem(
    val id: String,
    val name: String,
    val category: String,
    val defaultServingDescription: String,
    val defaultServingGrams: Float = 100f,
    val caloriesPerServing: Int,
    val proteinPerServing: Float,
    val carbsPerServing: Float,
    val fatPerServing: Float
) {
    fun calculateMacros(portions: Float): CalculatedMacros {
        val safePortion = portions.coerceAtLeast(0.1f)
        val cal = (caloriesPerServing * safePortion).toInt()
        val p = proteinPerServing * safePortion
        val c = carbsPerServing * safePortion
        val f = fatPerServing * safePortion

        // Evaluate macro balance: Protein >= 18% of calories, Fat <= 42%
        val pCal = p * 4f
        val fCal = f * 9f
        val totalCal = (pCal + (c * 4f) + fCal).coerceAtLeast(1f)
        val isBalanced = (pCal / totalCal >= 0.18f) && (fCal / totalCal <= 0.42f)

        return CalculatedMacros(
            calories = cal,
            proteinGrams = p,
            carbsGrams = c,
            fatGrams = f,
            isBalanced = isBalanced,
            portionDescription = if (safePortion == 1f) defaultServingDescription else "${String.format("%.1f", safePortion)}x $defaultServingDescription"
        )
    }
}

data class CalculatedMacros(
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val isBalanced: Boolean,
    val portionDescription: String
)

object FoodCatalog {
    val items: List<FoodDatabaseItem> = listOf(
        // High Protein & Meats
        FoodDatabaseItem("1", "Grilled Chicken Breast", "Proteins", "1 breast (150g / 5.3 oz)", 150f, 240, 46.0f, 0.0f, 4.5f),
        FoodDatabaseItem("2", "Wild Atlantic Salmon", "Proteins", "1 fillet (170g / 6 oz)", 170f, 350, 38.0f, 0.0f, 21.0f),
        FoodDatabaseItem("3", "Extra Lean Ground Beef (93/7)", "Proteins", "1 patty (120g / 4.2 oz)", 120f, 205, 26.0f, 0.0f, 10.0f),
        FoodDatabaseItem("4", "Large Whole Eggs", "Proteins", "2 large eggs (100g)", 100f, 145, 12.5f, 0.8f, 10.0f),
        FoodDatabaseItem("5", "Liquid Egg Whites", "Proteins", "1/2 cup (120ml)", 120f, 65, 14.0f, 1.0f, 0.2f),
        FoodDatabaseItem("6", "Non-Fat Greek Yogurt (Plain)", "Proteins", "1 cup (225g / 8 oz)", 225f, 130, 22.0f, 8.0f, 0.5f),
        FoodDatabaseItem("7", "Whey Protein Isolate", "Proteins", "1 scoop (30g)", 30f, 120, 25.0f, 2.0f, 1.0f),
        FoodDatabaseItem("8", "Firm Organic Tofu", "Proteins", "100g (3.5 oz)", 100f, 115, 12.0f, 2.5f, 6.0f),
        FoodDatabaseItem("9", "Canned Chunk White Tuna", "Proteins", "1 can drained (140g)", 140f, 150, 33.0f, 0.0f, 1.5f),
        FoodDatabaseItem("10", "Low-Fat Cottage Cheese", "Proteins", "1 cup (226g)", 226f, 180, 28.0f, 6.0f, 3.0f),
        FoodDatabaseItem("11", "Roast Turkey Breast", "Proteins", "1 serving (115g / 4 oz)", 115f, 155, 34.0f, 0.0f, 2.0f),

        // Carbs & Grains
        FoodDatabaseItem("12", "Jasmine White Rice (Cooked)", "Carbs", "1 cup (185g)", 185f, 240, 4.5f, 53.0f, 0.5f),
        FoodDatabaseItem("13", "Brown Rice (Cooked)", "Carbs", "1 cup (195g)", 195f, 218, 4.5f, 46.0f, 1.6f),
        FoodDatabaseItem("14", "Rolled Whole Oats", "Carbs", "1/2 cup dry (40g)", 40f, 150, 5.0f, 27.0f, 2.5f),
        FoodDatabaseItem("15", "Baked Sweet Potato", "Carbs", "1 medium (150g)", 150f, 135, 3.0f, 31.0f, 0.2f),
        FoodDatabaseItem("16", "Quinoa (Cooked)", "Carbs", "1 cup (185g)", 185f, 222, 8.0f, 39.0f, 3.5f),
        FoodDatabaseItem("17", "Whole Grain Bread", "Carbs", "2 slices (68g)", 68f, 160, 8.0f, 26.0f, 2.0f),
        FoodDatabaseItem("18", "Black Beans (Cooked)", "Carbs", "1 cup (172g)", 172f, 227, 15.0f, 41.0f, 0.9f),
        FoodDatabaseItem("19", "Whole Wheat Pasta (Cooked)", "Carbs", "1 cup (140g)", 140f, 174, 7.5f, 37.0f, 0.8f),

        // Vegetables & Greens
        FoodDatabaseItem("20", "Steamed Broccoli Florets", "Veggies", "1 cup (90g)", 90f, 35, 2.5f, 6.0f, 0.4f),
        FoodDatabaseItem("21", "Baby Spinach (Fresh)", "Veggies", "2 cups raw (60g)", 60f, 15, 1.8f, 2.2f, 0.2f),
        FoodDatabaseItem("22", "Grilled Asparagus", "Veggies", "6 spears (90g)", 90f, 25, 2.6f, 4.5f, 0.2f),
        FoodDatabaseItem("23", "Mixed Garden Salad", "Veggies", "1 bowl (120g)", 120f, 28, 1.5f, 5.0f, 0.3f),

        // Fruits
        FoodDatabaseItem("24", "Fresh Honeycrisp Apple", "Fruits", "1 medium (180g)", 180f, 95, 0.5f, 25.0f, 0.3f),
        FoodDatabaseItem("25", "Ripe Banana", "Fruits", "1 medium (118g)", 118f, 105, 1.3f, 27.0f, 0.4f),
        FoodDatabaseItem("26", "Fresh Blueberries", "Fruits", "1 cup (150g)", 150f, 85, 1.1f, 21.0f, 0.5f),
        FoodDatabaseItem("27", "Fresh Strawberries", "Fruits", "1 cup halves (150g)", 150f, 50, 1.0f, 11.5f, 0.4f),

        // Healthy Fats & Seeds
        FoodDatabaseItem("28", "Hass Avocado", "Fats", "1/2 avocado (75g)", 75f, 120, 1.5f, 6.0f, 11.0f),
        FoodDatabaseItem("29", "Raw California Almonds", "Fats", "1 oz (28g, ~23 nuts)", 28f, 165, 6.0f, 6.0f, 14.0f),
        FoodDatabaseItem("30", "Natural Peanut Butter", "Fats", "2 tbsp (32g)", 32f, 190, 8.0f, 7.0f, 16.0f),
        FoodDatabaseItem("31", "Extra Virgin Olive Oil", "Fats", "1 tbsp (15ml)", 15f, 120, 0.0f, 0.0f, 14.0f),
        FoodDatabaseItem("32", "Chia Seeds", "Fats", "2 tbsp (24g)", 24f, 115, 4.0f, 10.0f, 7.5f),

        // Common Meals
        FoodDatabaseItem("33", "Grilled Chicken Caesar Salad", "Meals", "1 large bowl (320g)", 320f, 420, 36.0f, 14.0f, 22.0f),
        FoodDatabaseItem("34", "Avocado Toast & Poached Egg", "Meals", "2 slices with 1 egg", 220f, 380, 18.0f, 29.0f, 21.0f),
        FoodDatabaseItem("35", "Beef & Rice Burrito Bowl", "Meals", "1 bowl (400g)", 400f, 620, 38.0f, 68.0f, 20.0f),
        FoodDatabaseItem("36", "Classic Pepperoni Pizza", "Meals", "1 slice (110g)", 110f, 290, 12.0f, 32.0f, 13.0f),
        FoodDatabaseItem("37", "Double Cheeseburger", "Meals", "1 burger (220g)", 220f, 610, 32.0f, 42.0f, 35.0f)
    )
}
