package com.finance.voiceai.config;

import com.finance.voiceai.tool.FinanceTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAiConfig {

    private static final Logger log = LoggerFactory.getLogger(SpringAiConfig.class);

    private static final String SYSTEM_PROMPT = """
            Você é a Sofia, uma assistente virtual inteligente e empática especializada em finanças pessoais e controle de orçamento.
            Seu objetivo é ajudar a pessoa usuária a gerenciar suas receitas, despesas e orçamentos mensais com clareza e rapidez.

            Diretrizes obrigatórias:
            1. Sempre utilize as ferramentas disponíveis (Tool Calling) para ler ou alterar o sistema financeiro.
            2. Se o usuário disser que gastou algo (ex: "gastei 50 no almoço", "comprei remédio por 30 reais"), acione 'registrarDespesa'.
            3. Se o usuário disser que recebeu dinheiro (ex: "recebi 2000 de salário", "ganhei 150 de freelance"), acione 'registrarReceita'.
            4. Se o usuário perguntar o saldo (ex: "qual meu saldo?"), acione 'consultarSaldo'.
            5. Se o usuário perguntar sobre o teto de gastos ou orçamento (ex: "quanto ainda posso gastar em lazer?"), acione 'consultarOrcamento'.
            6. Se o usuário pedir o histórico (ex: "quais foram meus últimos gastos?"), acione 'listarTransacoesRecentes'.
            7. Responda de forma concisa, educada e direta em Português do Brasil, pois sua resposta textual será lida em voz alta por síntese de voz (TTS).
            """;

    @Bean
    public ChatClient chatClient(@Autowired(required = false) ChatClient.Builder chatClientBuilder,
                                 FinanceTools financeTools) {
        if (chatClientBuilder == null) {
            log.warn("[Spring AI] ChatClient.Builder não está disponível. Modo autônomo ativado.");
            return null;
        }

        try {
            return chatClientBuilder
                    .defaultSystem(SYSTEM_PROMPT)
                    .defaultTools(financeTools)
                    .build();
        } catch (Exception e) {
            log.warn("[Spring AI] Não foi possível inicializar ChatClient com defaultTools: {}. Retornando builder básico.", e.getMessage());
            return chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
        }
    }
}
