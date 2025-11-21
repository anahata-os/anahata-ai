package uno.anahata.ai.tool.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Represents a tree data structure with a single root node.")
public class Tree {
    @Schema(description = "The name of this tree.", required = true)
    private String name;
    
    @Schema(description = "The root node of the tree.")
    private TreeNode root;
}
