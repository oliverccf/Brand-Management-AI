# 🤖 Social Media Brand Analyzer

**Português** | [English](README_EN.md)

[![Status](https://img.shields.io/badge/status-ativo-success.svg)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M6-blue.svg)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](#)

O **Social Media Brand Analyzer** é um sistema avançado de monitoramento e análise de marca em tempo real. Utiliza Inteligência Artificial (Spring AI) e arquitetura orientada a eventos (Kafka) para processar interações sociais, identificar sentimentos e sugerir ações estratégicas com isolamento multi-tenancy e contexto dinâmico de RAG.

---

## 🚀 Funcionalidades Principais

- **🧠 Análise Inteligente de Mensagens**: Extração automática de sentimento, categoria, palavras-chave e resumo utilizando LLMs via Spring AI.
- **🛡️ Confiança & Segurança (Trust Context)**: Identifica automaticamente usuários não verificados e sugere ações de segurança, como vinculação de conta para acesso ao histórico.
- **📚 RAG Dinâmico com Isolamento**: Recuperação de documentos baseada em `Knowledge Ingestion`, filtrada dinamicamente por `customer_id` e `brand_id`.
- **⚡ Processamento Real-time**: Arquitetura orientada a eventos utilizando Kafka para consumo massivo de mensagens.
- **🏢 Multi-tenancy Nativo**: Isolamento completo de dados e instruções por Marca (Brand), garantindo privacidade e personalização.
- **🧪 Testes BDD Estáveis**: Suíte completa de testes de comportamento (Cucumber) cobrindo simulações de canais, resolução de identidade e contexto de RAG.

---

## 🏗️ Arquitetura

O sistema segue uma arquitetura moderna de microsserviços orientada a IA:

1.  **Ingestão**: Mensagens chegam via Kafka ou API REST.
2.  **Enriquecimento**: O `IdentityService` resolve a identidade e o nível de confiança do usuário.
3.  **Contexto (RAG)**: O `VectorStore` (PGVector) é consultado com filtros dinâmicos para prover contexto relevante à IA.
4.  **Processamento AI**: O `BrandAnalyzerService` orquestra o prompt, integrando instruções da marca e o Trust Context.
5.  **Resultado**: Análises são persistidas e podem ser propagadas para outros tópicos Kafka.

---

## 🛠️ Stack Tecnológica

- **Backend**: Java 25, Spring Boot 4.0.2
- **IA**: Spring AI (Ollama/Llama 3)
- **Mensageria**: Apache Kafka
- **Persistência**: Spring Data JPA (PostgreSQL)
- **Busca Vetorial**: PGVector / Spring AI Vector Stores
- **Testes**: Cucumber (BDD), Testcontainers, Mockito, Awaitility
- **Produtividade**: Lombok, MapStruct

---

## 🚦 Como Iniciar

### Pré-requisitos
- **Java 25** instalado.
- **Docker** e Docker Compose (para serviços de infraestrutura e Testcontainers).

### Instalação e Build
1. Clone o repositório.
2. Compile o projeto:
   ```bash
   ./gradlew build
   ```

### Executando Testes
Para garantir que tudo está funcionando corretamente:
```bash
./gradlew test
```

---

## 📖 Exemplo de Uso (BDD)

O sistema é validado através de cenários Cucumber como o abaixo:

```gherkin
Cenário: Sugestão de vinculação para usuários não verificados
  Dado que uma marca "Alpha" existe
  E o usuário "oliver_user" é "UNVERIFIED"
  Quando eu envio uma mensagem do "Instagram" para o usuário "oliver_user" com o conteúdo "Como vejo meu extrato?" para "Alpha"
  Então o resultado da análise deve conter a ação sugerida "Request account linkage for history access"
```

---

## 🗺️ Roadmap de Desenvolvimento

- [x] Integração Kafka + Spring AI.
- [x] Implementação de RAG com filtros dinâmicos.
- [x] Lógica de Trust Context para usuários não verificados.
- [x] Loop de resolução e mensagens públicas de encerramento.
- [x] Testes de simulação multi-canal.
- [ ] Dashboard de visualização de crises para gerentes.
- [ ] Integração com modelos GPT-4/Claude para análise premium.

---

## 📝 Documentos Adicionais

- [Diagrama de Fluxo Detalhado](DIAGRAMA_FLUXO.md)
- [Análise Completa da Integração](ANALISE_COMPLETA.md)
- [Estratégia de Marketing/LinkedIn](LINKEDIN_POST.md)

---
Desenvolvido por **Antigravity AI Team** | [Google DeepMind - Advanced Agentic Coding]
