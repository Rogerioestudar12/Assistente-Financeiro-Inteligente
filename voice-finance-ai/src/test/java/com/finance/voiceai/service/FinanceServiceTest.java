package com.finance.voiceai.service;

import com.finance.voiceai.domain.dto.CategoryBudgetStatus;
import com.finance.voiceai.domain.dto.FinancialSummaryDto;
import com.finance.voiceai.domain.entity.Category;
import com.finance.voiceai.domain.entity.Transaction;
import com.finance.voiceai.domain.entity.TransactionType;
import com.finance.voiceai.repository.CategoryRepository;
import com.finance.voiceai.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private FinanceService financeService;

    private Category alimentacao;

    @BeforeEach
    void setUp() {
        alimentacao = new Category("Alimentação", BigDecimal.valueOf(1000.0), "🍔", "#F59E0B");
        alimentacao.setId(1L);
    }

    @Test
    void shouldRegisterExpenseSuccessfully() {
        when(categoryRepository.findByNameIgnoreCase("Alimentação")).thenReturn(Optional.of(alimentacao));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction tx = financeService.registerExpense("Almoço executivo", BigDecimal.valueOf(45.0), "Alimentação", "Sem observações");

        assertNotNull(tx);
        assertEquals("Almoço executivo", tx.getDescription());
        assertEquals(BigDecimal.valueOf(45.0), tx.getAmount());
        assertEquals(TransactionType.EXPENSE, tx.getType());
        assertEquals("Alimentação", tx.getCategory().getName());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void shouldRegisterIncomeSuccessfully() {
        Category salario = new Category("Salário", BigDecimal.ZERO, "💵", "#059669");
        when(categoryRepository.findByNameIgnoreCase("Salário")).thenReturn(Optional.of(salario));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction tx = financeService.registerIncome("Freelance", BigDecimal.valueOf(1500.0), "Salário", "Projeto web");

        assertNotNull(tx);
        assertEquals("Freelance", tx.getDescription());
        assertEquals(BigDecimal.valueOf(1500.0), tx.getAmount());
        assertEquals(TransactionType.INCOME, tx.getType());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void shouldCalculateSummaryCorrectly() {
        when(transactionRepository.sumAmountByType(TransactionType.INCOME)).thenReturn(BigDecimal.valueOf(5000.0));
        when(transactionRepository.sumAmountByType(TransactionType.EXPENSE)).thenReturn(BigDecimal.valueOf(2000.0));
        when(categoryRepository.findAll()).thenReturn(List.of(alimentacao));
        when(transactionRepository.sumAmountByCategoryAndType(alimentacao, TransactionType.EXPENSE)).thenReturn(BigDecimal.valueOf(450.0));
        when(transactionRepository.findTop20ByOrderByDateDesc()).thenReturn(List.of());

        FinancialSummaryDto summary = financeService.getSummary();

        assertNotNull(summary);
        assertEquals(BigDecimal.valueOf(5000.0), summary.getTotalIncome());
        assertEquals(BigDecimal.valueOf(2000.0), summary.getTotalExpense());
        assertEquals(BigDecimal.valueOf(3000.0), summary.getNetBalance());
        assertEquals(1, summary.getCategoryBudgets().size());
        
        CategoryBudgetStatus status = summary.getCategoryBudgets().get(0);
        assertEquals(BigDecimal.valueOf(450.0), status.getCurrentSpent());
        assertEquals(BigDecimal.valueOf(550.0), status.getRemaining());
        assertEquals(45.0, status.getPercentageUsed());
        assertFalse(status.isOverBudget());
    }
}
