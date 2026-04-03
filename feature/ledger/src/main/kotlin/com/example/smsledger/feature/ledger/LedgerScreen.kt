package com.example.smsledger.feature.ledger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.smsledger.domain.model.Category
import com.example.smsledger.domain.model.ParsingRule
import com.example.smsledger.domain.model.Transaction
import com.example.smsledger.domain.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(viewModel: LedgerViewModel) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(0) } // 0: List, 1: Stats, 2: Settings

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
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "추가", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (currentTab) {
                0 -> TransactionListView(state, viewModel)
                1 -> StatisticsView(state)
                2 -> SettingsView(state, viewModel)
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            state = state,
            ledgerViewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, store, category, type ->
                viewModel.handleIntent(LedgerIntent.Add(amount, store, category, type))
                showAddDialog = false
            },
            onAddCategory = { viewModel.handleIntent(LedgerIntent.AddCategory(it)) }
        )
    }
}

@Composable
fun TransactionListView(state: LedgerState, viewModel: LedgerViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Summary Card (Top)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFFF0F7FF),
            border = BorderStroke(1.dp, Color(0xFFE0EFFF))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("수입", color = Color(0xFF64748B), fontSize = 14.sp)
                        Text("₩${String.format("%,d", state.totalIncome)}", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("지출", color = Color(0xFF64748B), fontSize = 14.sp)
                        Text("₩${String.format("%,d", state.totalExpense)}", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFF2563EB).copy(alpha = 0.1f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("합계", color = Color(0xFF1E293B), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("₩${String.format("%,d", state.totalAmount)}", color = Color(0xFF1E293B), fontWeight = FontWeight.Black, fontSize = 22.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search Bar
        Surface(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.handleIntent(LedgerIntent.Search(it)) },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = Color(0xFF1E293B), fontSize = 15.sp),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (state.searchQuery.isEmpty()) {
                                Text("내역 검색", color = Color(0xFF94A3B8), fontSize = 15.sp)
                            }
                            innerTextField()
                        }
                    },
                    singleLine = true
                )
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.handleIntent(LedgerIntent.Search("")) }) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Filter Tabs and Count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterTab("전체", true)
                FilterTab("지출", false)
                FilterTab("수입", false)
            }
            Text(
                text = "총 ${state.transactions.size}건",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF64748B)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(state.transactions) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    categories = state.categories,
                    onCategoryChange = { newCat -> 
                        viewModel.handleIntent(LedgerIntent.UpdateCategory(transaction, newCat))
                    },
                    onDelete = { viewModel.handleIntent(LedgerIntent.Delete(transaction)) },
                    onAddCategory = { viewModel.handleIntent(LedgerIntent.AddCategory(it)) }
                )
            }
        }
    }
}

@Composable
fun FilterTab(text: String, isSelected: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF1E293B) else Color.Transparent,
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.height(34.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color(0xFF64748B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatisticsView(state: LedgerState) {
    val numberFormat = remember { NumberFormat.getCurrencyInstance(Locale.KOREA) }
    
    Column(modifier = Modifier.padding(20.dp).fillMaxSize()) {
        Text(
            "카테고리별 통계",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        if (state.monthlyStats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("데이터가 없습니다.", color = Color(0xFF94A3B8))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                state.monthlyStats.forEach { (category, amount) ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        category,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        numberFormat.format(amount),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color(0xFF2563EB)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                val progress = amount.toFloat() / state.totalAmount.coerceAtLeast(1).toFloat()
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    color = Color(0xFF2563EB),
                                    trackColor = Color(0xFFEFF6FF),
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsView(state: LedgerState, viewModel: LedgerViewModel) {
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<ParsingRule?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    var testSms by remember { mutableStateOf("") }
    var testSender by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Category Management Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "카테고리 관리",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = { 
                        editingCategory = null
                        showAddCategoryDialog = true 
                    }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "카테고리 추가", tint = Color(0xFF2563EB))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.categories.forEach { category ->
                        Surface(
                            onClick = {
                                editingCategory = category
                                showAddCategoryDialog = true
                            },
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(category.name, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "삭제",
                                    modifier = Modifier.size(16.dp).clickable {
                                        categoryToDelete = category
                                    },
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { HorizontalDivider(color = Color(0xFFF1F5F9)) }

        // SMS Parsing Rules Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "SMS 파싱 규칙",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = { 
                        editingRule = null
                        showAddRuleDialog = true 
                    }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "규칙 추가", tint = Color(0xFF2563EB))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                if (state.parsingRules.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Text(
                            "등록된 파싱 규칙이 없습니다.\n우측 상단의 + 버튼을 눌러 추가하세요.",
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        items(state.parsingRules) { rule ->
            ParsingRuleItem(
                rule = rule,
                onEdit = { 
                    editingRule = rule
                    showAddRuleDialog = true 
                },
                onDelete = { viewModel.handleIntent(LedgerIntent.DeleteParsingRule(rule)) },
                onToggle = { viewModel.handleIntent(LedgerIntent.UpdateParsingRule(rule.copy(isActive = !rule.isActive))) }
            )
        }

        item { HorizontalDivider(color = Color(0xFFF1F5F9)) }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "파싱 테스트",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color(0xFF1E293B)
                )
                
                OutlinedTextField(
                    value = testSender,
                    onValueChange = { testSender = it },
                    label = { Text("발신 번호 (선택사항)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("예: 1588-8100") }
                )

                OutlinedTextField(
                    value = testSms,
                    onValueChange = { testSms = it },
                    label = { Text("테스트 문자 내용") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )

                Button(
                    onClick = { viewModel.handleIntent(LedgerIntent.ParseSms(testSms, testSender.ifBlank { null })) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("테스트 실행 (DB 저장됨)", fontWeight = FontWeight.Bold)
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    if (showAddRuleDialog) {
        AddParsingRuleDialog(
            rule = editingRule,
            onDismiss = { showAddRuleDialog = false },
            onConfirm = { rule ->
                if (editingRule == null) {
                    viewModel.handleIntent(LedgerIntent.AddParsingRule(rule))
                } else {
                    viewModel.handleIntent(LedgerIntent.UpdateParsingRule(rule.copy(id = editingRule!!.id)))
                }
                showAddRuleDialog = false
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            category = editingCategory,
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name ->
                if (editingCategory == null) {
                    viewModel.handleIntent(LedgerIntent.AddCategory(name))
                } else {
                    viewModel.handleIntent(LedgerIntent.UpdateCategoryName(editingCategory!!, name))
                }
                showAddCategoryDialog = false
            }
        )
    }

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("카테고리 삭제") },
            text = { Text("'${categoryToDelete?.name}' 카테고리를 삭제하시겠습니까?\n해당 카테고리의 내역은 '미분류'로 이동됩니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.handleIntent(LedgerIntent.DeleteCategory(categoryToDelete!!))
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) { Text("취소") }
            }
        )
    }
}

@Composable
fun ParsingRuleItem(
    rule: ParsingRule,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (rule.isActive) Color.White else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (rule.isActive) Color(0xFFF1F5F9) else Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            rule.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (rule.type == TransactionType.INCOME) Color(0xFFEFF6FF) else Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (rule.type == TransactionType.INCOME) "수입" else "지출",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (rule.type == TransactionType.INCOME) Color(0xFF2563EB) else Color(0xFFEF4444)
                            )
                        }
                    }
                    if (!rule.senderNumber.isNullOrBlank()) {
                        Text("발신번호: ${rule.senderNumber}", style = MaterialTheme.typography.labelMedium, color = Color(0xFF64748B))
                    }
                }
                Switch(
                    checked = rule.isActive, 
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2563EB),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFE2E8F0),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("금액 패턴: ${rule.amountPattern}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
            Text("상점 패턴: ${rule.storePattern}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) { Text("수정", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold) }
                TextButton(onClick = onDelete) { Text("삭제", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParsingRuleDialog(
    rule: ParsingRule?,
    onDismiss: () -> Unit,
    onConfirm: (ParsingRule) -> Unit
) {
    var name by remember { mutableStateOf(rule?.name ?: "") }
    var senderNumber by remember { mutableStateOf(rule?.senderNumber ?: "") }
    var amountPattern by remember { mutableStateOf(rule?.amountPattern ?: "([0-9,]+)원") }
    var storePattern by remember { mutableStateOf(rule?.storePattern ?: "원\\s+(.+)") }
    var type by remember { mutableStateOf(rule?.type ?: TransactionType.EXPENSE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (rule == null) "파싱 규칙 추가" else "파싱 규칙 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == TransactionType.EXPENSE,
                        onClick = { type = TransactionType.EXPENSE },
                        label = { Text("지출") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == TransactionType.INCOME,
                        onClick = { type = TransactionType.INCOME },
                        label = { Text("수입") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("규칙 이름 (예: 신한은행)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = senderNumber, onValueChange = { senderNumber = it }, label = { Text("발신 번호 (선택사항)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amountPattern, onValueChange = { amountPattern = it }, label = { Text("금액 정규식") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = storePattern, onValueChange = { storePattern = it }, label = { Text("상점명 정규식") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(ParsingRule(name = name, senderNumber = senderNumber.ifBlank { null }, amountPattern = amountPattern, storePattern = storePattern, type = type)) 
            }) {
                Text(if (rule == null) "추가" else "저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    transaction: Transaction, 
    categories: List<Category>,
    onCategoryChange: (String) -> Unit,
    onDelete: () -> Unit,
    onAddCategory: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.KOREA) }
    var expanded by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.storeName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E293B)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dateFormat.format(Date(transaction.date)),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box {
                        Surface(
                            onClick = { expanded = true },
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            val truncatedCategory = if (transaction.category.length > 4) transaction.category.substring(0, 4) + ".." else transaction.category
                            Text(
                                truncatedCategory,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2563EB)
                            )
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        onCategoryChange(category.name)
                                        expanded = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("새 카테고리")
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    showAddCategoryDialog = true
                                }
                            )
                        }
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${transaction.amount}원",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (transaction.type == TransactionType.INCOME) Color(0xFF2563EB) else Color(0xFFEF4444)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color(0xFFCBD5E1), modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { 
                onAddCategory(it)
                onCategoryChange(it)
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    state: LedgerState,
    ledgerViewModel: LedgerViewModel,
    onDismiss: () -> Unit, 
    onConfirm: (Long, String, String, TransactionType) -> Unit,
    onAddCategory: (String) -> Unit
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var store by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(state.categories.firstOrNull()?.name ?: "기타") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var expanded by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var isAiLoading by remember { mutableStateOf(false) }
    var isOcrMode by remember { mutableStateOf(false) }

    // Image/Camera Launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            isAiLoading = true
            ledgerViewModel.processImage(context, it, isOcr = isOcrMode) { result: com.example.smsledger.feature.ledger.AiTransactionResult? ->
                isAiLoading = false
                result?.let { res: com.example.smsledger.feature.ledger.AiTransactionResult ->
                    amount = res.amount.toString()
                    store = res.storeName
                    category = res.category
                    type = if (res.type == "income") TransactionType.INCOME else TransactionType.EXPENSE
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            isAiLoading = true
            ledgerViewModel.processBitmap(it, isOcr = isOcrMode) { result: com.example.smsledger.feature.ledger.AiTransactionResult? ->
                isAiLoading = false
                result?.let { res: com.example.smsledger.feature.ledger.AiTransactionResult ->
                    amount = res.amount.toString()
                    store = res.storeName
                    category = res.category
                    type = if (res.type == "income") TransactionType.INCOME else TransactionType.EXPENSE
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch()
        }
    }

    fun handleCameraAction() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                cameraLauncher.launch()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp).fillMaxWidth(),
        content = {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "내역 추가",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        ),
                        color = Color(0xFF1E293B)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Top AI Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            onClick = { 
                                isOcrMode = false
                                handleCameraAction() 
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0F7FF),
                            border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isAiLoading) Icons.Default.Refresh else Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (isAiLoading) "인식 중..." else "스마트 인식",
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Surface(
                            onClick = { 
                                isOcrMode = true
                                galleryLauncher.launch("image/*") 
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFEF3C7))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.TextFields, 
                                    contentDescription = null, 
                                    tint = Color(0xFFD97706), 
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "텍스트 추출", 
                                    color = Color(0xFFD97706), 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Transaction Type Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { type = TransactionType.EXPENSE },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (type == TransactionType.EXPENSE) Color.White else Color(0xFFF1F5F9),
                            border = if (type == TransactionType.EXPENSE) BorderStroke(1.dp, Color(0xFFCBD5E1)) else null
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "지출", 
                                    color = if (type == TransactionType.EXPENSE) Color(0xFF1E293B) else Color(0xFF94A3B8), 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            onClick = { type = TransactionType.INCOME },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (type == TransactionType.INCOME) Color(0xFF2563EB) else Color(0xFFF1F5F9)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "수입", 
                                    color = if (type == TransactionType.INCOME) Color.White else Color(0xFF94A3B8), 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Amount Field
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("금액") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedLabelColor = Color(0xFF2563EB)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Store Name Field
                    OutlinedTextField(
                        value = store,
                        onValueChange = { store = it },
                        label = { Text("상점명") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedLabelColor = Color(0xFF2563EB)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Category Selection Field
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("카테고리") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedLabelColor = Color(0xFF2563EB)
                            )
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            state.categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        category = cat.name
                                        expanded = false
                                    }
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF2563EB))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("새 카테고리 추가", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    showAddCategoryDialog = true
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Bottom Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Text(
                                "취소", 
                                color = Color(0xFF64748B), 
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Button(
                            onClick = { onConfirm(amount.toLongOrNull() ?: 0L, store, category, type) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                "추가", 
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    )

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { 
                onAddCategory(it)
                category = it
            }
        )
    }
}

@Composable
fun AddCategoryDialog(
    category: Category? = null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "카테고리 추가" else "카테고리 수정") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("카테고리 이름") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }) {
                Text(if (category == null) "추가" else "저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
