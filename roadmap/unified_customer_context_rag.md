# Análise Técnica e de Negócio: Contexto Unificado do Cliente com RAG

## 1. Visão Geral do Negócio (Business Value)

**Objetivo:** Permitir que o Agente de IA reconheça um cliente independentemente do canal de comunicação (WhatsApp, Instagram, Reclame Aqui, E-mail) e utilize o histórico completo de interações passadas para fornecer respostas altamente contextualizadas e personalizadas.

**Valor Agregado:**
*   **Hiper-Personalização:** O cliente não precisa repetir a história ("Como eu disse semana passada no telefone...").
*   **Gestão de Crise Proativa:** Identificar clientes detratores reincidentes que mudam de canal para escalar reclamações.
*   **Eficiência Operacional:** A IA pode resolver problemas complexos sabendo o contexto (ex: status de um pedido mencionado anteriormente), evitando transbordo humano desnecessário.
*   **Retenção:** Clientes que se sentem "lembrados" têm maior probabilidade de fidelização.

---

## 2. O Desafio Técnico: Resolução de Identidade (Identity Resolution)

O principal obstáculo é **garantir a segurança** ao vincular identidades digitais distintas a uma **Pessoa Única (Customer ID)**.

**Classificação de Canais por Confiança:**

1.  **Canais Confiáveis (Trusted Identifiers):**
    *   **WhatsApp:** O número de telefone (`+55...`) é validado pela Meta. Vínculo direto.
    *   **App/Chat Logado:** O `user_id` vem do token de sessão autenticado. Vínculo direto.

2.  **Canais Abertos/Não Confiáveis (Zero Trust Inicial):**
    *   **Redes Sociais (Instagram, Twitter, YouTube):** Qualquer pessoa pode criar um perfil `@joao_silva`.
    *   **Risco:** Um fraudador pode se passar por um cliente legítimo para obter dados sensíveis se a IA tentar "adivinhar" quem é.
    *   **Solução Segura:** A IA nunca assume a identidade. Ela deve **solicitar validação explícita** através de um ambiente seguro.

---

## 3. Arquitetura Proposta

A solução é híbrida, combinando **Banco Relacional (SQL)** para identidade e **Banco Vetorial (RAG)** para memória semântica.

### 3.1 Camada de Identidade (PostgreSQL Relacional)
Necessidade de uma tabela "De-Para" que centraliza a identidade e gerencia o nível de confiança.

**Nova Tabela: `customer_identities`**
| customer_id (UUID) | channel_type (Enum) | channel_identifier (String) | trust_level (Enum) | verified_at |
| :--- | :--- | :--- | :--- | :--- |
| `c-123-uuid` | `WHATSAPP` | `+5511999998888` | `VERIFIED_PHONE` | 2024-02-14 |
| `c-123-uuid` | `APP_LOGIN` | `user_555` | `AUTHENTICATED` | 2024-02-14 |
| `c-123-uuid` | `INSTAGRAM` | `@joao.silva_dev` | `LINKED_VIA_OAUTH` | 2024-02-10 |
| `temp-anon-uuid` | `YOUTUBE` | `JSilva88` | `UNVERIFIED` | null |

### 3.2 Camada de Memória (PGVector - RAG)
O histórico de conversas não é guardado apenas como log, mas como **vetores de significado**.

**Estratégia de Ingestão no `vector_store`:**
Ao finalizar um atendimento ou processar uma mensagem relevante, o sistema deve:
1.  **Resumir:** Gerar um sumário da interação (ex: "Cliente reclamou de atraso na entrega do pedido #500. Estava frustrado.").
2.  **Vetorizar:** Transformar esse resumo em Embeddings.
3.  **Metadados (CRÍTICO):** Gravar com o metadado `{ "customer_id": "c-123-uuid", "type": "conversation_history", "date": "..." }`.

---

## 4. Fluxo de Implementação (Roadmap)

### Fase 1: Fundação de Dados (Identity Service)
1.  **Modelagem de Dados:** Tabela `customer_identities` com status de verificação.
2.  **Fluxo de Segurança (Canais Abertos):**
    *   Se msg vem de canal não verificado (`@joao` no Insta):
    *   IA responde: *"Para sua segurança e para acessar seu histórico, por favor autentique-se clicando aqui: `https://meuapp.com/vincular?token=xyz`"*.
    *   Usuário clica -> Loga no App -> Sistema grava vínculo: `@joao` = `Cliente ID 555`.
    *   A partir daí, `@joao` é tratado como confiável.

### Fase 2: Ingestão de Memória (Memory Service)
1.  **Pipeline de Resumo:** Criar um passo pós-processamento no `BrandAnalyzerService`.
    *   Após a resposta da IA, uma *segunda chamada* (assíncrona) pede para a LLM: "Resuma essa interação em 1 frase focada em fatos e sentimentos para memória futura."
2.  **Gravação Vetorial:** Usar o `KnowledgeIngestionService` (ou um novo `MemoryIngestionService`) para salvar esse resumo no PGVector com o `metadata` do `customer_id` identificado na Fase 1.

### Fase 3: Recuperação Contextual (RAG Retrieval)
1.  **Enriquecimento na Entrada:**
    *   Cliente manda mensagem -> `IdentityService` busca `customer_id`.
2.  **Busca Vetorial Filtrada:**
    *   Se `customer_id` existe -> `VectorStore.similaritySearch(query, filter: { "customer_id": "..." })`.
    *   Isso garante que a IA só "lembre" coisas *deste* cliente específico.
3.  **Prompt Engineering:**
    *   Injetar o histórico recuperado no System Prompt:
    > "CONTEXTO DO CLIENTE: Este cliente tem histórico de [resumos recuperados do RAG]. Considere isso ao responder."

---

## 5. Mockup do Prompt Final (Exemplo)

```text
SYSTEM:
Você é um atendente da marca X.
Use as diretrizes da marca abaixo:
[RAG: Diretrizes Gerais da Marca]

HISTÓRICO DO CLIENTE (Recuperado via Vector Store + Customer ID):
- [10/02/2026]: Cliente reclamou via Instagram sobre a cor do produto errado. Ticket #998 aberto. Resolvido com cupom.
- [20/01/2026]: Cliente elogiou a entrega rápida no Facebook.

MENSAGEM ATUAL (WhatsApp):
"Oi, meu cupom não está funcionando, podem me ajudar de novo?"

TAREFA:
Responda de forma empática, reconhecendo o problema anterior da cor errada.
```

## 6. Stack Tecnológica
*   **Java 25 + Spring Boot 4**
*   **Spring AI 2.0.0-M2+** (Suporte robusto a Metadata Filters no PGVector).
*   **PostgreSQL 16+** (Com `pgvector` extension).
*   **LLM Local (Zero Custo):** Llama 3.1 8B via **Ollama** rodando em Docker.
*   **Embeddings Local:** `all-minilm` via Ollama (384 dimensões).
