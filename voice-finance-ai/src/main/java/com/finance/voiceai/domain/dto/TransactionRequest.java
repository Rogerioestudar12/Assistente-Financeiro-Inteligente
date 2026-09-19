package com.finance.voiceai.domain.dto;

import com.finance.voiceai.domain.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class TransactionRequest {

    @NotBlank(message = "A descrição é obrigatória")
    private String description;

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser positivo")
    private BigDecimal amount;

    @NotNull(message = "O tipo de transação é obrigatório (EXPENSE ou INCOME)")
    private TransactionType type;

    private String categoryName;

    private String notes;

    public TransactionRequest() {
    }

    public TransactionRequest(String description, BigDecimal amount, TransactionType type, String categoryName, String notes) {
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.categoryName = categoryName;
        this.notes = notes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
