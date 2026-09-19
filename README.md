# 🎙️ Sofia Voice Finance AI — Assistente Financeiro Inteligente

Projeto desenvolvido como solução do **Desafio Spring Boot & Spring AI** da [Digital Innovation One (DIO)](https://www.dio.me/).

## 📖 Sobre o Projeto
Uma API REST inteligente e multimodal capaz de:
1. Receber gravações de voz via microfone do navegador;
2. Transcrever áudio via Whisper (Speech-to-Text);
3. Interpretar a intenção financeira com Spring AI `ChatClient`;
4. Acionar métodos reais de negócio via **Tool Calling** (`@Tool`);
5. Recalcular orçamentos e emitir avisos de categorias críticas;
6. Sintetizar respostas em áudio natural (Text-to-Speech).

## 🚀 Melhoria Implementada
> Descreva aqui a melhoria que você escolheu (ex: nova tool de metas financeiras, validações de saldo ou testes adicionais).

## 🛠️ Tecnologias Utilizadas
- Java 21 / Spring Boot 3.3.x
- Spring AI (OpenAI ChatClient, Whisper STT, TTS)
- Spring Data JPA & Banco de Dados H2
- HTML5 Audio API & Tailwind CSS

## ⚙️ Como Executar
```bash
./mvnw spring-boot:run
