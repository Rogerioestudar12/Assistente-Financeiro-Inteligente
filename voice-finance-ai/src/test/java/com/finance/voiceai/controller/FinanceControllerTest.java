package com.finance.voiceai.controller;

import com.finance.voiceai.domain.dto.FinancialSummaryDto;
import com.finance.voiceai.domain.entity.Category;
import com.finance.voiceai.domain.entity.Transaction;
import com.finance.voiceai.domain.entity.TransactionType;
import com.finance.voiceai.service.FinanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FinanceController.class)
class FinanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinanceService financeService;

    @Test
    void shouldReturnSummary() throws Exception {
        FinancialSummaryDto summary = new FinancialSummaryDto(
                BigDecimal.valueOf(5000.0),
                BigDecimal.valueOf(2000.0),
                BigDecimal.valueOf(3000.0),
                List.of(),
                List.of()
        );
        when(financeService.getSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/finance/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(5000.0))
                .andExpect(jsonPath("$.totalExpense").value(2000.0))
                .andExpect(jsonPath("$.netBalance").value(3000.0));
    }

    @Test
    void shouldCreateTransaction() throws Exception {
        Category alimentacao = new Category("Alimentação", BigDecimal.valueOf(1000.0), "🍔", "#F59E0B");
        alimentacao.setId(1L);
        Transaction tx = new Transaction("Supermercado", BigDecimal.valueOf(120.0), TransactionType.EXPENSE, alimentacao, LocalDateTime.now(), "Compra");
        tx.setId(10L);

        when(financeService.createTransaction(any())).thenReturn(tx);

        String jsonPayload = """
                {
                    "description": "Supermercado",
                    "amount": 120.0,
                    "type": "EXPENSE",
                    "categoryName": "Alimentação",
                    "notes": "Compra"
                }
                """;

        mockMvc.perform(post("/api/finance/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.description").value("Supermercado"))
                .andExpect(jsonPath("$.amount").value(120.0));
    }
}
