package application;

import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

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
		return FilenameUtils.getExtension(fileName).toUpperCase();
	}
	
	public static String getBaseName(String fileName) {
		return FilenameUtils.getBaseName(fileName);
	}

	public static String[] getWords(String text) {
		return text.toLowerCase().split("\\W+");
	}
}
