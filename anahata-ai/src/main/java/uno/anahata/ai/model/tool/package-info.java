/**
 * Defines the domain model for the tool (function) calling subsystem.
 * <p>
 * This package provides a set of model-agnostic POJOs and enums that represent
 * the entire lifecycle of a tool call, from its declaration to its final execution
 * result. By creating this abstraction layer, we decouple the core logic of the
 * {@code ToolManager} and the UI from the specific data types of any single AI
 * provider (like Google's {@code FunctionCall} or {@code FunctionResponse}).
 * <p>
 * The key components are:
 * <ul>
 *   <li>{@link uno.anahata.ai.model.tool.MethodDeclaration}: A model-agnostic definition of a tool.</li>
 *   <li>{@link uno.anahata.ai.model.tool.MethodInvocation}: A request from the model to execute a tool.</li>
 *   <li>{@link uno.anahata.ai.model.tool.MethodInvocationResult}: A rich object containing the final outcome of an invocation.</li>
 *   <li>State Enums ({@link uno.anahata.ai.model.tool.ToolPreference}, {@link uno.anahata.ai.model.tool.PromptDecision}, {@link uno.anahata.ai.model.tool.InvocationStatus}):
 *       Clearly defined enums that model the different states and choices within the
 *       tool lifecycle, eliminating the ambiguity of the previous implementation.</li>
 * </ul>
 *
 * @author anahata-gemini-pro-2.5
 */
package uno.anahata.ai.model.tool;
