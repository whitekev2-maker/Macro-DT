package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FoodCatalog
import com.example.model.FoodDatabaseItem
import com.example.model.MealType
import com.example.model.UserMacroGoals
import com.example.ui.theme.DietGreenPrimary
import com.example.ui.theme.MacroCalorieColor
import com.example.ui.theme.MacroCarbColor
import com.example.ui.theme.MacroFatColor
import com.example.ui.theme.MacroGreenGood
import com.example.ui.theme.MacroProteinColor
import com.example.ui.theme.MacroRedBad
import com.example.vision.FoodRecognitionService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFoodBottomSheet(
    onDismiss: () -> Unit,
    onFoodSelected: (FoodDatabaseItem, Float, MealType, String) -> Unit,
    onCustomFoodAdded: (String, Int, Float, Float, Float, MealType, String) -> Unit,
    userGoals: UserMacroGoals = UserMacroGoals(),
    initialQuery: String = ""
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Catalog & Web, 1: Live Web Search, 2: Custom Food

    var searchQuery by remember { mutableStateOf(initialQuery) }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedFoodItem by remember { mutableStateOf<FoodDatabaseItem?>(null) }
    var portionMultiplier by remember { mutableFloatStateOf(1.0f) }
    var mealType by remember { mutableStateOf(MealType.LUNCH) }
    var plainNote by remember { mutableStateOf("") }

    // Internet Search State
    var isSearchingInternet by remember { mutableStateOf(false) }
    var internetSearchError by remember { mutableStateOf<String?>(null) }
    var webSearchResults by remember { mutableStateOf<List<FoodDatabaseItem>>(emptyList()) }

    // Custom food state
    var customName by remember { mutableStateOf("") }
    var customCalories by remember { mutableStateOf("") }
    var customProtein by remember { mutableStateOf("") }
    var customCarbs by remember { mutableStateOf("") }
    var customFat by remember { mutableStateOf("") }
    var customMealType by remember { mutableStateOf(MealType.LUNCH) }
    var customNote by remember { mutableStateOf("") }

    val categories = listOf("All", "Proteins", "Carbs", "Veggies", "Fruits", "Fats", "Meals")

    val filteredItems = remember(searchQuery, selectedCategory) {
        FoodCatalog.items.filter { item ->
            val matchesCategory = (selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true))
            val matchesQuery = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true) || item.defaultServingDescription.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    // Function to query the internet for meal information
    fun executeInternetSearch(query: String) {
        if (query.isBlank()) return
        coroutineScope.launch {
            isSearchingInternet = true
            internetSearchError = null
            try {
                val result = FoodRecognitionService.resubmitWithWebSearch(
                    correctedItem = query.trim(),
                    userGoals = userGoals,
                    bitmap = null
                )
                val webItem = FoodDatabaseItem(
                    id = "web_${System.currentTimeMillis()}_${query.hashCode()}",
                    name = result.mealName,
                    category = "Internet Search",
                    defaultServingDescription = "1 standard serving (Web Verified)",
                    defaultServingGrams = 100f,
                    caloriesPerServing = result.calories,
                    proteinPerServing = result.proteinGrams,
                    carbsPerServing = result.carbsGrams,
                    fatPerServing = result.fatGrams
                )
                webSearchResults = listOf(webItem) + webSearchResults.filter { it.name != webItem.name }
                selectedFoodItem = webItem
                portionMultiplier = 1.0f
            } catch (e: Exception) {
                internetSearchError = "Could not complete web search: ${e.message}"
            } finally {
                isSearchingInternet = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = DietGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Search & Add Meal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Query local database or internet for meal info",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Tab Bar: Catalog & Web | 🌐 Live Web Search | Custom Food
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        selectedFoodItem = null
                    },
                    text = { Text("Catalog", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        selectedFoodItem = null
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = DietGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Web Search", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        selectedFoodItem = null
                    },
                    text = { Text("Custom Food", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // TAB 0: Catalog & Integrated Internet Search
            // ==========================================
            if (selectedTab == 0) {
                if (selectedFoodItem == null) {
                    // Search Bar with Integrated "Search Web" Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("food_search_input"),
                            placeholder = { Text("Search chicken, oatmeal, pad thai...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear"
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                if (searchQuery.isNotBlank()) {
                                    executeInternetSearch(searchQuery)
                                }
                            }),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Dedicated Internet Search Button on the search bar
                        Button(
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    executeInternetSearch(searchQuery)
                                }
                            },
                            enabled = searchQuery.isNotBlank() && !isSearchingInternet,
                            colors = ButtonDefaults.buttonColors(containerColor = DietGreenPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("search_internet_button"),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            if (isSearchingInternet) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Search Web",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Search",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Internet Search Banner when user has typed text
                    if (searchQuery.isNotBlank()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { executeInternetSearch(searchQuery) }
                                .testTag("search_web_quick_trigger"),
                            color = DietGreenPrimary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, DietGreenPrimary.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TravelExplore,
                                        contentDescription = null,
                                        tint = DietGreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Search Internet for \"$searchQuery\"",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = DietGreenPrimary
                                        )
                                        Text(
                                            text = "Query Google Search Grounding for live verified macros",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = DietGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Category filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isSearchingInternet) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = DietGreenPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Querying internet for meal information...",
                                fontSize = 13.sp,
                                color = DietGreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Combined Results: Web Search Results + Filtered Local Catalog Items
                    val combinedList = webSearchResults + filteredItems.filter { local ->
                        webSearchResults.none { it.name.equals(local.name, ignoreCase = true) }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (combinedList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "No matching foods found in catalog",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        if (searchQuery.isNotBlank()) {
                                            Button(
                                                onClick = { executeInternetSearch(searchQuery) },
                                                colors = ButtonDefaults.buttonColors(containerColor = DietGreenPrimary),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Language,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("🌐 Search Internet for \"$searchQuery\"")
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        OutlinedButton(
                                            onClick = { selectedTab = 2 },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Create Custom Food")
                                        }
                                    }
                                }
                            }
                        } else {
                            items(combinedList, key = { it.id }) { item ->
                                FoodListItem(
                                    item = item,
                                    onClick = {
                                        selectedFoodItem = item
                                        portionMultiplier = 1.0f
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Selected Food Detail & Portion Adjustment View
                    SelectedFoodDetailView(
                        food = selectedFoodItem!!,
                        portionMultiplier = portionMultiplier,
                        onPortionChange = { portionMultiplier = it },
                        mealType = mealType,
                        onMealTypeChange = { mealType = it },
                        plainNote = plainNote,
                        onPlainNoteChange = { plainNote = it },
                        onBack = { selectedFoodItem = null },
                        onConfirm = { food, mult, type, note ->
                            onFoodSelected(food, mult, type, note)
                        }
                    )
                }
            }

            // ==========================================
            // TAB 1: Dedicated Live Web Search Tab
            // ==========================================
            else if (selectedTab == 1) {
                if (selectedFoodItem == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Surface(
                            color = DietGreenPrimary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TravelExplore,
                                    contentDescription = null,
                                    tint = DietGreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Search Internet For Any Meal",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Retrieves authoritative calories, protein, carbs, and fat from Google Search Grounding.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Search Input & Action Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("web_search_input"),
                                placeholder = { Text("e.g. Cinnamon roll, Pad Thai, Chipotle bowl...") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = DietGreenPrimary
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    if (searchQuery.isNotBlank()) {
                                        executeInternetSearch(searchQuery)
                                    }
                                }),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { executeInternetSearch(searchQuery) },
                                enabled = searchQuery.isNotBlank() && !isSearchingInternet,
                                colors = ButtonDefaults.buttonColors(containerColor = DietGreenPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("tab_internet_search_button"),
                                contentPadding = PaddingValues(horizontal = 14.dp)
                            ) {
                                if (isSearchingInternet) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Search 🌐", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Popular Search Suggestions Chips
                        Text(
                            text = "Popular Meal Searches:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val suggestions = listOf(
                                "Cinnamon Roll 🥐",
                                "Chipotle Bowl 🥗",
                                "Avocado Toast 🥑",
                                "Grilled Salmon 🐟",
                                "In-N-Out Burger 🍔",
                                "Pad Thai Noodles 🍜",
                                "Greek Salad 🥗",
                                "Protein Shake 🥤"
                            )
                            suggestions.forEach { suggestion ->
                                val cleanQuery = suggestion.substringBefore(" ").trim()
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        searchQuery = cleanQuery
                                        executeInternetSearch(cleanQuery)
                                    },
                                    label = { Text(suggestion, fontSize = 12.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isSearchingInternet) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = DietGreenPrimary,
                                        modifier = Modifier.size(36.dp),
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Searching internet for \"$searchQuery\"...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Querying live nutritional database & macros",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else if (webSearchResults.isNotEmpty()) {
                            Text(
                                text = "Recent Internet Search Results (${webSearchResults.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(webSearchResults, key = { it.id }) { item ->
                                    FoodListItem(
                                        item = item,
                                        onClick = {
                                            selectedFoodItem = item
                                            portionMultiplier = 1.0f
                                        }
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TravelExplore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Type any meal or restaurant item above",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Tap 'Search' to retrieve live verified nutrition facts",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                } else {
                    SelectedFoodDetailView(
                        food = selectedFoodItem!!,
                        portionMultiplier = portionMultiplier,
                        onPortionChange = { portionMultiplier = it },
                        mealType = mealType,
                        onMealTypeChange = { mealType = it },
                        plainNote = plainNote,
                        onPlainNoteChange = { plainNote = it },
                        onBack = { selectedFoodItem = null },
                        onConfirm = { food, mult, type, note ->
                            onFoodSelected(food, mult, type, note)
                        }
                    )
                }
            }

            // ==========================================
            // TAB 2: Custom Manual Food Creator
            // ==========================================
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Add Custom Food Manually",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter custom nutrition label values for your personal food database.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Food / Meal Name *") },
                            placeholder = { Text("e.g. Homemade Protein Pancakes") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_food_name_input"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = customCalories,
                            onValueChange = { customCalories = it },
                            label = { Text("Total Calories (kcal) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_food_calories_input"),
                            singleLine = true
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customProtein,
                                onValueChange = { customProtein = it },
                                label = { Text("Protein (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_food_protein_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = customCarbs,
                                onValueChange = { customCarbs = it },
                                label = { Text("Carbs (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_food_carbs_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = customFat,
                                onValueChange = { customFat = it },
                                label = { Text("Fat (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_food_fat_input"),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Meal Classification:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MealType.values().forEach { type ->
                                FilterChip(
                                    selected = customMealType == type,
                                    onClick = { customMealType = type },
                                    label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = customNote,
                            onValueChange = { customNote = it },
                            label = { Text("Encrypted Privacy Note (Optional)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = DietGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (customName.isNotBlank()) {
                                    val cals = customCalories.toIntOrNull() ?: 250
                                    val p = customProtein.toFloatOrNull() ?: 20f
                                    val cb = customCarbs.toFloatOrNull() ?: 25f
                                    val f = customFat.toFloatOrNull() ?: 8f
                                    onCustomFoodAdded(customName, cals, p, cb, f, customMealType, customNote)
                                }
                            },
                            enabled = customName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = DietGreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_custom_food_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save & Log Food", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedFoodDetailView(
    food: FoodDatabaseItem,
    portionMultiplier: Float,
    onPortionChange: (Float) -> Unit,
    mealType: MealType,
    onMealTypeChange: (MealType) -> Unit,
    plainNote: String,
    onPlainNoteChange: (String) -> Unit,
    onBack: () -> Unit,
    onConfirm: (FoodDatabaseItem, Float, MealType, String) -> Unit
) {
    val calculated = food.calculateMacros(portionMultiplier)
    val isWeb = food.category.equals("Internet Search", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isWeb) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = DietGreenPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "🌐 Web Verified",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "Base Serving: ${food.defaultServingDescription}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Green/Red macro badge
            Surface(
                color = if (calculated.isBalanced) MacroGreenGood.copy(alpha = 0.15f) else MacroRedBad.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (calculated.isBalanced) MacroGreenGood else MacroRedBad, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (calculated.isBalanced) "Right Macros 🥗" else "Out of Balance ⚠️",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (calculated.isBalanced) MacroGreenGood else MacroRedBad
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Portion Multiplier Selector
        Text(
            text = "Select Portion Size:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { mult ->
                FilterChip(
                    selected = portionMultiplier == mult,
                    onClick = { onPortionChange(mult) },
                    label = { Text("${mult}x", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Dynamic Macro Breakdown Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Nutritional Breakdown (${calculated.portionDescription})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MacroStatItem(label = "Calories", value = "${calculated.calories}", unit = "kcal", color = MacroCalorieColor)
                    MacroStatItem(label = "Protein", value = "${calculated.proteinGrams.toInt()}", unit = "g", color = MacroProteinColor)
                    MacroStatItem(label = "Carbs", value = "${calculated.carbsGrams.toInt()}", unit = "g", color = MacroCarbColor)
                    MacroStatItem(label = "Fat", value = "${calculated.fatGrams.toInt()}", unit = "g", color = MacroFatColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Meal Type Selector
        Text(
            text = "Meal Type:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MealType.values().forEach { type ->
                FilterChip(
                    selected = mealType == type,
                    onClick = { onMealTypeChange(type) },
                    label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Note field
        OutlinedTextField(
            value = plainNote,
            onValueChange = onPlainNoteChange,
            label = { Text("Encrypted Note (Optional)") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = DietGreenPrimary,
                    modifier = Modifier.size(16.dp)
                )
            },
            placeholder = { Text("e.g. Added to salad bowl") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons: Back vs Add
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back to Search")
            }

            Button(
                onClick = {
                    onConfirm(food, portionMultiplier, mealType, plainNote)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DietGreenPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("confirm_add_food_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Food (${calculated.calories} kcal)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FoodListItem(
    item: FoodDatabaseItem,
    onClick: () -> Unit
) {
    val isWeb = item.category.equals("Internet Search", ignoreCase = true)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("food_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWeb) DietGreenPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = if (isWeb) BorderStroke(1.dp, DietGreenPrimary.copy(alpha = 0.4f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isWeb) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = DietGreenPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "🌐 Web Verified",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${item.category} • ${item.defaultServingDescription}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${item.caloriesPerServing} kcal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MacroCalorieColor
                )
                Text(
                    text = "P: ${item.proteinPerServing.toInt()}g • C: ${item.carbsPerServing.toInt()}g • F: ${item.fatPerServing.toInt()}g",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
