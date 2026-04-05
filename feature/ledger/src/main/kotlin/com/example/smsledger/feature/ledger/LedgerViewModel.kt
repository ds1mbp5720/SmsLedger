package com.example.smsledger.feature.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smsledger.domain.model.RecurringTransaction
import com.example.smsledger.domain.model.Transaction
import com.example.smsledger.domain.model.TransactionType
import com.example.smsledger.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.regex.Pattern
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import android.graphics.Bitmap
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Serializable
data class AiTransactionResult(
    val storeName: String = "",
    val amount: Long = 0,
    val category: String = "기타",
    val type: String = "expense",
    val allTextBlocks: List<String> = emptyList()
)

private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

@Serializable
data class RegexSuggestion(
    val amountPattern: String,
    val storePattern: String
)

@Serializable
data class OcrResult(
    val text: String,
    val allTextBlocks: List<String> = emptyList()
)

data class LedgerState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val totalAmount: Long = 0,
    val totalIncome: Long = 0,
    val totalExpense: Long = 0,
    val monthlyStats: Map<String, Long> = emptyMap(), // Category -> Amount for selected month
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val parsingRules: List<ParsingRule> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val geminiApiKey: String = "",
    val useSmartAi: Boolean = true,
    val recurringTransactions: List<RecurringTransaction> = emptyList()
)

sealed class LedgerIntent {
    object Load : LedgerIntent()
    data class Add(val amount: Long, val store: String, val category: String, val type: TransactionType = TransactionType.EXPENSE) : LedgerIntent()
    data class UpdateCategory(val transaction: Transaction, val newCategory: String) : LedgerIntent()
    data class Delete(val transaction: Transaction) : LedgerIntent()
    data class ParseSms(val body: String, val sender: String? = null) : LedgerIntent()
    data class ChangeMonth(val month: Int, val year: Int) : LedgerIntent()
    data class AddParsingRule(val rule: ParsingRule) : LedgerIntent()
    data class UpdateParsingRule(val rule: ParsingRule) : LedgerIntent()
    data class DeleteParsingRule(val rule: ParsingRule) : LedgerIntent()
    data class AddCategory(val name: String) : LedgerIntent()
    data class UpdateCategoryName(val category: Category, val newName: String) : LedgerIntent()
    data class DeleteCategory(val category: Category, val moveTransactions: Boolean = true) : LedgerIntent()
    data class Search(val query: String) : LedgerIntent()
    data class SaveApiKey(val key: String) : LedgerIntent()
    data class ToggleSmartAi(val use: Boolean) : LedgerIntent()
    data class AddRecurringTransaction(val recurring: RecurringTransaction) : LedgerIntent()
    data class UpdateRecurringTransaction(val recurring: RecurringTransaction) : LedgerIntent()
    data class DeleteRecurringTransaction(val recurring: RecurringTransaction) : LedgerIntent()
}

class LedgerViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getParsingRulesUseCase: GetParsingRulesUseCase,
    private val addParsingRuleUseCase: AddParsingRuleUseCase,
    private val updateParsingRuleUseCase: UpdateParsingRuleUseCase,
    private val deleteParsingRuleUseCase: DeleteParsingRuleUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val getGeminiApiKeyUseCase: GetGeminiApiKeyUseCase,
    private val saveGeminiApiKeyUseCase: SaveGeminiApiKeyUseCase,
    private val getUseSmartAiUseCase: GetUseSmartAiUseCase,
    private val setUseSmartAiUseCase: SetUseSmartAiUseCase,
    private val getRecurringTransactionsUseCase: GetRecurringTransactionsUseCase,
    private val addRecurringTransactionUseCase: AddRecurringTransactionUseCase,
    private val updateRecurringTransactionUseCase: UpdateRecurringTransactionUseCase,
    private val deleteRecurringTransactionUseCase: DeleteRecurringTransactionUseCase
) : ViewModel() {

    private var currentApiKey: String = ""

    private fun getGenerativeModel(): GenerativeModel {
        val apiKey = currentApiKey.ifBlank {
            System.getProperty("GEMINI_API_KEY") ?: System.getenv("GEMINI_API_KEY") ?: ""
        }
        
        if (apiKey.isBlank()) {
            throw IllegalStateException("API_KEY_MISSING")
        }

        return GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = apiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )
    }

    private val _state = MutableStateFlow(LedgerState())
    val state: StateFlow<LedgerState> = _state.asStateFlow()

    val categories = listOf("식비", "카페", "교통", "쇼핑", "생활", "기타")

    init {
        handleIntent(LedgerIntent.Load)
        observeApiKey()
        observeUseSmartAi()
    }

    private fun observeApiKey() {
        viewModelScope.launch {
            getGeminiApiKeyUseCase().collect { key ->
                currentApiKey = key
                _state.update { it.copy(geminiApiKey = key) }
            }
        }
    }

    private fun observeUseSmartAi() {
        viewModelScope.launch {
            getUseSmartAiUseCase().collect { use ->
                _state.update { it.copy(useSmartAi = use) }
            }
        }
    }

    fun handleIntent(intent: LedgerIntent) {
        when (intent) {
            LedgerIntent.Load -> {
                observeTransactions()
                observeParsingRules()
                observeCategories()
                observeRecurringTransactions()
            }
            is LedgerIntent.Add -> addManual(intent.amount, intent.store, intent.category, intent.type)
            is LedgerIntent.UpdateCategory -> updateCategory(intent.transaction, intent.newCategory)
            is LedgerIntent.Delete -> delete(intent.transaction)
            is LedgerIntent.ParseSms -> parseSms(intent.body, intent.sender)
            is LedgerIntent.ChangeMonth -> {
                _state.update { it.copy(selectedMonth = intent.month, selectedYear = intent.year) }
            }
            is LedgerIntent.AddParsingRule -> viewModelScope.launch { addParsingRuleUseCase(intent.rule) }
            is LedgerIntent.UpdateParsingRule -> viewModelScope.launch { updateParsingRuleUseCase(intent.rule) }
            is LedgerIntent.DeleteParsingRule -> viewModelScope.launch { deleteParsingRuleUseCase(intent.rule) }
            is LedgerIntent.AddCategory -> viewModelScope.launch { addCategoryUseCase(Category(name = intent.name)) }
            is LedgerIntent.UpdateCategoryName -> updateCategoryName(intent.category, intent.newName)
            is LedgerIntent.DeleteCategory -> deleteCategory(intent.category, intent.moveTransactions)
            is LedgerIntent.Search -> {
                _state.update { it.copy(searchQuery = intent.query) }
            }
            is LedgerIntent.SaveApiKey -> viewModelScope.launch { saveGeminiApiKeyUseCase(intent.key) }
            is LedgerIntent.ToggleSmartAi -> viewModelScope.launch { setUseSmartAiUseCase(intent.use) }
            is LedgerIntent.AddRecurringTransaction -> viewModelScope.launch { addRecurringTransactionUseCase(intent.recurring) }
            is LedgerIntent.UpdateRecurringTransaction -> viewModelScope.launch { updateRecurringTransactionUseCase(intent.recurring) }
            is LedgerIntent.DeleteRecurringTransaction -> viewModelScope.launch { deleteRecurringTransactionUseCase(intent.recurring) }
        }
    }

    fun processImage(context: Context, uri: Uri, isOcr: Boolean, onResult: (AiTransactionResult?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                processBitmap(bitmap, isOcr, onResult)
            } catch (e: Exception) {
                onResult(null, e.message)
            }
        }
    }

    fun processBitmap(bitmap: Bitmap, isOcr: Boolean, onResult: (AiTransactionResult?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val useSmartAi = _state.value.useSmartAi
                
                // If smart AI is OFF, always use ML Kit for OCR
                if (!useSmartAi) {
                    val visionText = recognizeTextWithMlKit(bitmap)
                    val textBlocks = visionText?.textBlocks?.map { it.text } ?: emptyList()
                    val fullText = visionText?.text ?: ""
                    onResult(AiTransactionResult(
                        storeName = fullText, 
                        amount = 0, 
                        category = "기타", 
                        type = "expense",
                        allTextBlocks = textBlocks
                    ), null)
                    return@launch
                }

                // If smart AI is ON, use Gemini
                val prompt = """
                    이 영수증 또는 결제 내역 이미지에서 정보를 추출해줘. 
                    반드시 단 하나의 JSON 객체만 응답해. 리스트([]) 형식이 아닌 객체({}) 형식이어야 해.
                    JSON 형식: 
                    {
                        "storeName": "가장 유력한 상점명", 
                        "amount": 12345, 
                        "category": "식비/카페/교통/쇼핑/생활/기타 중 하나", 
                        "type": "income 또는 expense",
                        "allTextBlocks": ["이미지에서 보이는 모든 개별 텍스트 조각들", "상점명", "금액", "날짜", "전화번호" 등]
                    }
                """.trimIndent()

                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }

                val response = getGenerativeModel().generateContent(inputContent)
                val responseText = response.text ?: ""
                
                // Clean markdown if present
                var cleanedJson = if (responseText.contains("```json")) {
                    responseText.substringAfter("```json").substringBeforeLast("```").trim()
                } else if (responseText.contains("```")) {
                    responseText.substringAfter("```").substringBeforeLast("```").trim()
                } else {
                    responseText.trim()
                }
                
                // Handle cases where AI returns a list instead of an object
                val result = try {
                    json.decodeFromString<AiTransactionResult>(cleanedJson)
                } catch (e: Exception) {
                    try {
                        // Try parsing as a list and take the first element
                        val list = json.decodeFromString<List<AiTransactionResult>>(cleanedJson)
                        list.firstOrNull() ?: throw e
                    } catch (e2: Exception) {
                        // If it's still failing, try to extract the first object if it's a list
                        if (cleanedJson.startsWith("[") && cleanedJson.endsWith("]")) {
                            val inner = cleanedJson.substring(1, cleanedJson.length - 1).trim()
                            json.decodeFromString<AiTransactionResult>(inner)
                        } else {
                            throw e
                        }
                    }
                }
                onResult(result, null)
            } catch (e: Exception) {
                android.util.Log.e("LedgerViewModel", "AI Error: ${e.message}", e)
                val errorType = when {
                    e.message?.contains("Quota exceeded") == true -> "QUOTA_EXCEEDED"
                    e.message?.contains("API_KEY_MISSING") == true -> "API_KEY_MISSING"
                    else -> "UNKNOWN_ERROR"
                }
                onResult(null, errorType)
            }
        }
    }

    fun generateRegexFromSample(sampleText: String, onResult: (RegexSuggestion?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val prompt = """
                    다음 SMS 문자 메시지 샘플에서 금액과 상점명을 추출하기 위한 정규표현식(Regex)을 생성해줘.
                    금액은 숫자를 캡처 그룹으로 포함해야 하고, 상점명은 해당 텍스트를 캡처 그룹으로 포함해야 해.
                    
                    문자 샘플:
                    "$sampleText"
                    
                    응답은 반드시 다음 JSON 형식으로만 해:
                    {
                        "amountPattern": "금액 추출용 정규식",
                        "storePattern": "상점명 추출용 정규식"
                    }
                """.trimIndent()

                val response = getGenerativeModel().generateContent(prompt)
                val responseText = response.text ?: ""
                
                val cleanedJson = if (responseText.contains("```json")) {
                    responseText.substringAfter("```json").substringBeforeLast("```").trim()
                } else if (responseText.contains("```")) {
                    responseText.substringAfter("```").substringBeforeLast("```").trim()
                } else {
                    responseText.trim()
                }
                
                val result = json.decodeFromString<RegexSuggestion>(cleanedJson)
                onResult(result, null)
            } catch (e: Exception) {
                android.util.Log.e("LedgerViewModel", "Regex AI Error: ${e.message}", e)
                val errorType = when {
                    e.message?.contains("Quota exceeded") == true -> "QUOTA_EXCEEDED"
                    e.message?.contains("API_KEY_MISSING") == true -> "API_KEY_MISSING"
                    else -> "UNKNOWN_ERROR"
                }
                onResult(null, errorType)
            }
        }
    }

    private suspend fun recognizeTextWithMlKit(bitmap: Bitmap): com.google.mlkit.vision.text.Text? = suspendCoroutine { continuation ->
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText)
                }
                .addOnFailureListener { e ->
                    continuation.resume(null)
                }
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }

    private fun updateCategoryName(category: Category, newName: String) {
        viewModelScope.launch {
            // Update the category itself
            updateCategoryUseCase(category.copy(name = newName))
            
            // Update all transactions that were using the old category name
            val transactions = getTransactionsUseCase().first()
            transactions.filter { it.category == category.name }.forEach { 
                updateTransactionUseCase(it.copy(category = newName))
            }
        }
    }

    private fun deleteCategory(category: Category, moveTransactions: Boolean) {
        viewModelScope.launch {
            // Delete the category from the list
            deleteCategoryUseCase(category)
            
            if (moveTransactions) {
                // Move all transactions using this category to "미분류" (Uncategorized)
                val transactions = getTransactionsUseCase().first()
                transactions.filter { it.category == category.name }.forEach { 
                    updateTransactionUseCase(it.copy(category = "미분류"))
                }
                
                // Ensure "미분류" category exists
                val currentCategories = getCategoriesUseCase().first()
                if (currentCategories.none { it.name == "미분류" }) {
                    addCategoryUseCase(Category(name = "미분류"))
                }
            }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { list ->
                _state.update { it.copy(categories = list) }
            }
        }
    }

    private fun observeRecurringTransactions() {
        viewModelScope.launch {
            getRecurringTransactionsUseCase().collect { list ->
                _state.update { it.copy(recurringTransactions = list) }
                processRecurringTransactions(list)
            }
        }
    }

    private fun processRecurringTransactions(recurring: List<RecurringTransaction>) {
        viewModelScope.launch {
            val allTransactions = getTransactionsUseCase().first()
            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            recurring.forEach { rec ->
                if (rec.dayOfMonth <= currentDay) {
                    val alreadyAdded = allTransactions.any { t ->
                        val tCal = Calendar.getInstance().apply { timeInMillis = t.date }
                        t.recurringId == rec.id && 
                        tCal.get(Calendar.MONTH) == currentMonth && 
                        tCal.get(Calendar.YEAR) == currentYear
                    }

                    if (!alreadyAdded) {
                        val transactionDate = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, rec.dayOfMonth)
                            set(Calendar.HOUR_OF_DAY, 9)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        addTransactionUseCase(Transaction(
                            amount = rec.amount,
                            storeName = rec.storeName,
                            category = rec.category,
                            type = rec.type,
                            date = transactionDate,
                            originalMessage = "고정 지출/수입 자동 추가",
                            recurringId = rec.id
                        ))
                    }
                }
            }
        }
    }

    private fun observeParsingRules() {
        viewModelScope.launch {
            getParsingRulesUseCase().collect { list ->
                _state.update { it.copy(parsingRules = list) }
            }
        }
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            combine(
                getTransactionsUseCase(),
                _state.map { it.selectedMonth }.distinctUntilChanged(),
                _state.map { it.selectedYear }.distinctUntilChanged(),
                _state.map { it.searchQuery }.distinctUntilChanged()
            ) { list, month, year, query ->
                val filteredList = list.filter { 
                    val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                    val matchesMonth = cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
                    val matchesSearch = it.storeName.contains(query, ignoreCase = true)
                    matchesMonth && matchesSearch
                }

                val income = filteredList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = filteredList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                val stats = filteredList.filter { it.type == TransactionType.EXPENSE }.groupBy { it.category }
                  .mapValues { entry -> entry.value.sumOf { it.amount } }
                  .toList()
                  .sortedByDescending { it.second }
                  .toMap()

                Triple(filteredList, income to expense, stats)
            }.collect { (filteredList, totals, stats) ->
                _state.update { it.copy(
                    transactions = filteredList,
                    totalAmount = totals.first - totals.second,
                    totalIncome = totals.first,
                    totalExpense = totals.second,
                    monthlyStats = stats
                ) }
            }
        }
    }

    private fun addManual(amount: Long, store: String, category: String, type: TransactionType) {
        viewModelScope.launch {
            addTransactionUseCase(Transaction(amount = amount, storeName = store, category = category, type = type))
        }
    }

    private fun updateCategory(transaction: Transaction, newCategory: String) {
        viewModelScope.launch {
            updateTransactionUseCase(transaction.copy(category = newCategory))
        }
    }

    private fun delete(transaction: Transaction) {
        viewModelScope.launch {
            deleteTransactionUseCase(transaction)
        }
    }

    private fun parseSms(body: String, sender: String?) {
        val rules = _state.value.parsingRules.filter { it.isActive }
        
        for (rule in rules) {
            // Check sender if specified
            val ruleSender = rule.senderNumber
            if (!ruleSender.isNullOrBlank() && sender != null) {
                if (!sender.contains(ruleSender)) continue
            }

            try {
                val amountPattern = Pattern.compile(rule.amountPattern)
                val storePattern = Pattern.compile(rule.storePattern)
                val amountMatcher = amountPattern.matcher(body)
                val storeMatcher = storePattern.matcher(body)

                if (amountMatcher.find()) {
                    val amount = amountMatcher.group(1).replace(",", "").toLongOrNull() ?: 0L
                    val store = if (storeMatcher.find()) storeMatcher.group(1).trim() else "알 수 없음"
                    viewModelScope.launch {
                        addTransactionUseCase(Transaction(amount = amount, storeName = store, originalMessage = body, type = rule.type))
                    }
                    return // Match found, stop processing other rules
                }
            } catch (e: Exception) {
                // Skip invalid regex
            }
        }
    }
}
