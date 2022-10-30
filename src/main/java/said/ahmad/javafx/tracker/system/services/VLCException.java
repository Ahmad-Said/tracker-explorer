package said.ahmad.javafx.tracker.system.services;

import lombok.Getter;

@Getter
public class VLCException extends Exception{
    private String title;
    private String header;
    private String content;
    /**
     *
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     */
    public VLCException(String title, String header, String content) {
        super(title + "\n" + header + "\n" + content);
        this.title = title;
        this.header = header;
        this.content = content;
    }
}
