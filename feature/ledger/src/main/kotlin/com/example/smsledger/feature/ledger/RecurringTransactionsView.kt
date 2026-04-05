package com.example.smsledger.feature.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smsledger.domain.model.RecurringTransaction
import com.example.smsledger.domain.model.TransactionType
import java.text.NumberFormat
import java.util.*

@Composable
fun RecurringTransactionsView(
    state: LedgerState,
    onAddRecurring: () -> Unit,
    onEditRecurring: (RecurringTransaction) -> Unit,
    onDeleteRecurring: (RecurringTransaction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("고정 지출/수입 관리", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            IconButton(onClick = onAddRecurring) {
                Icon(Icons.Default.Add, contentDescription = "추가", tint = Color(0xFF2563EB))
            }
        }
        
        Text(
            "매월 지정된 날짜에 자동으로 내역이 추가됩니다.",
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (state.recurringTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("등록된 고정 내역이 없습니다.", color = Color(0xFF94A3B8))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.recurringTransactions) { recurring ->
                    RecurringItem(
                        recurring = recurring,
                        onEdit = { onEditRecurring(recurring) },
                        onDelete = { onDeleteRecurring(recurring) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecurringItem(
    recurring: RecurringTransaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val numberFormat = NumberFormat.getInstance(Locale.KOREA)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (recurring.type == TransactionType.EXPENSE) Color(0xFFFFE4E6) else Color(0xFFDBEAFE),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${recurring.dayOfMonth}일",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (recurring.type == TransactionType.EXPENSE) Color(0xFFE11D48) else Color(0xFF2563EB)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(recurring.storeName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Text(recurring.category, fontSize = 12.sp, color = Color(0xFF64748B))
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (recurring.type == TransactionType.EXPENSE) "-" else "+"}${numberFormat.format(recurring.amount)}원",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (recurring.type == TransactionType.EXPENSE) Color(0xFFEF4444) else Color(0xFF2563EB)
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF94A3B8))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}
