package said.ahmad.javafx.tracker.system.call.inner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InnerFunctionCall {
    public static final Map<InnerFunctionName, CallBackContext> FUNCTION_CALLS = Collections.unmodifiableMap(
            new HashMap<InnerFunctionName, CallBackContext>() {
                {
                    put(InnerFunctionName.MERGE_PDF, new PdfMergerFunction());
                    put(InnerFunctionName.COPY_FULL_PATH, new CopyPathFunction(true));
                    put(InnerFunctionName.COPY_FILE_NAME, new CopyPathFunction(false));
                    put(InnerFunctionName.COMPARE_FILES, new CompareFilesFunction());
                }
            }
    );
}
