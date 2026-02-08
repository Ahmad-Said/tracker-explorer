package said.ahmad.javafx.tracker.system.call;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.system.call.inner.InnerFunctionCall;
import said.ahmad.javafx.tracker.system.call.inner.InnerFunctionName;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.PathLayerHelper;

public class GenericCaller {

    /**
     * @param source
     * @param prefix
     * @param suffix
     * @return temporary created file containing list of absolute paths, one on each
     * line.
     * @throws IOException
     */
    public static File getTempFileList(List<PathLayer> source, String prefix, String suffix) throws IOException {
        return getTempFileList(source, false, prefix, suffix);
    }

    /**
     * @param source
     * @param useNamesOnly <code>true</code> will print name of file only. <br>
     *                     <code>false</code> will print absolute path of file
     * @param prefix
     * @param suffix
     * @return temporary created file containing info of files, one file on each
     * line.
     * @throws IOException
     */
    public static File getTempFileList(List<PathLayer> source, boolean useNamesOnly, String prefix, String suffix)
            throws IOException {
        File tempFileList = File.createTempFile(prefix, suffix);
        // was using UTF 8 here but give problem with arabic letter since windows do not
        // use it as default!
        OutputStreamWriter p = new OutputStreamWriter(new FileOutputStream(tempFileList), Charset.defaultCharset());
        for (PathLayer path : source) {
            if (useNamesOnly) {
                p.write(path.getName() + "\n");
            } else {
                p.write(path.toString() + "\n");
            }
        }
        p.close();
        tempFileList.deleteOnExit();
        return tempFileList;
    }

    /**
     * Splits the input string by spaces, but preserves groups of words surrounded by double quotes.
     * The double quotes are removed in the output.
     *
     * <p>The input string is split based on the following rules:
     * <ul>
     *   <li>Words separated by spaces are split as individual elements in the returned list.</li>
     *   <li>Words or phrases enclosed in double quotes (") are treated as a single element and
     *   will not be split by spaces within the quotes.</li>
     *   <li>The double quotes themselves are removed in the output.</li>
     * </ul>
     *
     * @param input the input string to split, which may contain quoted phrases.
     * @return an {@link ArrayList} of {@link String} containing individual words or quoted phrases.
     * @example <pre>
     *     String input = "This is a \"quoted string\" example";
     *     ArrayList<String> result = splitPreserveQuotes(input);
     *     // result = ["This", "is", "a", "quoted string", "example"]
     * </pre>
     * @example <pre>
     *     String input = "She said \"Hello World\" with a smile";
     *     ArrayList<String> result = splitPreserveQuotes(input);
     *     // result = ["She", "said", "Hello World", "with", "a", "smile"]
     * </pre>
     */
    public static List<String> splitPreserveQuotes(String input) {
        ArrayList<String> result = new ArrayList<>();

        // Regex to match quoted text or sequences of non-space characters
        Matcher matcher = cmdQuotedPattern.matcher(input);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // If it's a quoted string, add it without the quotes
                result.add(matcher.group(1));
            } else {
                // Otherwise, add the regular word
                result.add(matcher.group());
            }
        }

        return result;
    }
    private static final Pattern cmdQuotedPattern = Pattern.compile("\"([^\"]*)\"|\\S+");

    public static List<String> prepareCommandOptions(String commandOptions) {
        return splitPreserveQuotes(commandOptions);
    }

    /**
     * Execute Command contained in UserContextMenu on the provided list.<br>
     * This fucntion does not check if context menu is compatible with the list.<br>
     * User {@link UserContextMenu#isCompatibleWithList(List)} to do so. <br>
     * <p>
     * Showing context command output is up to the caller, but will generate process
     * output in return holder.
     *
     * @param selections
     * @param con
     * @return
     * @throws IOException
     */
    public static List<CallReturnHolder> call(List<PathLayer> selections, UserContextMenu con, boolean isExecuteProcess)
            throws IOException {
        List<CallReturnHolder> callReturn = new ArrayList<>();

        /**
         * 	optional to do later if it is worth combining all list of files in case of recursive mode and use most common root
         *    {@link Path#relativize}.
         * 	Currently working in single directory and working directory is same as this directory.
         */
        boolean isUsingRelatifPath = con.isCallUsingRelatifPath()
                && PathLayerHelper.getParentsPathsAsString(selections.stream()).size() == 1;
        switch (con.getCallMethod()) {
            case COMBINED_CALL:
                String combinedCallCMD = buildCombinedCommand(selections, con, isUsingRelatifPath);
                File workingDirComb = selections.get(0).getParentPath().toFileIfLocal();
                Process pComb = executeProcess(combinedCallCMD, workingDirComb, isExecuteProcess);
                callReturn.add(new CallReturnHolder(pComb, combinedCallCMD, workingDirComb));
                break;
            case SEPARATE_CALL:
                for (PathLayer selection : selections) {
                    String separateCallCMD = buildSeparateCommand(selection, selections, con, isUsingRelatifPath);
                    File workingDir = selection.getParentPath().toFileIfLocal();
                    Process pSing = executeProcess(separateCallCMD, workingDir, isExecuteProcess);
                    callReturn.add(new CallReturnHolder(pSing, separateCallCMD, workingDir));
                    sleepForProcess();
                }
                break;
            case TXT_FILE_CALL:
                String txtFileCallCMD = buildTxtFileCommand(selections, con, isUsingRelatifPath);
                File workingDirTxt = selections.get(0).getParentPath().toFileIfLocal();
                Process pTxt = executeProcess(txtFileCallCMD, workingDirTxt, isExecuteProcess);
                callReturn.add(new CallReturnHolder(pTxt, txtFileCallCMD, workingDirTxt));
                sleepForProcess();
                break;
            case INNER_FUNCTION:
                handleInnerFunctionCall(selections, con, isExecuteProcess, callReturn);
                break;
        }
        if (con.isShowProcessOutput()) {
            for (CallReturnHolder callReturnHolder : callReturn) {
                callReturnHolder.generateProcessOutput();
            }
        }

        return callReturn;
    }

    private static String buildSeparateCommand(PathLayer selection,
                                        List<PathLayer> selections,
                                        UserContextMenu con,
                                        boolean isUsingRelatifPath) {
        String target = isUsingRelatifPath ? selection.getName() : selection.toString();
        return buildCommand(selections, con, selection, null, List.of(target));
    }

    private static String buildCombinedCommand(List<PathLayer> selections,
                                       UserContextMenu con,
                                       boolean isUsingRelatifPath) {
        List<String> targetList = selections.stream()
                .map(path -> isUsingRelatifPath ? path.getName() : path.toString())
                .toList();
        return buildCommand(selections, con, null, null, targetList);
    }

    private static String buildTxtFileCommand(List<PathLayer> selections,
                                       UserContextMenu con,
                                       boolean isUsingRelatifPath) throws IOException {
        File tempFileList = getTempFileList(selections, isUsingRelatifPath, "contextTracker", ".txt");
        return buildCommand(selections, con, null, tempFileList.toString(), null);
    }

    private static String buildCommand(
            List<PathLayer> selections,
            UserContextMenu con,
            PathLayer prioritySelectionIfAny,
            String targetFileIfAny,
            List<String> targetListIfAny) {
        StringBuilder callCmd = new StringBuilder();
        String executable = "\"" +  con.getPathToExecutableAsPath().getAbsolutePath() + "\"";
        String prefix = con.getPrefixCommandOptionsEvaluated(selections, prioritySelectionIfAny);
        String postfix = con.getPostfixCommandOptionsEvaluated(selections, prioritySelectionIfAny);
        if (targetFileIfAny != null) {
            if (!prefix.isEmpty() && prefix.charAt(prefix.length() - 1) == '@') {
                prefix = prefix.substring(0, prefix.length() - 1);
                // special fix for rar command line
                // the rar command line need the last character @ along with the file name
                // in here @ was in prefix we move it to target file to be quoted
                targetFileIfAny = "@" + targetFileIfAny;
            }
            targetFileIfAny = "\"" + targetFileIfAny + "\"";
            callCmd.append(executable)
                    .append(" ").append(prefix)
                    .append(" ").append(targetFileIfAny)
                    .append(" ").append(postfix);
        } else {
            callCmd.append(executable)
                    .append(" ").append(prefix);
            for (String target : targetListIfAny) {
                callCmd.append(" \"").append(target).append("\"");
            }
            callCmd.append(" ").append(postfix);
        }
        return callCmd.toString();
    }

    /**
     * Execute Command contained in UserContextMenu on the provided list.<br>
     *
     * @param command          the command will be transformed to array of strings and executed. each string will be separated preserving quoted ones.
     * @param workingDir       the working directory of the process
     * @param isExecuteProcess if <code>true</code> the process will be executed and returned, otherwise <code>null</code> will be returned.
     * @return the process if isExecuteProcess is <code>false</code>, otherwise <code>null</code>
     * @throws IOException if the command is not valid
     */
    private static Process executeProcess(String command, File workingDir, boolean isExecuteProcess) throws IOException {
        if (isExecuteProcess) {
            String[] cmdOptions = splitPreserveQuotes(command).toArray(new String[0]);
            return Runtime.getRuntime().exec(
                    cmdOptions,
                    null,
                    workingDir
            );
        }
        return null;
    }


    private static void sleepForProcess() {
        try {
            // Adding 200ms delay for consistency with SEPARATE_CALL case
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private static void handleInnerFunctionCall(List<PathLayer> selections,
                                                UserContextMenu con,
                                                boolean isExecuteProcess,
                                                List<CallReturnHolder> callReturn) {
        try {
            InnerFunctionName innerFunctionName = InnerFunctionName
                    .valueOf(con.getPathToExecutable().toUpperCase());
            if (InnerFunctionCall.FUNCTION_CALLS.containsKey(innerFunctionName)) {
                callReturn.add(new CallReturnHolder(null,
                        innerFunctionName + ": " + innerFunctionName.getDescription(), null));
                if (isExecuteProcess)
                    InnerFunctionCall.FUNCTION_CALLS.get(innerFunctionName).call(selections, con);
            } else {
                // function name isn't implemented
                callReturn.add(new CallReturnHolder(null, innerFunctionName + ": "
                        + innerFunctionName.getDescription() + " --> is not implemented yet!", null));
            }
        } catch (IllegalArgumentException exception) {
            // function name doesn't exist
            exception.printStackTrace();
        }
    }
}
