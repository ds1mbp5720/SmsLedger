package com.example.smsledger.feature.ledger

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smsledger.domain.model.Category
import com.example.smsledger.domain.model.ParsingRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(viewModel: LedgerViewModel) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(0) } // 0: List, 1: Stats, 2: Settings

    // State for Bottom Sheets
    var showAddCategoryScreen by remember { mutableStateOf(false) }
    var showAddRuleScreen by remember { mutableStateOf(false) }
    var editingCategoryForScreen by remember { mutableStateOf<Category?>(null) }
    var editingRuleForScreen by remember { mutableStateOf<ParsingRule?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.systemBarsPadding(),
            containerColor = Color(0xFFF8FAFC),
            topBar = {
                Surface(
                    color = Color.White,
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "SMS 가계부",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2563EB),
                                    fontSize = 20.sp
                                )
                            )
                            Row {
                                IconButton(onClick = { currentTab = 0 }) {
                                    Icon(Icons.Default.List, contentDescription = null, tint = if(currentTab == 0) Color(0xFF2563EB) else Color(0xFF94A3B8))
                                }
                                IconButton(onClick = { currentTab = 1 }) {
                                    Icon(Icons.Default.BarChart, contentDescription = null, tint = if(currentTab == 1) Color(0xFF2563EB) else Color(0xFF94A3B8))
                                }
                                IconButton(onClick = { currentTab = 2 }) {
                                    Icon(Icons.Default.Settings, contentDescription = null, tint = if(currentTab == 2) Color(0xFF2563EB) else Color(0xFF94A3B8))
                                }
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (state.selectedMonth == 0) viewModel.handleIntent(LedgerIntent.ChangeMonth(11, state.selectedYear - 1))
                                    else viewModel.handleIntent(LedgerIntent.ChangeMonth(state.selectedMonth - 1, state.selectedYear))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color(0xFF2563EB))
                            }
                            Text(
                                "${state.selectedYear}.${String.format("%02d", state.selectedMonth + 1)}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E293B)
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (state.selectedMonth == 11) viewModel.handleIntent(LedgerIntent.ChangeMonth(0, state.selectedYear + 1))
                                    else viewModel.handleIntent(LedgerIntent.ChangeMonth(state.selectedMonth + 1, state.selectedYear))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF2563EB))
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                if (currentTab == 0) {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 12.dp,
                            hoveredElevation = 10.dp,
                            focusedElevation = 10.dp
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "추가", modifier = Modifier.size(24.dp))
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (currentTab) {
                    0 -> TransactionListView(
                        state = state, 
                        viewModel = viewModel,
                        onAddCategory = {
                            editingCategoryForScreen = null
                            showAddCategoryScreen = true
                        }
                    )
                    1 -> StatisticsView(state)
                    2 -> SettingsView(
                        state = state, 
                        viewModel = viewModel,
                        onShowAddCategory = { category ->
                            editingCategoryForScreen = category
                            showAddCategoryScreen = true
                        },
                        onShowAddRule = { rule ->
                            editingRuleForScreen = rule
                            showAddRuleScreen = true
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showAddDialog,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AddTransactionScreen(
                state = state,
                ledgerViewModel = viewModel,
                onDismiss = { showAddDialog = false },
                onConfirm = { amount, store, category, type ->
                    viewModel.handleIntent(LedgerIntent.Add(amount, store, category, type))
                    showAddDialog = false
                },
                onAddCategory = { 
                    viewModel.handleIntent(LedgerIntent.AddCategory(it))
                }
            )
        }

        AnimatedVisibility(
            visible = showAddCategoryScreen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AddCategoryScreen(
                category = editingCategoryForScreen,
                existingCategories = state.categories,
                onDismiss = { showAddCategoryScreen = false },
                onConfirm = { name ->
                    if (editingCategoryForScreen == null) {
                        viewModel.handleIntent(LedgerIntent.AddCategory(name))
                    } else {
                        viewModel.handleIntent(LedgerIntent.UpdateCategoryName(editingCategoryForScreen!!, name))
                    }
                    showAddCategoryScreen = false
                }
            )
        }

        AnimatedVisibility(
            visible = showAddRuleScreen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AddParsingRuleScreen(
                rule = editingRuleForScreen,
                onDismiss = { showAddRuleScreen = false },
                onConfirm = { rule ->
                    if (editingRuleForScreen == null) {
                        viewModel.handleIntent(LedgerIntent.AddParsingRule(rule))
                    } else {
                        viewModel.handleIntent(LedgerIntent.UpdateParsingRule(rule.copy(id = editingRuleForScreen!!.id)))
                    }
                    showAddRuleScreen = false
                }
            )
        }
    }
}
