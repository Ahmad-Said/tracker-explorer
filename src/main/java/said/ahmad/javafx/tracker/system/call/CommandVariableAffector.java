package said.ahmad.javafx.tracker.system.call;

import org.jetbrains.annotations.Nullable;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.util.ArrayListHelper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandVariableAffector {

	/**
	 * Will replace each command variable with its corresponding value. General
	 * Usage: %iCommandOption% where <br>
	 * - "i" is a relative number pointing to the file from the selection. <br>
	 * 
	 * When "i" is not present, will use parameter priorityPath if not null,
	 * otherwise the first element in selections.
	 *
	 * Note: By default as implemented in {@link GenericCaller} it depend on call method:<br>
	 * <ul>
	 * <li>{@link CallMethod#COMBINED_CALL} use the first file of selections (as it
	 * is one command)</li>
	 * <li>{@link CallMethod#SEPARATE_CALL} use the called file respectively (as it
	 * is separate command for each file)</li>
	 * </ul>
	 *
	 * - Command Option is the string to extract from the file.<br>
	 * Example: %1BASENAME% will be affected with first file name without extension.
	 * 
	 * @param selections
	 *            files selections to work with
	 * @param priorityPath
	 *            A path to be used by default if no index specification is present in command
	 *            (usually the called file)
	 * @param command
	 *            String command containing %CommandVariable%
	 * @return Evaluated Command replacing all variable by their values
	 * @see CommandVariable
	 * @see ArrayListHelper#getCyclicIndex(int, int)
	 */
	public static String getEvaluatedCommand(List<PathLayer> selections, @Nullable PathLayer priorityPath, String command) {
		if (command == null || command.isEmpty())
			return "";
		Pattern p = Pattern.compile("%(-?([0-9]*))(.*?)%");
		Matcher m = p.matcher(command);
		StringBuilder evaluatedCommand = new StringBuilder();
		int lastIndex = 0;
		while (m.find()) {
			PathLayer usedFileForEvaluation;
			if (!m.group(2).isEmpty()) {
				int index = Integer.parseInt(m.group(1));
				index = ArrayListHelper.getCyclicIndex(index, selections.size());
				usedFileForEvaluation = selections.get(index);
			} else if (priorityPath != null) {
				usedFileForEvaluation = priorityPath;
			} else {
				usedFileForEvaluation = selections.get(0);
			}

			CommandVariable commandVariable = CommandVariable.valueOfIgnoringCase(m.group(3));
			evaluatedCommand.append(command, lastIndex, m.start());
			CommandVariable test = CommandVariable.BASENAME;
			lastIndex = m.end();
			switch (commandVariable) {
				case PATH :
					evaluatedCommand.append(usedFileForEvaluation.toString());
					break;
				case BASENAME :
					evaluatedCommand.append(usedFileForEvaluation.getBaseName());
					break;
				case NAME :
					evaluatedCommand.append(usedFileForEvaluation.getName());
					break;
				case EXTENSION :
					evaluatedCommand.append(usedFileForEvaluation.getExtension());
					break;
				case PARENT_PATH :
					evaluatedCommand.append(usedFileForEvaluation.getParent());
					break;
				case PARENT_NAME :
					evaluatedCommand.append(usedFileForEvaluation.getParentPath().getName());
					break;
			}
		}
		if (lastIndex < command.length()) {
			evaluatedCommand.append(command, lastIndex, command.length());
		}

		return evaluatedCommand.toString();
	}
}
