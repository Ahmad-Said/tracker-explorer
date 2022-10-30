package said.ahmad.javafx.tracker.system.call.inner;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class InnerFunctionCall {
    @Getter
    public static Map<FunctionName, CallBackContext> functionCalls = new HashMap<FunctionName, CallBackContext>() {
        {
            put(FunctionName.MERGE_PDF, new PdfMergerFunction());
        }
    };
}
