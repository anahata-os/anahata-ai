package uno.anahata.ai.tool.schema;

import uno.anahata.ai.tool.AIToolParam;
import uno.anahata.ai.tool.AiTool;
import uno.anahata.ai.tool.AiToolkit;
import java.io.File;

@AiToolkit("A mock toolkit for testing schema generation.")
public class MockToolkit {

    @AiTool("Returns a greeting for the given name.")
    public String sayHello(@AIToolParam("The name to greet.") String name) {
        return "Hello, " + name;
    }
/*
    @AiTool("A method that returns a complex object (File).")
    public File getFileDetails(@AIToolParam("The path of the file.") String path) {
        return new File(path);
    }
    */
    @AiTool("A method that returns a recursive Tree object.")
    public Tree getTree() {
        return new Tree();
    }

    @AiTool("A method with no return value.")
    public void doNothing() {
        // This method does nothing and returns void.
    }
}
