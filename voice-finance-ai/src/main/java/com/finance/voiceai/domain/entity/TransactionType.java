package com.finance.voiceai.domain.entity;

public enum TransactionType {
    EXPENSE("Despesa"),
    INCOME("Receita");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
