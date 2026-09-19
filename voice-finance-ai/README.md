# 🎙️ Sofia Voice Finance AI — API Inteligente com Spring Boot & Spring AI

API inteligente desenvolvida com **Java 21/25**, **Spring Boot 3** e **Spring AI**, capaz de interpretar comandos de voz em linguagem natural, converter áudio em texto (Speech-to-Text via Whisper), acionar regras de negócio do sistema financeiro utilizando **Tool Calling** no **ChatClient**, e gerar respostas em áudio sintetizado (Text-to-Speech).

---

## 🌟 Principais Funcionalidades

1. **Pipeline de Áudio Completa**:
   - **STT (Speech-to-Text)**: Transcrição de áudio via Whisper (`OpenAiAudioTranscriptionModel`). Suporta arquivos `.wav`, `.mp3`, `.webm`, `.ogg` e gravação direta do navegador.
   - **Raciocínio & Decisão**: ChatClient do Spring AI com prompt de persona financeira e raciocínio orientado a ferramentas.
   - **Tool Calling (@Tool)**: A IA decide e invoca autonomamente métodos Java para:
     - `registrarDespesa`: cadastra despesas e calcula impacto no teto da categoria.
     - `registrarReceita`: adiciona créditos ou salários e recalcula o saldo.
     - `consultarSaldo`: obtém o saldo consolidado, despesas e receitas.
     - `consultarOrcamento`: informa o consumo e margem restante de cada categoria de orçamento.
     - `listarTransacoesRecentes`: recupera o histórico mais recente.
     - `obterRelatorioGeral`: emite diagnósticos e avisos de categorias com risco de estouro.
   - **TTS (Text-to-Speech)**: Síntese de voz com voz natural via OpenAI TTS (`tts-1`).
2. **Arquitetura em Camadas Limpa**:
   - **Domain**: Entidades `Transaction`, `Category`, enums e DTOs tipados.
   - **Repository**: Spring Data JPA com queries de agregações financeiras.
   - **Service**: Regras de negócio de orçamento, limites percentuais e orquestração do assistente.
   - **Tools**: Métodos anotados com `@Tool` expostos ao `ChatClient`.
   - **Controllers**: Endpoints REST para comandos de voz, áudio em stream, chat em texto e operações de orçamento.
3. **Resiliência & Modo Autônomo**:
   - Funciona com sua chave da OpenAI (`OPENAI_API_KEY`) ou em modo de demonstração autônomo sem requerer faturamento prévio para testar.
4. **Interface Web Interativa**:
   - Frontend em `http://localhost:8080/` com gravação via microfone pelo navegador (MediaRecorder), player de áudio automático, cards de saldo em tempo real e barras de progresso do orçamento por categoria.

---

## 🏗️ Como Executar a Aplicação

### 1. Pré-requisitos
- JDK 21 ou JDK 25 instalado
- Terminal PowerShell ou Prompt de Comando

### 2. Configurando a Chave da OpenAI (Opcional)
Se desejar utilizar os modelos remotos de IA generativa (Whisper, GPT-4o-mini e TTS):
```powershell
$env:OPENAI_API_KEY = "sua-chave-openai-aqui"
```
> *Nota: Caso não configure nenhuma chave, o sistema opera automaticamente em modo de demonstração com motor semântico inteligente e geração de áudio WAV sintético.*

### 3. Compilando e Executando
No diretório do projeto:
```cmd
.\mvnw.bat spring-boot:run
```
Ou para rodar os testes:
```cmd
.\mvnw.bat test
```

### 4. Acessando o Dashboard Web
Abra o navegador em:
👉 **[http://localhost:8080/](http://localhost:8080/)**

Console do Banco H2:
👉 **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)**  
- JDBC URL: `jdbc:h2:mem:financedb`
- Usuário: `sa`
- Senha: *(vazio)*

---

## 📡 Guia de Endpoints da API

### 🎙️ Comandos de Voz

#### 1. Processamento Completo de Voz (STT + Tools + TTS + Resumo)
- **POST** `/api/voice/process`
- **Content-Type**: `multipart/form-data`
- **Parâmetro**: `audio` (Arquivo de áudio gravado ou carregado)
- **Retorno JSON**:
```json
{
  "transcription": "Gastei 55 reais no almoço hoje",
  "replyText": "Despesa 'Almoço' no valor de R$ 55,00 registrada com sucesso na categoria 'Alimentação'. Restam R$ 759,50 do seu limite.",
  "toolsCalled": [
    "registrarDespesa(descricao='Almoço', valor=55.0, categoria='Alimentação')"
  ],
  "audioBase64": "<string-base64-do-audio-mp3-ou-wav>",
  "audioContentType": "audio/mpeg",
  "updatedSummary": {
    "netBalance": 4096.60,
    "totalIncome": 4800.00,
    "totalExpense": 703.40
  }
}
```

#### 2. Stream Direto de Áudio (Audio-in -> Audio-out)
- **POST** `/api/voice/audio-stream`
- **Content-Type**: `multipart/form-data`
- **Retorno**: Arquivo binário de áudio (`audio/mpeg` ou `audio/wav`) que pode ser reproduzido diretamente.

---

### 💬 Comandos por Texto (Chat com Tools)

#### 3. Interação Textual
- **POST** `/api/chat`
- **Payload**:
```json
{
  "message": "Quanto ainda posso gastar em alimentação este mês?"
}
```
- **Retorno JSON**:
```json
{
  "reply": "Categoria Alimentação: Limite mensal de R$ 1.200,00. Já foram gastos R$ 385,50. Saldo restante disponível: R$ 814,50 (32.1% utilizado).",
  "toolsCalled": [
    "consultarOrcamento(categoria='Alimentação')"
  ],
  "updatedSummary": { ... }
}
```

---

### 📊 Gestão Financeira Tradicional (REST)

- `GET /api/finance/summary`: Retorna saldo consolidado, totais de receitas/despesas e orçamentos.
- `GET /api/finance/categories`: Retorna as categorias de gastos, limites e valores consumidos.
- `GET /api/finance/transactions`: Retorna o histórico das movimentações cadastradas.
- `POST /api/finance/transactions`: Registra manualmente uma despesa ou receita.
