package said.ahmad.javafx.tracker.system.call;

/**
 * @see CommandVariableAffector
 */
public enum CommandVariable {
	/**
	 * Used to get full path of selected file
	 */
	PATH,
	/**
	 * Used to get basename (without extension) of selected file
	 */
	BASENAME,
	/**
	 * Used to get the name of selected file
	 */
	NAME,
	/**
	 * Used to get extension of selected file
	 */
	EXTENSION,
	/**
	 * Used to get full path of parent directory of the selected file
	 */
	PARENT_PATH,
	/**
	 * Used to get name of parent directory of the selected file
	 */
	PARENT_NAME,
	/**
	 * Used to get the number of selected files
	 */
	FILES_COUNT,
	/**
	 * @TODO make pdf ask for a name
	 * Used to ask user for input like output file name
	 * Convention example %input:label:placeholder%
	 * Example of usage: "%input:output pdf file%"
	 * will ask user with a prompt of input string with label: "output pdf file"
	 * then replace value instead of the variable.
	 * replacement is done from UI/Controller side
	 */
	USER_INPUT;

	public static String getInfoFormat(CommandVariable command) {
		// only define description if implementation of CommandVariableAffector is done
		switch (command) {
			case PATH :
				return "Used to get full path of selected file";
			case BASENAME :
				return "Used to get basename (without extension) of selected file";
			case NAME :
				return "Used to get the name of selected file";
			case EXTENSION :
				return "Used to get extension of selected file";
			case PARENT_PATH :
				return "Used to get full path of parent directory of the selected file";
			case PARENT_NAME :
				return "Used to get name of parent directory of the selected file";
			case FILES_COUNT:
				return "Used to get the number of selected files";
			default:
				return null;
		}
	}

	public static CommandVariable valueOfIgnoringCase(String command) {
		return CommandVariable.valueOf(command.toUpperCase());
	}
}
