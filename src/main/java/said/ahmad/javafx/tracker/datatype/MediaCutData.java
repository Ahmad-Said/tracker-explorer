package said.ahmad.javafx.tracker.datatype;

public class MediaCutData {
	private int Start;
	private int End;
	private String Title;

	public MediaCutData(double start, double end, String title) {
		Start = (int) start;
		End = (int) end;
		Title = title;
	}

	public MediaCutData(String start, String end, String title) throws NumberFormatException {
		Start = Integer.parseInt(start);
		End = Integer.parseInt(end);
		Title = title;
	}

	public int getStart() {
		return Start;
	}

	public void setStart(int start) {
		Start = start;
	}

	public int getEnd() {
		return End;
	}

	public void setEnd(int end) {
		End = end;
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}
}
