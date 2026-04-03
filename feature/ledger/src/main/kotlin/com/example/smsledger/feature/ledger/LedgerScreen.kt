package com.example.smsledger.feature.ledger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.layout.ExperimentalLayoutApi
import androidx.compose.layout.FlowRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        topBar = {
            Surface(
                shadowElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEFF6FF)) // blue-50
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color(0xFF1A1A1A),
                    ),
                    title = {
                        Column {
                            Text("SMS 가계부", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            // Month Selector in Title area or below
                            if (currentTab != 2) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { /* Could open a month picker */ }
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (state.selectedMonth == 0) viewModel.handleIntent(LedgerIntent.ChangeMonth(11, state.selectedYear - 1))
                                            else viewModel.handleIntent(LedgerIntent.ChangeMonth(state.selectedMonth - 1, state.selectedYear))
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color(0xFF2563EB))
                                    }
                                    Text(
                                        "${state.selectedYear}.${String.format("%02d", state.selectedMonth + 1)}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A1A1A),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = {
                                            if (state.selectedMonth == 11) viewModel.handleIntent(LedgerIntent.ChangeMonth(0, state.selectedYear + 1))
                                            else viewModel.handleIntent(LedgerIntent.ChangeMonth(state.selectedMonth + 1, state.selectedYear))
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF2563EB))
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { currentTab = 0 }) {
                            Icon(
                                Icons.Default.List, 
                                contentDescription = "목록", 
                                tint = if(currentTab == 0) Color(0xFF2563EB) else Color(0xFF94A3B8)
                            )
                        }
                        IconButton(onClick = { currentTab = 1 }) {
                            Icon(
                                Icons.Default.BarChart, 
                                contentDescription = "통계", 
                                tint = if(currentTab == 1) Color(0xFF2563EB) else Color(0xFF94A3B8)
                            )
                        }
                        IconButton(onClick = { currentTab = 2 }) {
                            Icon(
                                Icons.Default.Settings, 
                                contentDescription = "설정", 
                                tint = if(currentTab == 2) Color(0xFF2563EB) else Color(0xFF94A3B8)
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 0) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "추가")
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
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.KOREA)
    Column {
        // Search Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.handleIntent(LedgerIntent.Search(it)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("상점명 검색") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.handleIntent(LedgerIntent.Search("")) }) {
                        Icon(Icons.Default.Clear, contentDescription = "지우기")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)), // blue-50
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDBEAFE)) // blue-100
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("수입", style = MaterialTheme.typography.labelMedium, color = Color(0xFF64748B))
                    Text(numberFormat.format(state.totalIncome), color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("지출", style = MaterialTheme.typography.labelMedium, color = Color(0xFF64748B))
                    Text(numberFormat.format(state.totalExpense), color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFDBEAFE))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("합계", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Text(
                        text = numberFormat.format(state.totalAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (state.totalAmount >= 0) Color(0xFF2563EB) else Color(0xFFEF4444)
                    )
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
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
fun StatisticsView(state: LedgerState) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.KOREA)
    
    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("카테고리별 통계", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (state.monthlyStats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("데이터가 없습니다.", color = Color.Gray)
            }
        } else {
            state.monthlyStats.forEach { (category, amount) ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val truncatedCategory = if (category.length > 8) category.substring(0, 8) + ".." else category
                        Text(truncatedCategory, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                        Text(numberFormat.format(amount), fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    }
                    val progress = amount.toFloat() / state.totalAmount.coerceAtLeast(1).toFloat()
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp),
                        color = Color(0xFF2563EB),
                        trackColor = Color(0xFFEFF6FF)
                    )
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

    LazyColumn(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Category Management Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("카테고리 관리", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { 
                    editingCategory = null
                    showAddCategoryDialog = true 
                }) {
                    Icon(Icons.Default.AddCircle, contentDescription = "카테고리 추가", tint = Color(0xFF2563EB))
                }
            }
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.categories.forEach { category ->
                    InputChip(
                        selected = false,
                        onClick = {
                            editingCategory = category
                            showAddCategoryDialog = true
                        },
                        label = { 
                            val truncatedName = if (category.name.length > 4) category.name.substring(0, 4) + ".." else category.name
                            Text(truncatedName, color = Color(0xFF1A1A1A)) 
                        },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = Color.White,
                        ),
                        border = InputChipDefaults.inputChipBorder(
                            borderColor = Color(0xFFEFF6FF)
                        ),
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "삭제",
                                modifier = Modifier.size(16.dp).clickable {
                                    categoryToDelete = category
                                },
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // SMS Parsing Rules Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SMS 파싱 규칙", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { 
                    editingRule = null
                    showAddRuleDialog = true 
                }) {
                    Icon(Icons.Default.AddCircle, contentDescription = "규칙 추가", tint = Color(0xFF2563EB))
                }
            }
        }

        if (state.parsingRules.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                ) {
                    Text(
                        "등록된 파싱 규칙이 없습니다.\n우측 상단의 + 버튼을 눌러 추가하세요.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        } else {
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
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        item {
            Text("파싱 테스트", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        
        item {
            OutlinedTextField(
                value = testSender,
                onValueChange = { testSender = it },
                label = { Text("발신 번호 (선택사항)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("예: 1588-8100") }
            )
        }

        item {
            OutlinedTextField(
                value = testSms,
                onValueChange = { testSms = it },
                label = { Text("테스트 문자 내용") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }

        item {
            Button(
                onClick = { viewModel.handleIntent(LedgerIntent.ParseSms(testSms, testSender.ifBlank { null })) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("테스트 실행 (DB 저장됨)")
            }
        }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isActive) Color.White else Color(0xFFF8FAFC)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (rule.isActive) Color(0xFFEFF6FF) else Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(rule.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (rule.type == TransactionType.INCOME) Color(0xFFEFF6FF) else Color(0xFFFEF2F2),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = if (rule.type == TransactionType.INCOME) "수입" else "지출",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (rule.type == TransactionType.INCOME) Color(0xFF2563EB) else Color(0xFFEF4444)
                            )
                        }
                    }
                    if (!rule.senderNumber.isNullOrBlank()) {
                        Text("발신번호: ${rule.senderNumber}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                    }
                }
                Switch(
                    checked = rule.isActive, 
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("금액 패턴: ${rule.amountPattern}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
            Text("상점 패턴: ${rule.storePattern}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2563EB))) { Text("수정") }
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))) { Text("삭제") }
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
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.KOREA)
    var expanded by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(transaction.storeName, fontWeight = FontWeight.Bold) },
        supportingContent = { 
            Column {
                Text(dateFormat.format(Date(transaction.date)), color = Color(0xFF64748B))
                Box {
                    Surface(
                        onClick = { expanded = true },
                        color = Color(0xFFEFF6FF), // blue-50
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        val truncatedCategory = if (transaction.category.length > 4) transaction.category.substring(0, 4) + ".." else transaction.category
                        Text(
                            truncatedCategory, 
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
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
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${transaction.amount}원",
                    color = if (transaction.type == TransactionType.INCOME) Color(0xFF2563EB) else Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFF94A3B8)) }
            }
        }
    )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    state: LedgerState,
    onDismiss: () -> Unit, 
    onConfirm: (Long, String, String, TransactionType) -> Unit,
    onAddCategory: (String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var store by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(state.categories.firstOrNull()?.name ?: "기타") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var expanded by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("내역 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // AI/OCR Buttons Placeholder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* AI Smart Recognition logic */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = Color(0xFF2563EB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDBEAFE)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("스마트 인식", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { /* OCR logic */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFBEB), contentColor = Color(0xFFD97706)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEF3C7)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("텍스트 추출", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Type Toggle
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

                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("금액") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = store, onValueChange = { store = it }, label = { Text("상점명") }, modifier = Modifier.fillMaxWidth())
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        label = { Text("카테고리") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        state.categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    category = cat.name
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
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amount.toLongOrNull() ?: 0L, store, category, type) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("추가", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
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
