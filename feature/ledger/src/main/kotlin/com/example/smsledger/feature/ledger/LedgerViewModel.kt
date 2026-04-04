package com.example.smsledger.feature.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smsledger.domain.model.Category
import com.example.smsledger.domain.model.ParsingRule
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Serializable
data class AiTransactionResult(
    val storeName: String,
    val amount: Long,
    val category: String,
    val type: String
)

@Serializable
data class OcrResult(
    val text: String
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
    val geminiApiKey: String = ""
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
    private val saveGeminiApiKeyUseCase: SaveGeminiApiKeyUseCase
) : ViewModel() {

    private var currentApiKey: String = ""

    private fun getGenerativeModel(): GenerativeModel {
        val apiKey = currentApiKey.ifBlank {
            System.getProperty("GEMINI_API_KEY") ?: System.getenv("GEMINI_API_KEY") ?: ""
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
    }

    private fun observeApiKey() {
        viewModelScope.launch {
            getGeminiApiKeyUseCase().collect { key ->
                currentApiKey = key
                _state.update { it.copy(geminiApiKey = key) }
            }
        }
    }

    fun handleIntent(intent: LedgerIntent) {
        when (intent) {
            LedgerIntent.Load -> {
                observeTransactions()
                observeParsingRules()
                observeCategories()
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
        }
    }

    fun processImage(context: Context, uri: Uri, isOcr: Boolean, onResult: (AiTransactionResult?) -> Unit) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                processBitmap(bitmap, isOcr, onResult)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    fun processBitmap(bitmap: Bitmap, isOcr: Boolean, onResult: (AiTransactionResult?) -> Unit) {
        viewModelScope.launch {
            try {
                if (isOcr) {
                    val text = recognizeTextWithMlKit(bitmap)
                    onResult(AiTransactionResult(storeName = text ?: "", amount = 0, category = "기타", type = "expense"))
                    return@launch
                }

                val prompt = "이 영수증 또는 결제 내역 이미지에서 정보를 추출해줘. JSON 형식으로 응답해: {\"storeName\": \"상점명\", \"amount\": 12345, \"category\": \"식비/카페/교통/쇼핑/생활/기타 중 하나\", \"type\": \"income 또는 expense\"}"

                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }

                val response = getGenerativeModel().generateContent(inputContent)
                val responseText = response.text ?: ""
                
                val result = Json.decodeFromString<AiTransactionResult>(responseText)
                onResult(result)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    private suspend fun recognizeTextWithMlKit(bitmap: Bitmap): String? = suspendCoroutine { continuation ->
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
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
