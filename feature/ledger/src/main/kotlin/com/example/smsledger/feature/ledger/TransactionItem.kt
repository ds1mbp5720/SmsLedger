package com.example.smsledger.feature.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smsledger.domain.model.Category
import com.example.smsledger.domain.model.Transaction
import com.example.smsledger.domain.model.TransactionType

import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    transaction: Transaction, 
    categories: List<Category>,
    onCategoryChange: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddCategory: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Info (Web Design 1:1 Match)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Category Chip (Web Style)
                Box {
                    Surface(
                        onClick = { expanded = true },
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        val truncatedCategory = if (transaction.category.length > 6) transaction.category.substring(0, 6) + ".." else transaction.category
                        Text(
                            truncatedCategory,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFF2563EB)
                        )
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name, fontSize = 16.sp) },
                                onClick = {
                                    onCategoryChange(category.name)
                                    expanded = false
                                }
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF2563EB))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("추가", fontSize = 16.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                expanded = false
                                onAddCategory()
                            }
                        )
                    }
                }
                
                // Store Name
                Text(
                    transaction.storeName,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E293B)
                )
                
                // Date
                Text(
                    transaction.date.toFormattedDate(),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    color = Color(0xFF94A3B8)
                )
            }
            
            // Right Side: Amount, Edit & Delete (Web Design 1:1 Match)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${transaction.amount.toKoreanCurrency()}",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black),
                    color = if (transaction.type == TransactionType.INCOME) Color(0xFF2563EB) else Color(0xFFEF4444)
                )
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "수정", tint = Color(0xFFE2E8F0), modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color(0xFFE2E8F0), modifier = Modifier.size(16.dp))
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF8FAFC), thickness = 1.dp)
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionItemPreview() {
    MaterialTheme {
        TransactionItem(
            transaction = Transaction(
                amount = 12500,
                storeName = "배달의민족",
                category = "식비",
                type = TransactionType.EXPENSE
            ),
            categories = listOf(Category(name = "식비"), Category(name = "교통비")),
            onCategoryChange = {},
            onEdit = {},
            onDelete = {},
            onAddCategory = {}
        )
    }
}
