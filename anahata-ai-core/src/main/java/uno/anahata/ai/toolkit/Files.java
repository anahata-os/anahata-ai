package uno.anahata.ai.toolkit;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import uno.anahata.ai.model.resource.AbstractPathResource;
import uno.anahata.ai.model.resource.AbstractResource;
import uno.anahata.ai.model.resource.TextFileResource;
import uno.anahata.ai.model.resource.TextViewport;
import uno.anahata.ai.tool.AiTool;
import uno.anahata.ai.tool.AiToolException;
import uno.anahata.ai.tool.AiToolkit;
import uno.anahata.ai.tool.JavaToolkitInstance;
import uno.anahata.ai.tool.AiToolParam;

/**
 * The definitive V2 toolkit for interacting with file-based resources.
 *
 * @author anahata-ai
 */
@AiToolkit("A toolkit for loading and managing file-based resources.")
@Slf4j
public class Files extends JavaToolkitInstance {

    public void updateTextFileViewport(
            @AiToolParam("The absolute paths to the text files.") String resourceId, @AiToolParam("The new view port for the text file") TextViewport newViewPort) throws Exception {
        TextFileResource tfr = getResourceManager().getResource(resourceId);
        tfr.setViewport(newViewPort);
        tfr.reload();
    }

    /**
     * Loads a text file into the context as a managed resource. The tool's
     * response is ephemeral and will be pruned from the context on the next
     * turn.
     *
     * @param path The absolute path to the text file.
     * @return The newly created TextFileResource.
     * @throws Exception if the file does not exist, is already loaded, or an
     * I/O error occurs.
     */
    @AiTool(value = "Loads a text file into the context as a managed resource.", retention = 0)
    public List<TextFileResource> loadTextFileResources(
            @AiToolParam("The absolute paths to the text files.") List<String> resourcePaths) throws Exception {

        List<TextFileResource> ret = new ArrayList<>(resourcePaths.size());
        List<String> errors = new ArrayList<>();
        for (String path : resourcePaths) {
            try {
                log("Loading " + path + "...");
                ret.add(loadTextFile(path));
                log("Loaded OK " + path);
            } catch (Exception e) {
                log.error("Exception loading text file resource", e);
                log(ExceptionUtils.getStackTrace(e));
                errors.add(e.getMessage());
            }
        }
        if (!errors.isEmpty()) {
            super.getResponse().setError(errors.toString());
        }

        if (ret.isEmpty()) {
            throw new AiToolException("Nothing got loaded");
        }

        return ret;
    }

    /**
     * Loads a text file into the context as a managed resource. The tool's
     * response is ephemeral and will be pruned from the context on the next
     * turn.
     *
     * @param path The absolute path to the text file.
     * @return The newly created TextFileResource.
     * @throws Exception if the file does not exist, is already loaded, or an
     * I/O error occurs.
     */
    private TextFileResource loadTextFile(String path) throws Exception {

        if (findByPath(path).isPresent()) {
            throw new AiToolException("Resource already loaded for path: " + path);
        }

        if (!java.nio.file.Files.exists(Paths.get(path))) {
            throw new AiToolException("File not found: " + path);
        }

        TextFileResource resource = new TextFileResource(Paths.get(path));
        getResourceManager().register(resource);
        log("Successfully loaded and registered text file: " + path);
        return resource;
    }

    /**
     * Finds a managed resource by its absolute file path. This is a private
     * helper method that encapsulates the logic specific to this toolkit.
     *
     * @param path The path to search for.
     * @return An Optional containing the resource if found, otherwise empty.
     */
    private Optional<AbstractPathResource> findByPath(String path) {
        return getResourceManager().getResources().stream()
                .filter(r -> r instanceof AbstractPathResource)
                .map(r -> (AbstractPathResource) r)
                .filter(r -> r.getPath().equals(path))
                .findFirst();
    }
}
