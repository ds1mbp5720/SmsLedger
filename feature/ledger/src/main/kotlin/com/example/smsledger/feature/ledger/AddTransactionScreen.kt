package com.example.smsledger.feature.ledger

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import android.widget.Toast
import com.example.smsledger.domain.model.TransactionType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
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
    var isAiLoading by remember { mutableStateOf(false) }
    var isOcrMode by remember { mutableStateOf(false) }
    var ocrResultText by remember { mutableStateOf("") }
    var showOcrPreview by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    // Update category if it was changed externally (e.g. added via bottom sheet)
    LaunchedEffect(state.categories) {
        if (category !in state.categories.map { it.name } && state.categories.isNotEmpty()) {
            category = state.categories.last().name
        }
    }

    // Image/Camera Launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            isAiLoading = true
            ledgerViewModel.processImage(context, it, isOcr = isOcrMode) { result ->
                isAiLoading = false
                result?.let { res ->
                    if (isOcrMode) {
                        ocrResultText = res.storeName // OCR result is stored in storeName in ViewModel
                        showOcrPreview = true
                    } else {
                        amount = res.amount.toString()
                        store = res.storeName
                        category = res.category
                        type = if (res.type == "income") TransactionType.INCOME else TransactionType.EXPENSE
                    }
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            isAiLoading = true
            ledgerViewModel.processBitmap(it, isOcr = isOcrMode) { result ->
                isAiLoading = false
                result?.let { res ->
                    if (isOcrMode) {
                        ocrResultText = res.storeName
                        showOcrPreview = true
                    } else {
                        amount = res.amount.toString()
                        store = res.storeName
                        category = res.category
                        type = if (res.type == "income") TransactionType.INCOME else TransactionType.EXPENSE
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleCameraAction() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                cameraLauncher.launch(null)
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Full Screen Overlay (Web Preview Style)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Content Panel (Web Preview Style: rounded-t-[32px])
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) { } // Prevent click through
                .graphicsLayer {
                    translationY = 0f
                },
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                // Bottom Sheet Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Header with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "내역 추가", 
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
                
                // AI Buttons (Web Preview Style: Vertical)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Smart Recognition (Camera)
                    Surface(
                        onClick = { 
                            isOcrMode = false
                            handleCameraAction() 
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(2.dp, Color(0xFFDBEAFE))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                shadowElevation = 2.dp
                            ) {
                                Box(modifier = Modifier.padding(8.dp)) {
                                    if (isAiLoading && !isOcrMode) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF2563EB))
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                            Text("스마트 인식", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    
                    // Text Extraction (Camera)
                    Surface(
                        onClick = { 
                            isOcrMode = true
                            handleCameraAction() 
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(2.dp, Color(0xFFFEF3C7))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                shadowElevation = 2.dp
                            ) {
                                Box(modifier = Modifier.padding(8.dp)) {
                                    if (isAiLoading && isOcrMode) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFFD97706))
                                    } else {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                            Text("카메라 OCR", color = Color(0xFFD97706), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    // Text Extraction (Album)
                    Surface(
                        onClick = { 
                            isOcrMode = true
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(2.dp, Color(0xFFDCFCE7))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                shadowElevation = 2.dp
                            ) {
                                Box(modifier = Modifier.padding(8.dp)) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                                }
                            }
                            Text("앨범 OCR", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Divider with "또는"
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color(0xFFF1F5F9))
                    Surface(color = Color.White, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text("또는", color = Color(0xFFCBD5E1), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Transaction Type Toggle (Web Preview Style: bg-gray-100 rounded-xl p-1)
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
                                Text(
                                    "지출", 
                                    color = if (type == TransactionType.EXPENSE) Color(0xFFEF4444) else Color(0xFF94A3B8), 
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
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
                                Text(
                                    "수입", 
                                    color = if (type == TransactionType.INCOME) Color(0xFF2563EB) else Color(0xFF94A3B8), 
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Store Name Field (Web Style: OutlinedTextField)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("상점명", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                    OutlinedTextField(
                        value = store,
                        onValueChange = { store = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B)),
                        placeholder = { Text("예: 스타벅스", color = Color(0xFFCBD5E1), fontSize = 18.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFF1F5F9)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Amount Field (Web Style: OutlinedTextField)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("금액", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("0", color = Color(0xFFCBD5E1), fontSize = 18.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFF1F5F9)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category Selection (Web Style: Chip Flow)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("카테고리", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.categories.forEach { cat ->
                            Surface(
                                onClick = { category = cat.name },
                                shape = RoundedCornerShape(100.dp),
                                color = if (category == cat.name) Color(0xFF2563EB) else Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    cat.name,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (category == cat.name) Color.White else Color(0xFF64748B)
                                )
                            }
                        }
                        Surface(
                            onClick = { showAddCategoryDialog = true },
                            shape = RoundedCornerShape(100.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                        ) {
                            Text(
                                "+ 추가",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Add Button (Web Style: Full Width)
                Button(
                    onClick = { onConfirm(amount.toLongOrNull() ?: 0L, store, category, type) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        "추가하기", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }

    // OCR Preview Bottom Sheet
    if (showOcrPreview) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { showOcrPreview = false },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) { }
                    .graphicsLayer { translationY = 0f },
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .navigationBarsPadding()
                ) {
                    // Handle
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
                            "텍스트 추출 결과",
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF1E293B)
                        )
                        IconButton(onClick = { showOcrPreview = false }) {
                            Icon(
                                Icons.Default.Add, 
                                contentDescription = "닫기", 
                                modifier = Modifier.size(24.dp).rotate(45f),
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "아래 텍스트를 길게 눌러 선택하고 복사할 수 있습니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            androidx.compose.foundation.text.selection.SelectionContainer {
                                Text(
                                    text = ocrResultText,
                                    style = TextStyle(
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        color = Color(0xFF334155)
                                    )
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { 
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.Context.CLIPBOARD_SERVICE as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("OCR Result", ocrResultText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("복사", color = Color(0xFF64748B), style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                        }

                        Button(
                            onClick = { showOcrPreview = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text("확인", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }

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
