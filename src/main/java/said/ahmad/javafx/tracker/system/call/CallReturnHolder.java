package said.ahmad.javafx.tracker.system.call;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

@Data
@AllArgsConstructor
/**
 * Class that hold argument of function execute, also its returned process
 * @see Runtime#exec(String, String[], File)
 */
public class CallReturnHolder {
    /**
     * Process executing the call, Can be null.
     */
    private Process process;
    private String command;
    private File workingDir;

}
