# AIChatAPI

# AI Chat API — Spring AI Learning & Integration Project
### *** IMP NOTE: Original author of the concept and structure of the code is eazybytes, I have modified it as and when required for my study purpose of AI related concepts learning. ***

A Java/Spring Boot application demonstrating practical **Generative AI integration using Spring AI and OpenAI**.

This project started with basic LLM chat APIs and progressively explores production-oriented AI application concepts including:

- Prompt engineering
- Streaming responses
- Structured output
- Chat memory
- RAG (Retrieval-Augmented Generation)
- Qdrant vector database
- Semantic caching
- Tool calling
- Trusted application context with `ToolContext`
- Web Search RAG using Tavily
- PII masking
- Token usage tracking
- Prometheus/Grafana observability
- OpenTelemetry/Jaeger tracing
- AI response evaluation using `RelevancyEvaluator`
- Reactive streaming over existing blocking JPA repositories

---

# Architecture

```text
                        Client / Postman
                               |
                               v
                       Spring REST API
                               |
                               v
                          ChatClient
                               |
          +--------------------+--------------------+
          |                    |                    |
          v                    v                    v
       OpenAI             Advisors              AI Tools
                            |                      |
                  +---------+---------+            |
                  |                   |            v
               Memory               RAG       Java Services
                  |                   |            |
                  v                   v            v
               Oracle              Qdrant       Oracle
                                      |
                            Documents / Embeddings
```

For selected use cases, the application also integrates external web search:

```text
User Question
     |
     v
Spring AI RAG
     |
     v
Tavily Web Search
     |
     v
Retrieved Documents
     |
     v
OpenAI
     |
     v
Grounded Response
```

---

# Technology Stack

- Java 25
- Spring Boot 4.1.0
- Spring AI 2.0.0
- OpenAI
- Spring MVC
- Spring Data JPA
- Oracle XE
- Qdrant Vector Database
- Apache Tika
- Tavily Search API
- Reactor / Project Reactor
- Spring Boot Actuator
- Micrometer
- Prometheus
- Grafana
- OpenTelemetry
- Jaeger
- Docker Compose
- Maven
- JUnit 5
- Lombok

---

# Main Spring AI Dependency

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

The project also uses dedicated Spring AI modules for:

```text
Qdrant Vector Store
RAG
Tika Document Reader
Semantic Cache
JDBC Chat Memory
```

---

# Environment Variables

The application expects the following environment variables.

## OpenAI

```text
OPENAI_API_KEY
```

## Oracle

```text
DB_USER
DB_PASSWORD
```

## Tavily Web Search

```text
TAVILY_API_KEY
```

Do not commit real API keys, database passwords or other credentials into Git.

---

# Basic OpenAI Chat

Basic OpenAI communication is exposed through:

```http
GET /api/openai/chat?message=What%20is%20Spring%20AI
```

Flow:

```text
REST Controller
      |
      v
ChatClient
      |
      v
OpenAI
      |
      v
Generated Response
```

The default `ChatClient` also uses:

- `SimpleLoggerAdvisor`
- Custom `TokenUsageAdvisor`

---

# Multiple ChatClient Configurations

Instead of using one `ChatClient` for every use case, the application defines multiple specialized clients.

Examples include:

```text
openAiChatClient
openAiChatClientWithOptions
chatMemoryChatClient
openChatClient
helpDeskChatClient
timeChatClient
webSearchRAGChatClient
```

This allows different applications of the LLM to have their own:

```text
System Prompt
Advisors
Tools
Memory
Model Options
RAG Configuration
```

---

# Model-Specific ChatClient

A separate ChatClient demonstrates model configuration:

```java
.defaultOptions(
    OpenAiChatOptions.builder()
        .model("gpt-5.4-mini")
)
```

This makes it possible to select different models for different workloads.

---

# Prompt Engineering

The project contains external prompt templates instead of embedding every prompt directly inside Java code.

Examples:

```text
prompt-stuffing/
├── hr-policy-sys-prompt-template.st
└── resume-analysis-sys-prompt.st
```

and:

```text
sys-prompt-template/
├── helpdesk-sys-prompt-template.st
├── rag-sys-prompt-hrpolicy.st
└── rag-sys-prompt-random-data-example.st
```

This keeps prompts separated from application logic and makes them easier to modify and maintain.

---

# HR Policy Chat

Example:

```http
GET /api/openai/hr-chat?message=How%20many%20annual%20leaves%20do%20employees%20get?
```

The controller attaches an HR-specific system prompt before sending the user request to the LLM.

Conceptually:

```text
HR System Prompt
       +
User Question
       |
       v
     OpenAI
       |
       v
HR-focused Answer
```

---

# Resume Analysis

The project contains separate endpoints for resume-oriented AI interaction.

```http
GET /api/openai/resume-chat
```

and:

```http
GET /api/openai/resume-chat-v2
```

The second version demonstrates using a ChatClient configured with explicit model options.

---

# Streaming LLM Responses

Streaming responses are supported using Reactor `Flux`.

```http
GET /api/openai/resume-stream-resp
```

Instead of waiting for the complete model response:

```text
Request
   |
   | wait...
   |
Complete Response
```

streaming allows tokens/content to arrive incrementally:

```text
Request
   |
   +--> chunk
   +--> chunk
   +--> chunk
   +--> chunk
```

The application uses:

```java
.stream()
.content()
```

to expose the generated response as:

```java
Flux<String>
```

---

# Structured Output

The project demonstrates converting an LLM response directly into a Java object.

Example endpoint:

```http
GET /api/openai/structured-resp
```

Instead of receiving only free-form text:

```text
"Delhi, Mumbai, Pune..."
```

Spring AI maps the result to:

```java
CountryCities
```

using:

```java
.call()
.entity(CountryCities.class)
```

This is useful when AI output needs to participate in normal backend processing.

---

# Chat Memory

The application supports persistent conversation memory using Spring AI JDBC Chat Memory.

Configuration creates:

```java
MessageWindowChatMemory
```

with:

```text
Maximum Messages = 10
```

The memory repository is backed by the configured relational database.

Endpoint:

```http
GET /api/openai/chat-memory
```

with header:

```text
username: user1
```

The username is used as:

```java
ChatMemory.CONVERSATION_ID
```

Conceptually:

```text
User A
   |
   +---- Conversation ID = userA
   |
   v
Chat Memory

User B
   |
   +---- Conversation ID = userB
   |
   v
Different Chat Memory
```

This allows conversation history to remain isolated by conversation identifier.

---

# Retrieval-Augmented Generation — RAG

The project implements a document-based RAG pipeline for HR policy queries.

Architecture:

```text
             INDEXING PHASE

HR Policy PDF
     |
     v
TikaDocumentReader
     |
     v
Documents
     |
     v
TokenTextSplitter
     |
     v
OpenAI Embeddings
     |
     v
Qdrant
```

Query phase:

```text
             QUERY PHASE

User Question
     |
     v
Query Transformation
     |
     v
Vector Search
     |
     v
Relevant Documents
     |
     v
PII Masking
     |
     v
Context Augmentation
     |
     v
OpenAI
     |
     v
Grounded Answer
```

---

# HR Policy Document Ingestion

The following document is loaded from the classpath:

```text
rag-documents/Eazybytes_HR_Policies.pdf
```

Apache Tika is used to read the PDF:

```java
TikaDocumentReader
```

The document is then split using:

```java
TokenTextSplitter
```

with approximately:

```text
Chunk Size     = 200
Maximum Chunks = 500
```

and stored inside Qdrant.

---

# Qdrant Vector Database

The main vector store is configured as:

```yaml
spring:
  ai:
    vectorstore:
      qdrant:
        initialize-schema: true
        host: localhost
        port: 6334
        collection-name: eazybytes
```

Qdrant is used for:

```text
Document embeddings
Similarity search
RAG retrieval
Semantic cache
```

The project maintains a separate Qdrant collection for semantic caching:

```text
eazybytes-semantic-cache
```

---

# RAG Retrieval Configuration

The RAG advisor retrieves:

```text
Top K = 3
Similarity Threshold = 0.5
```

Conceptually:

```text
Question
   |
   v
Embedding
   |
   v
Qdrant Similarity Search
   |
   +--> Chunk 1
   +--> Chunk 2
   +--> Chunk 3
```

Only sufficiently similar documents are supplied to the generation phase.

---

# Query Transformation

Before retrieval, the project uses:

```java
TranslationQueryTransformer
```

with:

```text
Target Language = English
```

This allows a user query to be transformed into English before document retrieval.

For example:

```text
User:
"Mala annual leaves kiti miltat?"

        |
        v

Query Transformer

        |
        v

"How many annual leaves are available?"
```

The transformed query can then be used for vector retrieval.

---

# PII Masking

Retrieved documents pass through a custom:

```java
PIIMaskingDocPostProcessor
```

before being provided to the LLM.

The processor currently masks:

```text
Email Addresses
Phone Numbers
```

Example:

```text
Before:

Contact john@example.com at +1-202-555-0185

After:

Contact [REDACTED_EMAIL] at [REDACTED_PHONE]
```

The document metadata also records:

```text
pii_masked = true
```

This demonstrates how post-retrieval processing can be inserted between:

```text
Retrieval
    |
    v
Document Processing
    |
    v
LLM
```

---

# RAG Endpoint

Document RAG is available through:

```http
GET /rag-api/document/chat
```

Required parameters:

```text
message=<question>
```

Header:

```text
username=<conversation-id>
```

The endpoint combines:

```text
Chat Memory
+
RetrievalAugmentationAdvisor
+
Qdrant
+
OpenAI
```

---

# Web Search RAG

The application also demonstrates RAG using live web-search results rather than only a local vector database.

Endpoint:

```http
GET /rag-api/web-search/chat
```

The application implements a custom Spring AI:

```java
DocumentRetriever
```

named:

```java
WebSearchDocumentRetriever
```

It calls the Tavily Search API and converts search results into Spring AI `Document` objects.

---

# Tavily Web Search Flow

```text
User Question
     |
     v
RetrievalAugmentationAdvisor
     |
     v
WebSearchDocumentRetriever
     |
     v
Tavily API
     |
     v
Search Results
     |
     v
Spring AI Documents
     |
     v
OpenAI
     |
     v
Web-grounded Response
```

The current implementation requests up to:

```text
5 search results
```

using Tavily:

```text
search_depth = advanced
```

Each result contains metadata such as:

```text
title
URL
score
```

---

# Semantic Cache

The application implements semantic caching through:

```java
SemanticCacheAdvisor
```

Unlike a traditional cache where keys must match exactly:

```text
"What is Java?"
```

and:

```text
"Explain Java to me"
```

are different string keys.

A semantic cache compares meaning using embeddings.

Conceptually:

```text
User Prompt
    |
    v
Embedding
    |
    v
Semantic Similarity Search
    |
    +---- Similar cached prompt found
    |          |
    |          v
    |     Return cached response
    |
    +---- No similar response
               |
               v
             LLM
```

The current implementation uses a dedicated Qdrant vector store with:

```text
Similarity Threshold = 0.8
```

---

# Open Chat with Semantic Cache

Endpoint:

```http
GET /api/open-chat
```

uses a ChatClient configured with:

```text
SimpleLoggerAdvisor
TokenUsageAdvisor
SemanticCacheAdvisor
```

This provides a simple endpoint for testing semantic cache behavior.

---

# Tool Calling

The project demonstrates Spring AI tool calling without MCP.

Two tool domains are currently included:

```text
Time Tools
HelpDesk Tools
```

---

# Time Tools

Available tools include:

```text
getCurrentLocalTime
getCurrentTime
```

Example user request:

```text
What is the current time in Asia/Kolkata?
```

Instead of expecting the LLM to guess the current time:

```text
User
  |
  v
LLM
  |
  | selects getCurrentTime
  v
Java Tool
  |
  v
ZoneId / LocalTime
  |
  v
Actual Result
```

This demonstrates how tools can provide real-time or deterministic information that should not be generated from model knowledge.

---

# HelpDesk Tool Calling

The HelpDesk functionality demonstrates how an AI conversation can call normal enterprise Java services.

Available tools:

```text
createTicket
fetchTickets
```

Architecture:

```text
User
  |
  v
LLM
  |
  | selects tool
  v
@Tool
  |
  v
HelpDeskTicketService
  |
  v
HelpDeskTicketRepository
  |
  v
Oracle
```

This follows the same layered architecture as a traditional Spring application.

Compare:

```text
Traditional REST

Controller
   |
   v
Service
   |
   v
Repository
```

with:

```text
AI Tool Calling

LLM
 |
 v
@Tool
 |
 v
Service
 |
 v
Repository
```

Business logic remains inside the normal application layers.

---

# ToolContext for Trusted Application Data

The HelpDesk flow demonstrates passing user information through:

```java
ToolContext
```

The REST layer receives:

```text
username
```

as an HTTP header.

It then passes the value through:

```java
.toolContext(
    Map.of("username", username)
)
```

The tool reads it using:

```java
toolContext.getContext().get("username")
```

Conceptually:

```text
HTTP Header
    |
    v
Spring Application
    |
    v
ToolContext
    |
    v
HelpDesk Tool
```

The LLM does not need to generate or infer the username.

This is useful for application-owned values such as:

```text
User Identity
Tenant ID
Security Context
Correlation ID
```

---

# Direct Tool Response

The `createTicket` tool demonstrates:

```java
returnDirect = true
```

Conceptually, normal tool execution can be:

```text
User
 |
 v
LLM
 |
 v
Tool
 |
 v
Tool Result
 |
 v
LLM Again
 |
 v
Final Response
```

With direct return:

```text
User
 |
 v
LLM
 |
 v
Tool
 |
 v
Final Tool Result
```

This can be useful for deterministic business operations where unnecessary LLM rewriting may introduce additional or unsupported information.

---

# HelpDesk Chat Endpoint

```http
GET /api/tool/helpdesk-chat
```

Parameters:

```text
message=<request>
```

Header:

```text
username=<user>
```

Example:

```text
My VPN is not working. Create a support ticket.
```

The model can identify that ticket creation is required and invoke the corresponding Java tool.

---

# Token Usage Advisor

The application contains a custom:

```java
TokenUsageAdvisor
```

which logs model usage information after each LLM request.

It captures model metadata such as token usage and also logs the temperature used for the request.

This demonstrates how Spring AI Advisors can be used for cross-cutting concerns.

Conceptually:

```text
Request
   |
   v
Advisor
   |
   v
LLM
   |
   v
Response
   |
   v
Advisor
   |
   +--> Token Usage
   +--> Temperature
```

---

# Advisors

The project demonstrates several types of Spring AI advisors:

```text
SimpleLoggerAdvisor
MessageChatMemoryAdvisor
SemanticCacheAdvisor
RetrievalAugmentationAdvisor
Custom TokenUsageAdvisor
```

This creates an advisor chain around model interaction.

```text
User
 |
 v
Logging
 |
 v
Memory
 |
 v
Cache / RAG
 |
 v
LLM
 |
 v
Response
```

The exact advisor set depends on the configured ChatClient.

---

# Reactive Streaming Demo

The project also includes a separate books example to demonstrate Reactor and streaming over a traditional JPA repository.

Endpoints include:

```http
GET /api/books
```

and:

```http
GET /api/books-stream
```

The streaming endpoint uses:

```text
Server-Sent Events
```

and returns:

```java
Flux<BookRecord>
```

---

# Blocking J
