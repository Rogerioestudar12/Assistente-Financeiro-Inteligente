package com.finance.voiceai.domain.dto;

import java.util.ArrayList;
import java.util.List;

public class ChatResponse {

    private String reply;
    private List<String> toolsCalled = new ArrayList<>();
    private FinancialSummaryDto updatedSummary;

    public ChatResponse() {
    }

    public ChatResponse(String reply, List<String> toolsCalled, FinancialSummaryDto updatedSummary) {
        this.reply = reply;
        this.toolsCalled = toolsCalled != null ? toolsCalled : new ArrayList<>();
        this.updatedSummary = updatedSummary;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<String> getToolsCalled() {
        return toolsCalled;
    }

    public void setToolsCalled(List<String> toolsCalled) {
        this.toolsCalled = toolsCalled;
    }

    public FinancialSummaryDto getUpdatedSummary() {
        return updatedSummary;
    }

    public void setUpdatedSummary(FinancialSummaryDto updatedSummary) {
        this.updatedSummary = updatedSummary;
    }
}
