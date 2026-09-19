package com.finance.voiceai.controller;

import com.finance.voiceai.domain.dto.CategoryBudgetStatus;
import com.finance.voiceai.domain.dto.FinancialSummaryDto;
import com.finance.voiceai.domain.dto.TransactionRequest;
import com.finance.voiceai.domain.dto.TransactionResponse;
import com.finance.voiceai.domain.entity.Category;
import com.finance.voiceai.domain.entity.Transaction;
import com.finance.voiceai.service.FinanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin(origins = "*")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/summary")
    public ResponseEntity<FinancialSummaryDto> getSummary() {
        return ResponseEntity.ok(financeService.getSummary());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryBudgetStatus>> getCategories() {
        return ResponseEntity.ok(financeService.getAllCategoryBudgets());
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        return ResponseEntity.ok(financeService.getRecentTransactions(limit));
    }

    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        Transaction tx = financeService.createTransaction(request);
        return new ResponseEntity<>(TransactionResponse.fromEntity(tx), HttpStatus.CREATED);
    }

    @PutMapping("/categories/{name}/limit")
    public ResponseEntity<Category> updateCategoryLimit(
            @PathVariable("name") String name,
            @RequestBody Map<String, BigDecimal> payload) {
        BigDecimal limit = payload.get("monthlyLimit");
        if (limit == null || limit.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().build();
        }
        Category updated = financeService.updateCategoryLimit(name, limit);
        return ResponseEntity.ok(updated);
    }
}
