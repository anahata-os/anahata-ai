# V2 Tool Subsystem Doctrine

This document outlines the definitive design and requirements for the V2 tool subsystem in the `anahata-ai` framework.

## 1. Core Principle: Domain-Driven Design

The tool subsystem is built on a hierarchical, object-oriented domain model. The flat, disorganized lists of the V1 architecture are superseded by a clean, encapsulated design.

-   **`ToolManager`**: The central orchestrator, responsible for discovering and managing a list of `JavaObjectTool` instances.
-   **`JavaObjectTool`**: A rich domain object representing a single tool class. It acts as a container for the tool's singleton instance and its list of `MethodDeclaration` objects.
-   **`MethodDeclaration`**: A stateful domain object representing a single tool method. It contains all static metadata (name, description, parameters), a `transient Method` for invocation, and the user's runtime `ToolPermission`.

## 2. The Unified `ToolPermission` Model



### 2.1. Effecitve `ToolPermission` 

1.  **Annotation value**: By default when a tool gets loaded, the effective permissions is the requiresApproval annotation value, if "false" it woud default to
ALWAYS, otherwise it will default to YES
    **If the user has application level preference defaults configured** those will override the annoation definitions
    **Value from last prompt** whatever the user chooses on the batch tool call prompt, that preference will be store for that chat




## 3. Preference Storage
The global preferences with kryo in the work dir
All preferences for that chat will be serialized when the session gets saved in the SessionData 

