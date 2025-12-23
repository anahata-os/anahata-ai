# Anahata AI V2 - Consolidated Task List (Core & Swing Modules)

This document consolidates all active and pending tasks for the core framework and its Swing UI module.

## High Priority / Core Framework

- [ ] **UI Fix:** `JXErrorPane` stack trace still not indented (wrong component font changed).
- [ ] **Response Modalities and Server Tools Design:** Implement `AbstractModel.getSupportedResponseModalities()` `AbstractModel.getAvailableServerTools()` and `GeminiModel` implementation. `RquestConfig` should have `List<String> getResponseModalities()` `List<ServerTool> getEnabledServerTools()`. 
- [ ] add support for token streaming so the tokens are rendered as they arrive
- [ ] add tool execution panels (tool calls and responses) needs extensive discussion before starting
- [ ] Session Management: Implement the "Save" and "Load Session" buttons


## UI / Swing Module Tasks



## Research & Technical Debt

- [ ] Research Shared Schema Definitions for Token Optimization
- [ ] Improve Binary File Handling in `LocalFiles` Tool
- [ ] Implement FailureTracker in V2 ToolManager
- [ ] Implement Asynchronous Job Execution for V2 Tools
- [ ] Live Workspace: Implement the functionality for the "Live Workspace" button
- [ ] Flesh out `ToolsPanel` details (schemas, logging)
- [ ] Context Heatmap
- [ ] Api Keys editor
- [ ] **Report Bug Capability:** Implement a feature to allow users to report bugs directly from the application, including relevant context.
