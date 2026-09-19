package com.finance.voiceai.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public class FinancialSummaryDto {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private List<CategoryBudgetStatus> categoryBudgets;
    private List<TransactionResponse> recentTransactions;

    public FinancialSummaryDto() {
    }

    public FinancialSummaryDto(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal netBalance,
                               List<CategoryBudgetStatus> categoryBudgets, List<TransactionResponse> recentTransactions) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netBalance = netBalance;
        this.categoryBudgets = categoryBudgets;
        this.recentTransactions = recentTransactions;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }

    public List<CategoryBudgetStatus> getCategoryBudgets() {
        return categoryBudgets;
    }

    public void setCategoryBudgets(List<CategoryBudgetStatus> categoryBudgets) {
        this.categoryBudgets = categoryBudgets;
    }

    public List<TransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionResponse> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }
}
