package com.finance.voiceai.service;

import com.finance.voiceai.domain.dto.ChatResponse;
import com.finance.voiceai.domain.dto.FinancialSummaryDto;
import com.finance.voiceai.domain.dto.VoiceProcessResponse;
import com.finance.voiceai.tool.FinanceTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VoiceAssistantService {

    private static final Logger log = LoggerFactory.getLogger(VoiceAssistantService.class);

    @Value("${spring.ai.openai.api-key:demo-key}")
    private String apiKey;

    private final AudioTranscriptionService transcriptionService;
    private final TextToSpeechService speechService;
    private final FinanceService financeService;
    private final FinanceTools financeTools;
    private final ChatClient chatClient;

    public VoiceAssistantService(AudioTranscriptionService transcriptionService,
                                 TextToSpeechService speechService,
                                 FinanceService financeService,
                                 FinanceTools financeTools,
                                 @Autowired(required = false) ChatClient chatClient) {
        this.transcriptionService = transcriptionService;
        this.speechService = speechService;
        this.financeService = financeService;
        this.financeTools = financeTools;
        this.chatClient = chatClient;
    }

    /**
     * Pipeline completa: Áudio -> Transcrição (STT) -> Raciocínio com Tool Calling (ChatClient) -> Síntese de Voz (TTS)
     */
    public VoiceProcessResponse processVoiceCommand(byte[] audioBytes, String filename) {
        log.info("[Voice Pipeline] Iniciando processamento de comando de voz...");

        // 1. Transcrição (Speech to Text)
        String transcription = transcriptionService.transcribe(audioBytes, filename);

        // 2. Execução da IA e Acionamento de Ferramentas (Tool Calling)
        FinanceTools.clearInvokedTools();
        String replyText = executeAiChat(transcription);
        List<String> toolsCalled = FinanceTools.getInvokedTools();

        // 3. Síntese de Voz da Resposta (Text to Speech)
        byte[] speechAudio = speechService.synthesize(replyText);
        String audioBase64 = (speechAudio != null && speechAudio.length > 0)
                ? Base64.getEncoder().encodeToString(speechAudio)
                : null;

        // 4. Consolidação do Estado Financeiro Atualizado
        FinancialSummaryDto updatedSummary = financeService.getSummary();

        log.info("[Voice Pipeline] Processamento concluído. Ferramentas acionadas: {}", toolsCalled);
        return new VoiceProcessResponse(
                transcription,
                replyText,
                toolsCalled,
                audioBase64,
                speechService.getAudioContentType(),
                updatedSummary
        );
    }

    /**
     * Processamento de comando textual: Texto -> Raciocínio com Tool Calling -> Resposta Textual
     */
    public ChatResponse processTextCommand(String userMessage) {
        log.info("[Chat Pipeline] Recebido comando textual: \"{}\"", userMessage);

        FinanceTools.clearInvokedTools();
        String replyText = executeAiChat(userMessage);
        List<String> toolsCalled = FinanceTools.getInvokedTools();
        FinancialSummaryDto updatedSummary = financeService.getSummary();

        return new ChatResponse(replyText, toolsCalled, updatedSummary);
    }

    /**
     * Pipeline de voz retornando diretamente o stream binário de áudio
     */
    public byte[] processVoiceToAudioStream(byte[] audioBytes, String filename) {
        VoiceProcessResponse response = processVoiceCommand(audioBytes, filename);
        return speechService.synthesize(response.getReplyText());
    }

    private String executeAiChat(String prompt) {
        if (!isDemoOrMissingKey() && chatClient != null) {
            try {
                log.info("[Spring AI ChatClient] Enviando prompt para o modelo com Tool Calling ativo: \"{}\"", prompt);
                String response = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();
                if (response != null && !response.trim().isEmpty()) {
                    return response.trim();
                }
            } catch (Exception e) {
                log.warn("[Spring AI ChatClient] Chamada à API remota falhou ({}). Ativando motor de Tool Calling autônomo.", e.getMessage());
            }
        }

        // Motor autônomo de interpretação semântica de intenções e execução direta de ferramentas (@Tool)
        return interpretAndExecuteTools(prompt);
    }

    /**
     * Motor inteligente de execução de ferramentas para operação contínua mesmo sem chave remota ativa.
     * Analisa a intenção semântica e invoca os mesmos métodos anotados com @Tool na classe FinanceTools.
     */
    private String interpretAndExecuteTools(String text) {
        String lower = text.toLowerCase(Locale.ROOT);

        // Intenção: Consulta de Saldo
        if (lower.contains("saldo") || lower.contains("quanto tenho") || lower.contains("extrato geral")) {
            return "Olá! " + financeTools.consultarSaldo();
        }

        // Intenção: Consulta de Orçamento / Limites
        if (lower.contains("orçamento") || lower.contains("limite") || lower.contains("quanto posso gastar") || lower.contains("quanto resta")) {
            String categoria = extractCategoryFromText(lower);
            return financeTools.consultarOrcamento(categoria);
        }

        // Intenção: Histórico de Transações
        if (lower.contains("últimas") || lower.contains("últimos") || lower.contains("histórico") || lower.contains("movimentações")) {
            return financeTools.listarTransacoesRecentes(5);
        }

        // Intenção: Relatório Geral
        if (lower.contains("relatório") || lower.contains("resumo geral") || lower.contains("panorama")) {
            return financeTools.obterRelatorioGeral();
        }

        // Intenção: Registro de Receita
        if (lower.contains("recebi") || lower.contains("ganhei") || lower.contains("salário") || lower.contains("depósito") || lower.contains("receita")) {
            double valor = extractAmount(lower);
            if (valor <= 0) valor = 1000.0;
            String descricao = extractDescription(text, "receita", "salário", "freelance");
            return financeTools.registrarReceita(descricao, valor, "Salário", "Registrado via assistente de voz");
        }

        // Intenção: Registro de Despesa (default para gastos)
        if (lower.contains("gastei") || lower.contains("comprei") || lower.contains("paguei") || lower.contains("despesa") || lower.contains("gasto")) {
            double valor = extractAmount(lower);
            if (valor <= 0) valor = 50.0;
            String categoria = extractCategoryFromText(lower);
            String descricao = extractDescription(text, "almoço", "mercado", "combustível", "lanche", "compra");
            return financeTools.registrarDespesa(descricao, valor, categoria, "Registrado via assistente de voz");
        }

        // Se houver valor explícito, trata como despesa
        double possibleAmount = extractAmount(lower);
        if (possibleAmount > 0) {
            String categoria = extractCategoryFromText(lower);
            return financeTools.registrarDespesa("Gasto informado", possibleAmount, categoria, "Registrado via voz");
        }

        return "Entendido. Você pode me pedir para registrar despesas, cadastrar receitas, consultar seu saldo ou verificar os limites do seu orçamento por categoria!";
    }

    private double extractAmount(String text) {
        // Regex para capturar padrões como "45 reais", "R$ 45,50", "120.00", "50"
        Pattern pattern = Pattern.compile("(\\d+([.,]\\d{1,2})?)\\s*(reais|r\\$)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                String num = matcher.group(1).replace(",", ".");
                return Double.parseDouble(num);
            } catch (Exception ignored) {}
        }
        return 0.0;
    }

    private String extractCategoryFromText(String text) {
        if (text.contains("alimenta") || text.contains("almoço") || text.contains("jantar") || text.contains("lanche") || text.contains("mercado") || text.contains("padaria") || text.contains("comida")) {
            return "Alimentação";
        }
        if (text.contains("transporte") || text.contains("combustível") || text.contains("gasolina") || text.contains("uber") || text.contains("ônibus") || text.contains("posto")) {
            return "Transporte";
        }
        if (text.contains("lazer") || text.contains("cinema") || text.contains("jogo") || text.contains("passeio") || text.contains("viagem") || text.contains("bar")) {
            return "Lazer";
        }
        if (text.contains("moradia") || text.contains("aluguel") || text.contains("condomínio") || text.contains("luz") || text.contains("água") || text.contains("internet")) {
            return "Moradia";
        }
        if (text.contains("saúde") || text.contains("remédio") || text.contains("farmácia") || text.contains("médico") || text.contains("consulta")) {
            return "Saúde";
        }
        return "Alimentação";
    }

    private String extractDescription(String text, String... hints) {
        for (String hint : hints) {
            if (text.toLowerCase().contains(hint.toLowerCase())) {
                return capitalize(hint);
            }
        }
        return "Movimentação Financeira";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private boolean isDemoOrMissingKey() {
        return apiKey == null || apiKey.trim().isEmpty() || "demo-key".equalsIgnoreCase(apiKey) || apiKey.startsWith("demo");
    }
}
