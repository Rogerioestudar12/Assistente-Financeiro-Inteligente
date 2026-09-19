package com.finance.voiceai.controller;

import com.finance.voiceai.domain.dto.ChatRequest;
import com.finance.voiceai.domain.dto.ChatResponse;
import com.finance.voiceai.service.VoiceAssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final VoiceAssistantService voiceAssistantService;

    public ChatController(VoiceAssistantService voiceAssistantService) {
        this.voiceAssistantService = voiceAssistantService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = voiceAssistantService.processTextCommand(request.getMessage());
        return ResponseEntity.ok(response);
    }
}
