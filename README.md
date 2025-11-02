# Anahata AI Project

This is the parent project for the Anahata AI multi-module build.

## Modules

This project orchestrates the build for the following modules:

-   `anahata-ai`: The core, model-agnostic AI framework.
-   `anahata-ai-gemini`: The implementation-specific module for Google's Gemini models. (Planned)
-   `anahata-ai-swing`: The Swing UI components for building standalone chat applications. (Planned)
-   `anahata-ai-netbeans`: The NetBeans plugin that integrates the AI assistant into the IDE. (Planned)

## Building

To build all modules, run the following command from this directory:

```bash
mvn clean install
```
