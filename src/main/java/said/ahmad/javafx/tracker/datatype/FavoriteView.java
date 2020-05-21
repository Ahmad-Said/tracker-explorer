package said.ahmad.javafx.tracker.datatype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import said.ahmad.javafx.tracker.system.file.PathLayer;

public class FavoriteView {
	private String title;
	private List<PathLayer> locations;

	/** Empty constructor */
	public FavoriteView() {
		title = "";
		locations = new ArrayList<>();
	}

	public FavoriteView(String title, List<PathLayer> locations) {
		this.title = title;
		this.locations = locations;
	}

	public FavoriteView(String title, PathLayer... locations) {
		this.title = title;
		this.locations = Arrays.asList(locations);
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the locations
	 */
	public List<PathLayer> getLocations() {
		return locations;
	}

	/**
	 * @param locations the locations to set
	 */
	public void setLocations(List<PathLayer> locations) {
		this.locations = locations;
	}

	@Override
	public String toString() {
		return "FavoriteView [title=" + title + ", locations=" + locations + "]";
	}

}
