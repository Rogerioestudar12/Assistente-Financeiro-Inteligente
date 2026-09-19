package com.finance.voiceai.controller;

import com.finance.voiceai.domain.dto.VoiceProcessResponse;
import com.finance.voiceai.service.AudioTranscriptionService;
import com.finance.voiceai.service.TextToSpeechService;
import com.finance.voiceai.service.VoiceAssistantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/voice")
@CrossOrigin(origins = "*")
public class VoiceAssistantController {

    private static final Logger log = LoggerFactory.getLogger(VoiceAssistantController.class);

    private final VoiceAssistantService voiceAssistantService;
    private final AudioTranscriptionService transcriptionService;
    private final TextToSpeechService speechService;

    public VoiceAssistantController(VoiceAssistantService voiceAssistantService,
                                    AudioTranscriptionService transcriptionService,
                                    TextToSpeechService speechService) {
        this.voiceAssistantService = voiceAssistantService;
        this.transcriptionService = transcriptionService;
        this.speechService = speechService;
    }

    /**
     * Endpoint principal de comando de voz:
     * Recebe áudio (WAV, MP3, WebM, etc.), transcreve (STT), raciocina com a IA acionando ferramentas reais (@Tool),
     * sintetiza a resposta em áudio (TTS) e retorna JSON com transcrição, áudio base64 e resumo financeiro.
     */
    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceProcessResponse> processVoiceCommand(
            @RequestParam("audio") MultipartFile audioFile) {
        try {
            if (audioFile == null || audioFile.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            log.info("[Voice Controller] Recebido arquivo de áudio: {}, tamanho: {} bytes, tipo: {}",
                    audioFile.getOriginalFilename(), audioFile.getSize(), audioFile.getContentType());

            VoiceProcessResponse response = voiceAssistantService.processVoiceCommand(
                    audioFile.getBytes(),
                    audioFile.getOriginalFilename()
            );

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Erro ao ler dados do áudio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint direto de áudio-para-áudio:
     * Recebe áudio e devolve diretamente o arquivo de áudio sintetizado como stream binário.
     */
    @PostMapping(value = "/audio-stream", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> processVoiceStream(
            @RequestParam("audio") MultipartFile audioFile) {
        try {
            if (audioFile == null || audioFile.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            byte[] audioOutput = voiceAssistantService.processVoiceToAudioStream(
                    audioFile.getBytes(),
                    audioFile.getOriginalFilename()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(speechService.getAudioContentType()));
            headers.setContentLength(audioOutput.length);
            headers.set("Content-Disposition", "inline; filename=\"response-speech.mp3\"");

            return new ResponseEntity<>(audioOutput, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Erro ao processar stream de áudio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint isolado de Speech-to-Text (STT) para testes ou fluxos parciais.
     */
    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> transcribeOnly(
            @RequestParam("audio") MultipartFile audioFile) {
        try {
            if (audioFile == null || audioFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Arquivo de áudio vazio"));
            }
            String transcription = transcriptionService.transcribe(audioFile.getBytes(), audioFile.getOriginalFilename());
            return ResponseEntity.ok(Map.of("transcription", transcription));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint isolado de Text-to-Speech (TTS) para sintetizar texto em áudio.
     */
    @PostMapping(value = "/speak", produces = {"audio/mpeg", "audio/wav"})
    public ResponseEntity<byte[]> speakOnly(@RequestBody Map<String, String> payload) {
        String text = payload.get("text");
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        byte[] audio = speechService.synthesize(text);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(speechService.getAudioContentType()));
        headers.setContentLength(audio.length);

        return new ResponseEntity<>(audio, headers, HttpStatus.OK);
    }
}
