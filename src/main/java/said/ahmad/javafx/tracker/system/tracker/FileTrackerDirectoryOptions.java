package said.ahmad.javafx.tracker.system.tracker;

import lombok.Getter;
import lombok.Setter;
import said.ahmad.javafx.tracker.datatype.DirectoryViewOptions;
import said.ahmad.javafx.tracker.system.tracker.FileTracker.CommandOption;

@Getter @Setter
public class FileTrackerDirectoryOptions extends FileTrackerHolder {
	public static final String OPTION_NAME = "DirectoryOptions.tracker";
	DirectoryViewOptions directoryViewOptions;

	public FileTrackerDirectoryOptions() {
		setName(OPTION_NAME);
	}
	public FileTrackerDirectoryOptions(DirectoryViewOptions directoryViewOptions) {
		setName(OPTION_NAME);
		this.directoryViewOptions = directoryViewOptions;
	}

	@Override
	public boolean isForDisplayRecord() {
		return false;
	}

	@Override
	public boolean isVirtualOption() {
		return true;
	}

	@Override
	public String toString() {
		return getName() + ">" + (isSeen() != null && isSeen() ? 1 : 0) + ">" + getNoteText()
				+ ">|" + CommandOption.DirectoryOptions + ">" + directoryViewOptions.toJSONString()
				+ "\r\n";
	}
}
