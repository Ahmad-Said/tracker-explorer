package said.ahmad.javafx.tracker.system.tracker;

import java.util.ArrayList;

import said.ahmad.javafx.tracker.datatype.MediaCutData;
import said.ahmad.javafx.tracker.system.tracker.FileTracker.CommandOption;

/**
 * Used to hold File tracker data such as name, Seen status, notes...
 *
 */
public class FileTrackerHolder {

	/**
	 * Empty file tracker with null Seen/empty status Manually set seen if needed
	 *
	 * @param name
	 * @see #setSeen(Boolean)
	 */
	public FileTrackerHolder(String name) {
		this.name = name;
	}

	public FileTrackerHolder(String name, Boolean isSeen) {
		this.name = name;
		this.isSeen = isSeen;
	}

	/**
	 * Follow Convention of {@link FileTracker}
	 *
	 * @author user
	 *
	 */
	@Override
	public String toString() {
		return name + ">" + (isSeen != null && isSeen ? 1 : 0) + ">" + noteText
				+ (mediaCutDataUnPrased == null ? "" : mediaCutDataUnPrased)
				+ (timeToLive > 0 ? ">|" + CommandOption.TimeToLive + ">" + timeToLive : "") + "\r\n";
	}

	private String name;
	private Boolean isSeen;
	private String noteText = "";
	private int timeToLive = -1;
	private String mediaCutDataUnPrased;

	public String getName() {
		return name;
	}

	public FileTrackerHolder setName(String name) {
		this.name = name;
		return this;
	}

	public Boolean isSeen() {
		return isSeen;
	}

	public FileTrackerHolder setSeen(Boolean isSeen) {
		this.isSeen = isSeen;
		return this;
	}

	public FileTrackerHolder toggleSeen() {
		isSeen = !isSeen;
		return this;
	}

	public String getNoteText() {
		return noteText;
	}

	/**
	 *
	 * @param noteText Using char '>' or empty string in note if forbidden always
	 *                 clean note use, if not sure use:
	 *                 {@link #setNoteTextCleaned(String)}
	 * @return
	 */
	public FileTrackerHolder setNoteText(String noteText) {
		this.noteText = noteText;
		return this;
	}

	/**
	 * Auto remove any occurrence of '>'
	 *
	 * @param noteText
	 * @return
	 */
	public String setNoteTextCleaned(String noteText) {
		this.noteText = noteText.replaceAll(">", "_");
		return this.noteText;
	}

	/**
	 * <code>-1</code> is the default time to live
	 *
	 * @return
	 */
	public int getTimeToLive() {
		return timeToLive;
	}

	public FileTrackerHolder setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
		return this;
	}

	public FileTrackerHolder setTimeToLiveDefaultMax() {
		timeToLive = FileTracker.TIME_TO_LIVE_MAX;
		return this;
	}

	/**
	 * Format is >start1>end1>title>start2>end2>title <br>
	 * Use .substring(1).split(">") to get options
	 *
	 * @return
	 */
	public String getMediaCutDataUnPrased() {
		return mediaCutDataUnPrased == null ? "" : mediaCutDataUnPrased;
	}

	public ArrayList<MediaCutData> getMediaCutDataParsed() {
		if (getMediaCutDataUnPrased() == null || getMediaCutDataUnPrased().isEmpty()) {
			return new ArrayList<>();
		}
		String mediaCutData[] = getMediaCutDataUnPrased().substring(1).split(">");
		ArrayList<MediaCutData> listCutDatas = new ArrayList<MediaCutData>();
		for (int i = 0; i <= mediaCutData.length - 3; i++) {
			try {
				listCutDatas.add(new MediaCutData(mediaCutData[i], mediaCutData[++i], mediaCutData[++i]));
			} catch (Exception e) {
			}
		}
		return listCutDatas;
	}

	/**
	 * Format is >start1>end1>title>start2>end2>title
	 *
	 * @param mediaCutDataUnPrased
	 * @return
	 */
	public FileTrackerHolder concatMediaCutDataUnPrased(String mediaCutDataUnPrased) {
		if (this.mediaCutDataUnPrased == null) {
			this.mediaCutDataUnPrased = "";
		}
		this.mediaCutDataUnPrased += mediaCutDataUnPrased;
		return this;
	}

	/**
	 * Format is >start1>end1>title>start2>end2>title <br>
	 * Use ">"+{@linkplain String#join(CharSequence, Iterable)}
	 *
	 * @param mediaCutDataUnPrased
	 * @return
	 */
	public FileTrackerHolder setMediaCutDataUnPrased(String mediaCutDataUnPrased) {
		this.mediaCutDataUnPrased = mediaCutDataUnPrased;
		return this;
	}
}
