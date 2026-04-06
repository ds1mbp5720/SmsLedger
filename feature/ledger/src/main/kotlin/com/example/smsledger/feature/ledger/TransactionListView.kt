package com.example.smsledger.feature.ledger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TransactionListView(
    state: LedgerState, 
    onSearch: (String) -> Unit,
    onUpdateCategory: (Transaction, String) -> Unit,
    onDelete: (Transaction) -> Unit,
    onEdit: (Transaction) -> Unit,
    onAddCategory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Summary Section (Web Design 1:1 Match)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFF6FF).copy(alpha = 0.3f))
                    .padding(20.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                ) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("수입", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                            Text("+${state.totalIncome.toKoreanCurrency()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF2563EB))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("지출", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                            Text("-${state.totalExpense.toKoreanCurrency()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
                        }
                        HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("합계", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text(
                                text = state.totalAmount.toKoreanCurrency(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (state.totalAmount >= 0) Color(0xFF2563EB) else Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Filter & Search Row (Web Preview Style)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Filter Dropdown
                var filterExpanded by remember { mutableStateOf(false) }
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { filterExpanded = true }
                    ) {
                        Text("전체 내역", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.transactions.size}건",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    DropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                        DropdownMenuItem(text = { Text("전체") }, onClick = { filterExpanded = false })
                        DropdownMenuItem(text = { Text("지출") }, onClick = { filterExpanded = false })
                        DropdownMenuItem(text = { Text("수입") }, onClick = { filterExpanded = false })
                    }
                }

                // Search Bar (Slim & Modern - Integrated)
                Surface(
                    modifier = Modifier.weight(1f).height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        BasicTextField(
                            value = state.searchQuery,
                            onValueChange = { onSearch(it) },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(color = Color(0xFF1E293B), fontSize = 11.sp, fontWeight = FontWeight.Medium),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (state.searchQuery.isEmpty()) {
                                        Text("검색", color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                    innerTextField()
                                }
                            },
                            singleLine = true
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(state.transactions) { transaction ->
            TransactionItem(
                transaction = transaction,
                categories = state.categories,
                onCategoryChange = { newCat -> 
                    onUpdateCategory(transaction, newCat)
                },
                onEdit = { onEdit(transaction) },
                onDelete = { onDelete(transaction) },
                onAddCategory = onAddCategory
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionListViewPreview() {
    MaterialTheme {
        TransactionListView(
            state = LedgerState(
                transactions = listOf(
                    Transaction(amount = 10000, storeName = "마트", category = "식비", type = TransactionType.EXPENSE),
                    Transaction(amount = 50000, storeName = "월급", category = "수입", type = TransactionType.INCOME)
                ),
                categories = listOf(Category(name = "식비"), Category(name = "수입"))
            ),
            onSearch = {},
            onUpdateCategory = { _, _ -> },
            onDelete = {},
            onEdit = {},
            onAddCategory = {}
        )
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
