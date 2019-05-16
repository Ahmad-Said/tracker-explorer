package application;

import java.nio.file.Path;

public class StringHelper {

	public static Path InitialLeftPath;
	public static Path InitialRightPath;

	public static boolean containsWord(String text, String word) {
		String[] words = getWords(text);
		word = word.toLowerCase();
		for (String item : words) {
			if (item.equals(word))
				return true;
		}
		return false;
	}
	
	public static String getExtention(String fileName) {
		int index = fileName.lastIndexOf('.') + 1;
		if (index >= 0 )
			return fileName.substring(index).toUpperCase();
		return "";
	}
	public static String getBaseName(String fileName) {
		int index = fileName.lastIndexOf('.');
		if (index >= 0)
			return fileName.substring(0,index).toUpperCase();
		return fileName;
	}

	public static String[] getWords(String text) {
		return text.toLowerCase().split("\\W+");
	}
}
