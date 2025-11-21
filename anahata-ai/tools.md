# Anahata AI V2 Tool Framework Architecture

This document provides a detailed technical explanation of the `anahata-ai` tool framework. The framework is designed to be model-agnostic, reflection-based, type-safe, and self-documenting.

## 1. Core Principles

-   **Model-Agnostic:** The entire framework is defined within the `anahata-ai` core module. It has no dependency on any specific AI provider. Provider-specific "adapter" modules are responsible for translating their native function-calling formats to and from this core model.
-   **Annotation-Driven:** The framework uses a simple set of annotations to discover and define tools directly from Java source code, minimizing boilerplate.
-   **Type-Safe & Self-Documenting:** By leveraging Java reflection and a powerful schema generator, the framework automatically creates rich, detailed schemas for all tools, parameters, and return types. This ensures the AI model has a precise understanding of how to use the tools.
-   **Deferred Execution:** The framework follows a deferred execution model. A tool call from the model creates a `ToolCall` object which contains a corresponding `ToolResponse` object. The `execute()` method on the response object is called later, providing a clean separation between parsing the model's request and executing the business logic.

## 2. Key Components

### 2.1. Annotations (`uno.anahata.ai.tool`)

-   `@AiToolkit`: A class-level annotation that marks a class as a container for related tools. It requires a description of the toolkit's purpose.
-   `@AiTool`: A method-level annotation that marks a public method within a toolkit as an AI-callable tool. It requires a detailed description of what the tool does.
-   `@AIToolParam`: A parameter-level annotation that provides a description for each parameter of an `@AiTool`-annotated method.

### 2.2. The Tool Manager (`uno.anahata.ai.tool.ToolManager`)

The `ToolManager` is the central orchestrator of the framework. Its responsibilities include:

1.  **Discovery & Registration:** It scans specified classes, finds those annotated with `@AiToolkit`, and uses reflection to parse them into `JavaObjectToolkit` domain objects.
2.  **Lifecycle Management:** It manages the collection of all available toolkits and tools.
3.  **Tool Call Factory:** Its primary runtime role is to act as a factory for creating `AbstractToolCall` objects. When the AI provider's adapter receives a tool call request, it passes the tool name and raw JSON arguments to `ToolManager.createToolCall()`. The manager finds the correct tool definition and initiates the type conversion and validation process.

### 2.3. The Domain Model (`uno.anahata.ai.model.tool.*`)

This is the heart of the frameworka rich, hierarchical set of POJOs representing the entire tool ecosystem.

-   **`JavaObjectToolkit`**: Represents a single class annotated with `@AiToolkit`. It contains a list of all the tools discovered within it.
-   **`JavaMethodTool`**: Represents a single method annotated with `@AiTool`. This is a rich object containing:
    -   The tool's name and description.
    -   A reference to the underlying Java `Method` object.
    -   A list of `JavaMethodToolParameter` objects.
    -   The user's configured permissions (`ToolPermission`).
-   **`JavaMethodToolParameter`**: Represents a single method parameter. Crucially, it stores not only the name and description but also the Java reflection `Type`, which is essential for accurate deserialization of arguments.

### 2.4. The Execution Lifecycle

1.  An AI model decides to call a tool and sends the tool name and arguments (as JSON) to the provider-specific adapter.
2.  The adapter calls `ToolManager.createToolCall(name, args)`.
3.  The `ToolManager` finds the corresponding `JavaMethodTool`.
4.  The `JavaMethodTool.createCall()` factory method is invoked. This method:
    a. Validates that all required parameters are present.
    b. Iterates through the `JavaMethodToolParameter` list. For each parameter, it uses `GSON` and the stored Java `Type` to deserialize the raw JSON value from the model into the correct Java object (e.g., `String`, `Integer`, `List<String>`, custom POJOs).
    c. Returns a new `JavaMethodToolCall` object containing the fully typed and validated Java arguments.
5.  The `JavaMethodToolCall` constructor automatically creates its corresponding `JavaMethodToolResponse` object, linking the call and response together.
6.  At a later stage, the `JavaMethodToolResponse.execute()` method is called.
7.  This `execute()` method uses the stored `Method` reference and the converted Java arguments to invoke the actual tool code via `method.invoke()`.
8.  The result, status, execution time, and any exceptions are captured within the `JavaMethodToolResponse` object, completing the cycle.

### 2.5. Schema Generation (`uno.anahata.ai.tool.schema.SchemaProvider`)

The `SchemaProvider` is a critical component that makes the framework self-documenting.

-   It uses a combination of Jackson, Swagger Core, and deep reflection.
-   It generates OpenAPI 3-compliant JSON schemas for tool parameters and return types.
-   **Key Feature:** It recursively analyzes Java types (including complex generics like `List<Map<String, MyObject>>`) and embeds the fully qualified Java class name into the `title` field of the schema. This provides unparalleled clarity for both the AI model and human developers.
-   The generated schemas are used by the provider adapters to construct the function declarations in the format required by the specific AI model's API.
