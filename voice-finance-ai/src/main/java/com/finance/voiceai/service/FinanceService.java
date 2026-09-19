package com.finance.voiceai.service;

import com.finance.voiceai.domain.dto.CategoryBudgetStatus;
import com.finance.voiceai.domain.dto.FinancialSummaryDto;
import com.finance.voiceai.domain.dto.TransactionRequest;
import com.finance.voiceai.domain.dto.TransactionResponse;
import com.finance.voiceai.domain.entity.Category;
import com.finance.voiceai.domain.entity.Transaction;
import com.finance.voiceai.domain.entity.TransactionType;
import com.finance.voiceai.repository.CategoryRepository;
import com.finance.voiceai.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class FinanceService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public FinanceService(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Transaction registerExpense(String description, BigDecimal amount, String categoryName, String notes) {
        Category category = resolveCategory(categoryName, TransactionType.EXPENSE);
        Transaction transaction = new Transaction(
                description,
                amount,
                TransactionType.EXPENSE,
                category,
                LocalDateTime.now(),
                notes
        );
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction registerIncome(String description, BigDecimal amount, String categoryName, String notes) {
        Category category = resolveCategory(categoryName, TransactionType.INCOME);
        Transaction transaction = new Transaction(
                description,
                amount,
                TransactionType.INCOME,
                category,
                LocalDateTime.now(),
                notes
        );
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction createTransaction(TransactionRequest request) {
        if (request.getType() == TransactionType.EXPENSE) {
            return registerExpense(request.getDescription(), request.getAmount(), request.getCategoryName(), request.getNotes());
        } else {
            return registerIncome(request.getDescription(), request.getAmount(), request.getCategoryName(), request.getNotes());
        }
    }

    @Transactional(readOnly = true)
    public FinancialSummaryDto getSummary() {
        BigDecimal totalIncome = transactionRepository.sumAmountByType(TransactionType.INCOME);
        BigDecimal totalExpense = transactionRepository.sumAmountByType(TransactionType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        List<CategoryBudgetStatus> categoryBudgets = getAllCategoryBudgets();
        List<TransactionResponse> recent = getRecentTransactions(10);

        return new FinancialSummaryDto(totalIncome, totalExpense, netBalance, categoryBudgets, recent);
    }

    @Transactional(readOnly = true)
    public List<CategoryBudgetStatus> getAllCategoryBudgets() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryBudgetStatus> statuses = new ArrayList<>();

        for (Category cat : categories) {
            BigDecimal spent = transactionRepository.sumAmountByCategoryAndType(cat, TransactionType.EXPENSE);
            statuses.add(new CategoryBudgetStatus(
                    cat.getId(),
                    cat.getName(),
                    cat.getIcon(),
                    cat.getColor(),
                    cat.getMonthlyLimit(),
                    spent
            ));
        }
        return statuses;
    }

    @Transactional(readOnly = true)
    public Optional<CategoryBudgetStatus> getCategoryBudget(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return Optional.empty();
        }
        return categoryRepository.findByNameIgnoreCase(categoryName.trim())
                .map(cat -> {
                    BigDecimal spent = transactionRepository.sumAmountByCategoryAndType(cat, TransactionType.EXPENSE);
                    return new CategoryBudgetStatus(
                            cat.getId(),
                            cat.getName(),
                            cat.getIcon(),
                            cat.getColor(),
                            cat.getMonthlyLimit(),
                            spent
                    );
                });
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactions(int limit) {
        return transactionRepository.findTop20ByOrderByDateDesc().stream()
                .limit(limit > 0 ? limit : 10)
                .map(TransactionResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category updateCategoryLimit(String categoryName, BigDecimal newLimit) {
        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseGet(() -> new Category(categoryName, newLimit, "🏷️", "#6B7280"));
        category.setMonthlyLimit(newLimit);
        return categoryRepository.save(category);
    }

    private Category resolveCategory(String categoryName, TransactionType type) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            categoryName = (type == TransactionType.INCOME) ? "Salário" : "Outros";
        }
        final String searchName = categoryName.trim();
        return categoryRepository.findByNameIgnoreCase(searchName)
                .orElseGet(() -> {
                    // Tenta encontrar por aproximação
                    for (Category c : categoryRepository.findAll()) {
                        if (searchName.toLowerCase(Locale.ROOT).contains(c.getName().toLowerCase(Locale.ROOT)) ||
                            c.getName().toLowerCase(Locale.ROOT).contains(searchName.toLowerCase(Locale.ROOT))) {
                            return c;
                        }
                    }
                    // Se não encontrar, cria uma nova categoria padrão
                    return categoryRepository.save(new Category(searchName, BigDecimal.valueOf(500.0), "📁", "#3B82F6"));
                });
    }
}
