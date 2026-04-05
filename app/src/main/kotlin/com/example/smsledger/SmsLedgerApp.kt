package com.example.smsledger

import android.app.Application
import androidx.room.Room
import com.example.smsledger.data.local.AppDatabase
import com.example.smsledger.data.repository.CategoryRepositoryImpl
import com.example.smsledger.data.repository.ParsingRuleRepositoryImpl
import com.example.smsledger.data.repository.TransactionRepositoryImpl
import com.example.smsledger.domain.model.Category
import com.example.smsledger.domain.usecase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmsLedgerApp : Application() {
    
    lateinit var database: AppDatabase
    lateinit var repository: TransactionRepositoryImpl
    
    lateinit var getTransactionsUseCase: GetTransactionsUseCase
    lateinit var addTransactionUseCase: AddTransactionUseCase
    lateinit var updateTransactionUseCase: UpdateTransactionUseCase
    lateinit var deleteTransactionUseCase: DeleteTransactionUseCase

    lateinit var getParsingRulesUseCase: GetParsingRulesUseCase
    lateinit var addParsingRuleUseCase: AddParsingRuleUseCase
    lateinit var updateParsingRuleUseCase: UpdateParsingRuleUseCase
    lateinit var deleteParsingRuleUseCase: DeleteParsingRuleUseCase

    lateinit var getCategoriesUseCase: GetCategoriesUseCase
    lateinit var addCategoryUseCase: AddCategoryUseCase
    lateinit var updateCategoryUseCase: UpdateCategoryUseCase
    lateinit var deleteCategoryUseCase: DeleteCategoryUseCase
    
    lateinit var getGeminiApiKeyUseCase: GetGeminiApiKeyUseCase
    lateinit var saveGeminiApiKeyUseCase: SaveGeminiApiKeyUseCase
    lateinit var getUseSmartAiUseCase: GetUseSmartAiUseCase
    lateinit var setUseSmartAiUseCase: SetUseSmartAiUseCase

    lateinit var getRecurringTransactionsUseCase: GetRecurringTransactionsUseCase
    lateinit var addRecurringTransactionUseCase: AddRecurringTransactionUseCase
    lateinit var updateRecurringTransactionUseCase: UpdateRecurringTransactionUseCase
    lateinit var deleteRecurringTransactionUseCase: DeleteRecurringTransactionUseCase

    override fun onCreate() {
        super.onCreate()
        
        database = Room.databaseBuilder(this, AppDatabase::class.java, "sms_ledger.db").build()
        repository = TransactionRepositoryImpl(database.transactionDao())
        
        getTransactionsUseCase = GetTransactionsUseCase(repository)
        addTransactionUseCase = AddTransactionUseCase(repository)
        updateTransactionUseCase = UpdateTransactionUseCase(repository)
        deleteTransactionUseCase = DeleteTransactionUseCase(repository)

        val parsingRuleRepo = ParsingRuleRepositoryImpl(database.parsingRuleDao())
        getParsingRulesUseCase = GetParsingRulesUseCase(parsingRuleRepo)
        addParsingRuleUseCase = AddParsingRuleUseCase(parsingRuleRepo)
        updateParsingRuleUseCase = UpdateParsingRuleUseCase(parsingRuleRepo)
        deleteParsingRuleUseCase = DeleteParsingRuleUseCase(parsingRuleRepo)

        val categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        getCategoriesUseCase = GetCategoriesUseCase(categoryRepository)
        addCategoryUseCase = AddCategoryUseCase(categoryRepository)
        updateCategoryUseCase = UpdateCategoryUseCase(categoryRepository)
        deleteCategoryUseCase = DeleteCategoryUseCase(categoryRepository)

        val settingsRepository = com.example.smsledger.data.repository.SettingsRepositoryImpl(this)
        getGeminiApiKeyUseCase = GetGeminiApiKeyUseCase(settingsRepository)
        saveGeminiApiKeyUseCase = SaveGeminiApiKeyUseCase(settingsRepository)
        getUseSmartAiUseCase = GetUseSmartAiUseCase(settingsRepository)
        setUseSmartAiUseCase = SetUseSmartAiUseCase(settingsRepository)

        val recurringRepo = com.example.smsledger.data.repository.RecurringTransactionRepositoryImpl(database.recurringTransactionDao())
        getRecurringTransactionsUseCase = GetRecurringTransactionsUseCase(recurringRepo)
        addRecurringTransactionUseCase = AddRecurringTransactionUseCase(recurringRepo)
        updateRecurringTransactionUseCase = UpdateRecurringTransactionUseCase(recurringRepo)
        deleteRecurringTransactionUseCase = DeleteRecurringTransactionUseCase(recurringRepo)

        // Pre-populate default categories if empty
        CoroutineScope(Dispatchers.IO).launch {
            val currentCategories = getCategoriesUseCase().first()
            if (currentCategories.isEmpty()) {
                listOf("식비", "카페", "교통", "쇼핑", "생활", "기타").forEach {
                    addCategoryUseCase(Category(name = it))
                }
            }
        }
    }
}
