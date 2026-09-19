package com.finance.voiceai.domain.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CategoryBudgetStatus {

    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private BigDecimal monthlyLimit;
    private BigDecimal currentSpent;
    private BigDecimal remaining;
    private double percentageUsed;
    private boolean overBudget;

    public CategoryBudgetStatus() {
    }

    public CategoryBudgetStatus(Long categoryId, String categoryName, String categoryIcon, String categoryColor,
                                BigDecimal monthlyLimit, BigDecimal currentSpent) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon != null ? categoryIcon : "📁";
        this.categoryColor = categoryColor != null ? categoryColor : "#3B82F6";
        this.monthlyLimit = monthlyLimit != null ? monthlyLimit : BigDecimal.ZERO;
        this.currentSpent = currentSpent != null ? currentSpent : BigDecimal.ZERO;

        if (this.monthlyLimit.compareTo(BigDecimal.ZERO) > 0) {
            this.remaining = this.monthlyLimit.subtract(this.currentSpent);
            this.percentageUsed = this.currentSpent
                    .divide(this.monthlyLimit, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            this.overBudget = this.remaining.compareTo(BigDecimal.ZERO) < 0;
        } else {
            this.remaining = BigDecimal.ZERO;
            this.percentageUsed = 0.0;
            this.overBudget = false;
        }
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public BigDecimal getCurrentSpent() {
        return currentSpent;
    }

    public void setCurrentSpent(BigDecimal currentSpent) {
        this.currentSpent = currentSpent;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }

    public void setRemaining(BigDecimal remaining) {
        this.remaining = remaining;
    }

    public double getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(double percentageUsed) {
        this.percentageUsed = percentageUsed;
    }

    public boolean isOverBudget() {
        return overBudget;
    }

    public void setOverBudget(boolean overBudget) {
        this.overBudget = overBudget;
    }
}
