package com.example.smsledger.feature.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import com.example.smsledger.domain.model.ParsingRule
import com.example.smsledger.domain.model.TransactionType

@Composable
fun AddParsingRuleScreen(
    rule: ParsingRule?,
    onDismiss: () -> Unit,
    onConfirm: (ParsingRule) -> Unit
) {
    var name by remember { mutableStateOf(rule?.name ?: "") }
    var senderNumber by remember { mutableStateOf(rule?.senderNumber ?: "") }
    var amountPattern by remember { mutableStateOf(rule?.amountPattern ?: "([0-9,]+)원") }
    var storePattern by remember { mutableStateOf(rule?.storePattern ?: "원\\s+(.+)") }
    var type by remember { mutableStateOf(rule?.type ?: TransactionType.EXPENSE) }

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
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
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
                        if (rule == null) "파싱 규칙 추가" else "파싱 규칙 수정", 
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

                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
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

                    // Fields
                    ParsingField(label = "규칙 이름", value = name, onValueChange = { name = it }, placeholder = "예: 신한은행")
                    Spacer(modifier = Modifier.height(16.dp))
                    ParsingField(label = "발신 번호", value = senderNumber, onValueChange = { senderNumber = it }, placeholder = "예: 1588-8100")
                    Spacer(modifier = Modifier.height(16.dp))
                    ParsingField(label = "금액 정규식", value = amountPattern, onValueChange = { amountPattern = it })
                    Spacer(modifier = Modifier.height(16.dp))
                    ParsingField(label = "상점명 정규식", value = storePattern, onValueChange = { storePattern = it })
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { 
                        onConfirm(ParsingRule(name = name, senderNumber = senderNumber.ifBlank { null }, amountPattern = amountPattern, storePattern = storePattern, type = type)) 
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        if (rule == null) "추가하기" else "저장하기", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ParsingField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String = "") {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B)),
            placeholder = { Text(placeholder, color = Color(0xFFCBD5E1), fontSize = 18.sp) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color(0xFFF1F5F9)
            )
        )
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
