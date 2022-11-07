package said.ahmad.javafx.tracker.system.call.inner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InnerFunctionCall {
    public static final Map<InnerFunctionName, CallBackContext> FUNCTION_CALLS = Collections.unmodifiableMap(
            new HashMap<InnerFunctionName, CallBackContext>() {
                {
                    put(InnerFunctionName.MERGE_PDF, new PdfMergerFunction());
                }
            }
    );
}
