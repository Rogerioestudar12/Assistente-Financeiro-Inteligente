package com.finance.voiceai.domain.dto;

import com.finance.voiceai.domain.entity.Transaction;
import com.finance.voiceai.domain.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private String categoryName;
    private String categoryIcon;
    private LocalDateTime date;
    private String notes;

    public TransactionResponse() {
    }

    public static TransactionResponse fromEntity(Transaction t) {
        TransactionResponse dto = new TransactionResponse();
        dto.setId(t.getId());
        dto.setDescription(t.getDescription());
        dto.setAmount(t.getAmount());
        dto.setType(t.getType());
        if (t.getCategory() != null) {
            dto.setCategoryName(t.getCategory().getName());
            dto.setCategoryIcon(t.getCategory().getIcon());
        } else {
            dto.setCategoryName("Geral");
            dto.setCategoryIcon("💰");
        }
        dto.setDate(t.getDate());
        dto.setNotes(t.getNotes());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
