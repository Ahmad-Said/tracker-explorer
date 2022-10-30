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
	 * @TODO make pdf ask for a name
	 * Used to ask user for input like output file name
	 * Example of usage: "%input:output pdf file%"
	 * will ask user with a prompt of input string with label: "output pdf file"
	 * then replace value instead of the variable.
	 * replacement is done from UI/Controller side
	 */
	INPUT_COLON_DESCRIPTION;

	public static String getInfoFormat(CommandVariable command) {
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
			default:
				return "Command not yet defined!";
		}
	}

	public static CommandVariable valueOfIgnoringCase(String command) {
		return CommandVariable.valueOf(command.toUpperCase());
	}
}
