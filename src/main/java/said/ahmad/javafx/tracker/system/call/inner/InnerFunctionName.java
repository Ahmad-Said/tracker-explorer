package said.ahmad.javafx.tracker.system.call.inner;

import lombok.Getter;

public enum InnerFunctionName {
    /**
     * Command to merge multiple files together
     */
    MERGE_PDF("Combine multiple pdf files into one file"),

    COMPARE_FILES("Compare files"),

    COPY_FULL_PATH("Copy full path to clipbaord"),

    COPY_FILE_NAME("Copy file name to clipboard");


    @Getter
    private String description;
    InnerFunctionName(String description) {
        this.description = description;
    }
}
