# 🤖 Social Media Brand Analyzer

[Português](README.md) | **English**

[![Status](https://img.shields.io/badge/status-active-success.svg)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M6-blue.svg)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](#)

**Social Media Brand Analyzer** is an advanced real-time brand monitoring and analysis system. It leverages Artificial Intelligence (Spring AI) and event-driven architecture (Kafka) to process social interactions, identify sentiment, and suggest strategic actions with robust multi-tenancy isolation and dynamic RAG (Retrieval-Augmented Generation) context.

---

## 🚀 Key Features

- **🧠 Intelligent Message Analysis**: Automatic extraction of sentiment, category, keywords, and summary using LLMs via Spring AI.
- **🛡️ Trust & Security (Trust Context)**: Automatically identifies unverified users and suggests security actions, such as account linkage for history access, protecting sensitive data.
- **📚 Dynamic RAG with Isolation**: Knowledge retrieval based on metadata, dynamically filtered by `customer_id` and `brand_id` to ensure one brand's data never leaks to another.
- **⚡ Real-time Processing**: Event-driven architecture using Kafka for massive, low-latency message consumption.
- **🏢 Native Multi-tenancy**: Complete isolation of data, prompts, and instructions per Brand, allowing full customization of AI behavior.
- **🧪 Stable BDD Tests**: A comprehensive suite of behavior tests (Cucumber) covering multi-channel simulations, identity resolution, and RAG context.

---

## 🏗️ Architecture

The system follows a modern AI-driven microservices architecture:

1.  **Ingestion**: Messages arrive via Kafka topics or REST API endpoints.
2.  **Enrichment**: The `IdentityService` resolves the global user identity and determines the trust level.
3.  **Context (RAG)**: The `VectorStore` (PGVector) is queried using dynamic filters to provide the AI with brand-specific and user-specific historical context.
4.  **AI Processing**: The `BrandAnalyzerService` orchestrates the chat client, integrating custom brand instructions and the security-focused Trust Context.
5.  **Result**: Analysis results are persisted in a relational database and published back to Kafka for integration with CRMs or Dashboards.

---

## 🛠️ Technology Stack

- **Backend**: Java 25, Spring Boot 4.0.2
- **AI**: Spring AI (Ollama/Llama 3, OpenAI/Azure ready)
- **Messaging**: Apache Kafka
- **Persistence**: Spring Data JPA (PostgreSQL)
- **Vector Search**: PGVector / Spring AI Vector Stores
- **Testing**: Cucumber (BDD), Testcontainers, Mockito, Awaitility
- **Productivity**: Lombok, MapStruct

---

## 🚦 Getting Started

### Prerequisites
- **Java 25** installed.
- **Docker** and Docker Compose (for infrastructure services and Testcontainers).

### Build and Install
1. Clone the repository.
2. Build the project:
   ```bash
   ./gradlew build
   ```

### Running Tests
To ensure the entire flow is correct:
```bash
./gradlew test
```

---

## 📖 Example Usage (BDD)

The system is validated through Cucumber scenarios like the one below:

```gherkin
Scenario: Suggested account linkage for unverified users
  Given a brand "Alpha" exists
  And the user "oliver_user" is "UNVERIFIED"
  When I send a message from "Instagram" for user "oliver_user" with content "How do I see my statement?" to "Alpha"
  Then the analysis result should contain the suggested action "Request account linkage for history access"
```

---

## 🗺️ Development Roadmap

- [x] Kafka + Spring AI integration.
- [x] RAG implementation with dynamic filters.
- [x] Trust Context logic for unverified users.
- [x] Resolution loop and public closing messages.
- [x] Multi-channel simulation tests.
- [ ] Crisis visualization dashboard for managers.
- [ ] Integration with premium models (GPT-4/Claude).

---

## 📝 Additional Documentation

- [Detailed Flow Diagram](DIAGRAMA_FLUXO.md) (PT-BR)
- [Full Integration Analysis](ANALISE_COMPLETA.md) (PT-BR)
- [LinkedIn/Marketing Strategy](LINKEDIN_POST.md) (PT-BR)

---
Developed by **Antigravity AI Team** | [Google DeepMind - Advanced Agentic Coding]
