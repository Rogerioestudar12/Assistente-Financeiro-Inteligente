package com.finance.voiceai.domain.dto;

import java.util.ArrayList;
import java.util.List;

public class VoiceProcessResponse {

    private String transcription;
    private String replyText;
    private List<String> toolsCalled = new ArrayList<>();
    private String audioBase64;
    private String audioContentType;
    private FinancialSummaryDto updatedSummary;

    public VoiceProcessResponse() {
    }

    public VoiceProcessResponse(String transcription, String replyText, List<String> toolsCalled,
                                String audioBase64, String audioContentType, FinancialSummaryDto updatedSummary) {
        this.transcription = transcription;
        this.replyText = replyText;
        this.toolsCalled = toolsCalled != null ? toolsCalled : new ArrayList<>();
        this.audioBase64 = audioBase64;
        this.audioContentType = audioContentType;
        this.updatedSummary = updatedSummary;
    }

    public String getTranscription() {
        return transcription;
    }

    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public List<String> getToolsCalled() {
        return toolsCalled;
    }

    public void setToolsCalled(List<String> toolsCalled) {
        this.toolsCalled = toolsCalled;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }

    public String getAudioContentType() {
        return audioContentType;
    }

    public void setAudioContentType(String audioContentType) {
        this.audioContentType = audioContentType;
    }

    public FinancialSummaryDto getUpdatedSummary() {
        return updatedSummary;
    }

    public void setUpdatedSummary(FinancialSummaryDto updatedSummary) {
        this.updatedSummary = updatedSummary;
    }
}
