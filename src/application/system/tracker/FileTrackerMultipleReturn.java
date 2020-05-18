package application.system.tracker;

import java.util.HashSet;

import application.system.file.PathLayer;

public class FileTrackerMultipleReturn {

	public HashSet<PathLayer> trackedList;
	public HashSet<PathLayer> unTrackedList;
	public boolean didTrackNewFolder;

	public FileTrackerMultipleReturn() {
		trackedList = new HashSet<PathLayer>();
		unTrackedList = new HashSet<PathLayer>();
		didTrackNewFolder = false;
	}

	@Override
	public String toString() {
		return "FileTrackerMultipleReturn [trackedList=" + trackedList + ", unTrackedList=" + unTrackedList
				+ ", didTrackNewFolder=" + didTrackNewFolder + "]";
	}

}
