package com.example.smsledger.feature.ledger

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smsledger.domain.model.Category
import com.example.smsledger.domain.model.RecurringTransaction
import com.example.smsledger.domain.model.TransactionType

@Composable
fun AddRecurringTransactionScreen(
    recurring: RecurringTransaction?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (RecurringTransaction) -> Unit
) {
    var amount by remember { mutableStateOf(recurring?.amount?.toString() ?: "") }
    var storeName by remember { mutableStateOf(recurring?.storeName ?: "") }
    var category by remember { mutableStateOf(recurring?.category ?: "기타") }
    var type by remember { mutableStateOf(recurring?.type ?: TransactionType.EXPENSE) }
    var dayOfMonth by remember { mutableStateOf(recurring?.dayOfMonth?.toString() ?: "1") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { },
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (recurring == null) "고정 내역 추가" else "고정 내역 수정", 
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = "닫기", 
                            modifier = Modifier.size(24.dp).rotate(45f),
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    // Type Toggle
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            Surface(
                                onClick = { type = TransactionType.EXPENSE },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                shape = RoundedCornerShape(8.dp),
                                color = if (type == TransactionType.EXPENSE) Color.White else Color.Transparent,
                                shadowElevation = if (type == TransactionType.EXPENSE) 2.dp else 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("지출", color = if (type == TransactionType.EXPENSE) Color(0xFFEF4444) else Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Surface(
                                onClick = { type = TransactionType.INCOME },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                shape = RoundedCornerShape(8.dp),
                                color = if (type == TransactionType.INCOME) Color.White else Color.Transparent,
                                shadowElevation = if (type == TransactionType.INCOME) 2.dp else 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("수입", color = if (type == TransactionType.INCOME) Color(0xFF2563EB) else Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Amount
                    Text("금액", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B)),
                        placeholder = { Text("0", color = Color(0xFFCBD5E1), fontSize = 24.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFF1F5F9)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Store Name
                    Text("내역 이름", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B)),
                        placeholder = { Text("예: 월세, 적금, 통신비", color = Color(0xFFCBD5E1), fontSize = 18.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFF1F5F9)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Day of Month
                    Text("매월 일자", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                    OutlinedTextField(
                        value = dayOfMonth,
                        onValueChange = { if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 1..31)) dayOfMonth = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B)),
                        placeholder = { Text("1~31", color = Color(0xFFCBD5E1), fontSize = 18.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFF1F5F9)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category
                    Text("카테고리", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = category == cat.name
                            Surface(
                                onClick = { category = cat.name },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Text(
                                    cat.name, 
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = if (isSelected) Color.White else Color(0xFF64748B),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { 
                        val amountLong = amount.toLongOrNull() ?: 0L
                        val dayInt = dayOfMonth.toIntOrNull() ?: 1
                        onConfirm(RecurringTransaction(amount = amountLong, storeName = storeName, category = category, type = type, dayOfMonth = dayInt)) 
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        if (recurring == null) "추가하기" else "저장하기", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}
