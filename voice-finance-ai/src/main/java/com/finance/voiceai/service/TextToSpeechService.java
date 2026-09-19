package com.finance.voiceai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Service
public class TextToSpeechService {

    private static final Logger log = LoggerFactory.getLogger(TextToSpeechService.class);

    @Value("${spring.ai.openai.api-key:demo-key}")
    private String apiKey;

    private final OpenAiAudioSpeechModel speechModel;

    public TextToSpeechService(@Autowired(required = false) OpenAiAudioSpeechModel speechModel) {
        this.speechModel = speechModel;
    }

    public byte[] synthesize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new byte[0];
        }

        if (isDemoOrMissingKey() || speechModel == null) {
            log.info("[TTS Fallback/Demo] Gerando áudio de demonstração sonoro para o texto: \"{}\"", text);
            return generatePlayableWavTone();
        }

        try {
            log.info("[Spring AI TTS] Sintetizando voz via OpenAI TTS para texto de {} caracteres...", text.length());
            byte[] audioBytes = speechModel.call(text);
            log.info("[Spring AI TTS] Áudio sintetizado com sucesso ({} bytes)", audioBytes.length);
            return audioBytes;
        } catch (Exception e) {
            log.warn("[Spring AI TTS] Falha ao sintetizar áudio via API remota ({}). Gerando áudio sonoro de fallback.", e.getMessage());
            return generatePlayableWavTone();
        }
    }

    public String getAudioContentType() {
        if (isDemoOrMissingKey() || speechModel == null) {
            return "audio/wav";
        }
        return "audio/mpeg";
    }

    private boolean isDemoOrMissingKey() {
        return apiKey == null || apiKey.trim().isEmpty() || "demo-key".equalsIgnoreCase(apiKey) || apiKey.startsWith("demo");
    }

    /**
     * Gera um áudio WAV PCM 16-bit 44.1kHz sintético curto (dois tons musicais de confirmação agradáveis)
     * para que qualquer navegador web consiga reproduzir o som sem falhas mesmo em ambiente offline/sem chave.
     */
    private byte[] generatePlayableWavTone() {
        try {
            int sampleRate = 44100;
            double durationSeconds = 0.5;
            int totalSamples = (int) (sampleRate * durationSeconds);
            short[] samples = new short[totalSamples];

            for (int i = 0; i < totalSamples; i++) {
                double time = (double) i / sampleRate;
                // Dois tons harmônicos (523.25 Hz [C5] e 659.25 Hz [E5]) com decaimento suave de volume
                double freq = (time < 0.25) ? 523.25 : 659.25;
                double envelope = Math.max(0.0, 1.0 - (time / durationSeconds));
                double sinValue = Math.sin(2.0 * Math.PI * freq * time);
                samples[i] = (short) (sinValue * envelope * 20000);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            // RIFF header
            writeString(dos, "RIFF");
            dos.writeInt(Integer.reverseBytes(36 + totalSamples * 2));
            writeString(dos, "WAVE");

            // fmt subchunk
            writeString(dos, "fmt ");
            dos.writeInt(Integer.reverseBytes(16)); // tamanho do subchunk
            dos.writeShort(Short.reverseBytes((short) 1)); // PCM
            dos.writeShort(Short.reverseBytes((short) 1)); // 1 canal (mono)
            dos.writeInt(Integer.reverseBytes(sampleRate));
            dos.writeInt(Integer.reverseBytes(sampleRate * 2)); // byte rate (sampleRate * 1 * 16/8)
            dos.writeShort(Short.reverseBytes((short) 2)); // block align
            dos.writeShort(Short.reverseBytes((short) 16)); // bits por amostra

            // data subchunk
            writeString(dos, "data");
            dos.writeInt(Integer.reverseBytes(totalSamples * 2));
            for (short s : samples) {
                dos.writeShort(Short.reverseBytes(s));
            }

            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Erro ao gerar tom WAV", e);
            return new byte[0];
        }
    }

    private void writeString(DataOutputStream dos, String str) throws IOException {
        for (int i = 0; i < str.length(); i++) {
            dos.writeByte(str.charAt(i));
        }
    }
}
