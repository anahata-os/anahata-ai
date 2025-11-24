/* Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça! */
package uno.anahata.ai.gemini;

/**
 * A simple main class in the Gemini provider module that launches the core,
 * provider-agnostic command-line interface.
 * <p>
 * It delegates directly to the core {@code Cli.main} method, which uses reflection
 * to discover and load the {@code GeminiCliChatConfig} in this module's classpath.
 *
 * @author anahata-gemini-pro-2.5
 */
public class Main {
    public static void main(String[] args) {
        // Delegate directly to the core CLI's main method.
        uno.anahata.ai.Cli.main(args);
    }
}
