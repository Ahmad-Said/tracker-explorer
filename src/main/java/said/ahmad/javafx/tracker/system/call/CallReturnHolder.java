package said.ahmad.javafx.tracker.system.call;

import lombok.Data;
import said.ahmad.javafx.tracker.app.StringHelper;

import java.io.File;
import java.util.List;

@Data
/**
 * Class that hold argument of function execute, also its returned process
 * @see Runtime#exec(String, String[], File)
 */
public class CallReturnHolder {
    /**
     * Process executing the call, Can be null.
     */
    private Process process;
	private String processStandardOutput;
	private String processErrorOutput;
    private String command;
    private File workingDir;

	public CallReturnHolder(Process process, String command, File workingDir) {
		this.process = process;
		this.command = command;
		this.workingDir = workingDir;
	}

	/**
	 * Generate standard and error output of the process if it exists
	 */
	public void generateProcessOutput() {
		generateProcessStandardOutput();
		generateProcessErrorOutput();
	}
	/**
	 * fill standard output from process
	 */
	private void generateProcessStandardOutput() {
		if (process == null)
			return;
		processStandardOutput = StringHelper.getProcessStandardOutput(process);
	}

	/**
	 * fill error output from process
	 */
	private void generateProcessErrorOutput() {
		if (process == null)
			return;
		processErrorOutput = StringHelper.getProcessErrorOutput(process);
	}

    /**
     * Concatenate all process standard / error output in one string
     * @param callReturnHolderList
     * @return concatenate string of all outputs
     */
	public static String getPrettyProcessOutputs(List<CallReturnHolder> callReturnHolderList) {
		int i = 1;
		StringBuilder stringBuilder = new StringBuilder();
		for (CallReturnHolder callReturnHolder : callReturnHolderList) {
			String callNumber = callReturnHolderList.size() > 1 ? " # " + i++ : "";
			String callUnderLine = !callNumber.isEmpty()
					? new String(new char[callNumber.length()]).replace('\0', '-')
					: "";
			stringBuilder.append("Standard Output Call").append(callNumber).append("\n");
			stringBuilder.append("--------------------").append(callUnderLine).append("\n");
			stringBuilder.append(callReturnHolder.getProcessStandardOutput());
			stringBuilder.append("\n");
			if (!StringHelper.isEmpty(callReturnHolder.getProcessErrorOutput())) {
				stringBuilder.append("Error Output Call").append(callNumber).append("\n");
				stringBuilder.append("-----------------").append(callUnderLine).append("\n");
				stringBuilder.append(callReturnHolder.getProcessErrorOutput());
				stringBuilder.append("\n");
			}
			stringBuilder.append("***************************************************");
			stringBuilder.append("\n");
		}
		return stringBuilder.toString();
	}

    /**
     * Count the number of error of call present in the list
     * @param callReturnHolderList
     * @return number of errors (1 error for call if present)
     */
	public static long getNumberOfErrors(List<CallReturnHolder> callReturnHolderList) {
		return callReturnHolderList.stream().filter(c -> !StringHelper.isEmpty(c.processErrorOutput)).count();
	}

}
