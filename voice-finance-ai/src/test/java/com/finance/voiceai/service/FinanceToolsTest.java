package com.finance.voiceai.service;

import com.finance.voiceai.domain.dto.CategoryBudgetStatus;
import com.finance.voiceai.domain.dto.FinancialSummaryDto;
import com.finance.voiceai.domain.entity.Category;
import com.finance.voiceai.domain.entity.Transaction;
import com.finance.voiceai.domain.entity.TransactionType;
import com.finance.voiceai.tool.FinanceTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceToolsTest {

    @Mock
    private FinanceService financeService;

    @InjectMocks
    private FinanceTools financeTools;

    private Category categoryAlimentacao;

    @BeforeEach
    void setUp() {
        FinanceTools.clearInvokedTools();
        categoryAlimentacao = new Category("Alimentação", BigDecimal.valueOf(1000.0), "🍔", "#F59E0B");
        categoryAlimentacao.setId(1L);
    }

    @Test
    void testRegistrarDespesaTool() {
        Transaction tx = new Transaction("Almoço", BigDecimal.valueOf(50.0), TransactionType.EXPENSE, categoryAlimentacao, LocalDateTime.now(), "Nota");
        when(financeService.registerExpense(eq("Almoço"), eq(BigDecimal.valueOf(50.0)), eq("Alimentação"), any()))
                .thenReturn(tx);

        CategoryBudgetStatus budgetStatus = new CategoryBudgetStatus(1L, "Alimentação", "🍔", "#F59E0B", BigDecimal.valueOf(1000.0), BigDecimal.valueOf(50.0));
        when(financeService.getCategoryBudget("Alimentação")).thenReturn(Optional.of(budgetStatus));

        String result = financeTools.registrarDespesa("Almoço", 50.0, "Alimentação", "Sem notas");

        assertNotNull(result);
        assertTrue(result.contains("Almoço"));
        assertTrue(result.contains("registrada com sucesso"));
        assertEquals(1, FinanceTools.getInvokedTools().size());
        assertTrue(FinanceTools.getInvokedTools().get(0).contains("registrarDespesa"));
    }

    @Test
    void testConsultarSaldoTool() {
        FinancialSummaryDto summary = new FinancialSummaryDto(
                BigDecimal.valueOf(4000.0),
                BigDecimal.valueOf(1500.0),
                BigDecimal.valueOf(2500.0),
                List.of(),
                List.of()
        );
        when(financeService.getSummary()).thenReturn(summary);

        String result = financeTools.consultarSaldo();

        assertNotNull(result);
        assertTrue(result.contains("Saldo Líquido"));
        assertEquals(1, FinanceTools.getInvokedTools().size());
    }
}
