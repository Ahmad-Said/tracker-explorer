package said.ahmad.javafx.tracker.system.call;

/**
 * Used in case of multiple selection files to precise how to call the
 * executable.
 */
public enum CallMethod {

	/**
	 * One process is created calling on all files separated by space and surrounded
	 * by double quotes: one for all files
	 */
	COMBINED_CALL,

	/**
	 * As many as process is created: one for each file
	 */
	SEPARATE_CALL,

	/**
	 * Create a temporary text files containing the paths of all files and pass it
	 * as argument in command instead of using plain text. <br>
	 * This is useful when you have large list of files and avoid reaching maximum
	 * limit of characters in command line <br>
	 * <b>The executable file must support this option such as TeraCopy</b>
	 */
	TXT_FILE_CALL,

	/**
	 * Inner programme function call, in this case path to executable file will be a map from string to function
	 * call using interface
	 */
	INNER_FUNCTION
}
