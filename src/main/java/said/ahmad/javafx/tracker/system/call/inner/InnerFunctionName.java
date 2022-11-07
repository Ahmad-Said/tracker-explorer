package said.ahmad.javafx.tracker.system.call.inner;

import lombok.Getter;

public enum InnerFunctionName {
    /**
     * Command to merge multiple files together
     */
    MERGE_PDF("Combine multiple pdf files into one file");

    @Getter
    private String description;
    InnerFunctionName(String description) {
        this.description = description;
    }
}
