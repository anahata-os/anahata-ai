# Anahata AI Project Plan

This document outlines the vision, architecture, and development plan for the `anahata-ai` project. This project serves as the core, model-agnostic AI framework.

## 1. Vision & Goal

The primary goal is to create a robust, extensible, and model-agnostic AI framework in Java. This core library will define a standard set of interfaces and models for interacting with Large Language Models (LLMs), allowing developers to build AI-powered applications without being locked into a specific provider (e.g., Google Gemini, OpenAI, etc.).

This project will contain the foundational logic, while provider-specific implementations will be developed in separate "adapter" projects (e.g., `anahata-ai-gemini`).

## 2. Architectural Principles (Validated)

Our architecture is founded on established software design patterns, validated by research into existing AI frameworks like LangChain4j and Spring AI.

*   **The Adapter Pattern:** This is our core principle. Provider-specific modules (like `anahata-ai-gemini`) will act as **Adapters**. They will be responsible for translating the provider's unique API and data types (e.g., a Google `FunctionCall`) into the standardized, internal models defined in this core `anahata-ai` project. This decouples our application logic from any single vendor.

*   **Decoupling and Portability:** By defining a standard interface for chat, tool execution, and context management, we can easily swap LLM providers. This is a best practice highlighted by major frameworks and ensures long-term flexibility.

*   **Future-Proofing with Factory/Strategy Patterns:** As the project grows, we can incorporate other patterns:
    *   **Factory Pattern:** To dynamically create clients for different models (`ModelFactory.create("gemini")`).
    *   **Strategy Pattern:** To select the best model for a specific task on-the-fly.

## 3. Proposed Core Package Structure

Based on our discussions, the initial package structure will be:

*   `uno.anahata.ai.chat`: Contains the core logic for managing a conversation.
    *   `model`: Will house standardized data models like `AnahataChatMessage`.
*   `uno.anahata.ai.tool`: The central hub for defining and executing tools.
    *   `execution`: For the `FunctionExecutor` and related classes.
    *   `conversion`: For the `TypeConverter` responsible for serializing return types.
    *   `model`: For standardized tool-related models like `AnahataFunctionInfo` and `ExecutedToolCall`.
*   `uno.anahata.ai.context`: For managing the stateful "Active Workspace" and context-related logic.

## 4. Immediate Development Roadmap

**Phase 1: Project Setup**
1.  **Configure `pom.xml`:**
    *   Set the Java version to 21.
    *   Add dependencies for `lombok` and `swagger-annotations`.
    *   Critically, configure the `maven-compiler-plugin` with the `-parameters` argument to preserve method parameter names for the tool framework.
2.  **Create Core Packages:** Create the directory structure outlined above.

**Phase 2: Initial Class Implementation**
1.  Begin migrating and refactoring the core, non-Gemini-specific classes from `gemini-java-client` into their new homes within `anahata-ai`.
2.  Develop the initial standardized data models (e.g., `AnahataChatMessage`).

## 5. References & Citations

*   **Adapter Pattern:** [Refactoring Guru: Adapter Pattern](https://refactoring.guru/design-patterns/adapter)
*   **LangChain4j:** [Official Documentation](https://docs.langchain4j.dev/) - An example of a model-agnostic Java AI framework.
*   **Spring AI:** [Official Project Page](https://spring.io/projects/spring-ai) - Demonstrates the industry trend towards portable AI APIs.
