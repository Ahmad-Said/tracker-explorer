package application.datatype;

public class MediaCutData {
	private int Start;
	private int End;
	private String Title;

	public MediaCutData(double start, double end, String title) {
		this.Start = (int) start;
		this.End = (int) end;
		this.Title = title;
	}

	public MediaCutData(String start, String end, String title) {
		this.Start = Integer.parseInt(start);
		this.End = Integer.parseInt(end);
		this.Title = title;
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
