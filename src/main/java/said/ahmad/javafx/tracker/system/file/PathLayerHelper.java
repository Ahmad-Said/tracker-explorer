package said.ahmad.javafx.tracker.system.file;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import javafx.scene.input.DataFormat;
import said.ahmad.javafx.tracker.system.file.ftp.FTPPathLayer;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;
import said.ahmad.javafx.tracker.system.file.util.URIHelper;

public class PathLayerHelper {
	/**
	 * To make on drag and drop use this key and put {@link PathLayer#toURI()} in string
	 * format
	 */
	public static final String ON_DROP_URI_OPERATION_KEY = "ON_DROP_URI_OPERATION_KEY";
	public static final DataFormat DATA_FORMAT_KEY = new DataFormat("TRACKER_EXPLORER_PATH_LAYER");
	private static final String LINE_SEPARATOR = "\n";

	/** @see PathLayerHelper#parseGeneratedOnDropString(String) */
	public static String generateOnDropString(List<PathLayer> list) {
		StringBuilder onDropString = new StringBuilder();
		onDropString.append(ON_DROP_URI_OPERATION_KEY + LINE_SEPARATOR);
		for (PathLayer pathLayer : list) {
			onDropString.append(pathLayer.toURI() + LINE_SEPARATOR);
		}
		return onDropString.toString();
	}

	/**
	 * Error while parsing URI will be ignored
	 *
	 * @param string Containing {@link URI} as string separated by line
	 *               {@link #LINE_SEPARATOR}
	 * @return
	 * @see PathLayerHelper#generateOnDropString(List)
	 */
	public static List<PathLayer> parseGeneratedOnDropString(String string) {
		ArrayList<PathLayer> list = new ArrayList<>();
		String splitted[] = string.split(LINE_SEPARATOR);
		if (splitted.length > 0) {
			int i = 0;
			if (splitted[0].equals(ON_DROP_URI_OPERATION_KEY)) {
				i++;
			}
			for (; i < splitted.length; i++) {
				try {
					list.add(parseURI(splitted[i]));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}

		}
		return list;
	}

	/** return null if no match of any provider or fail to create one */
	@Nullable
	public static PathLayer parseURI(URI uri) {
		switch (uri.getScheme().toUpperCase()) {
		case "FTP":
			return FTPPathLayer.parseURI(uri);
		case "FILE":
			return new FilePathLayer(new File(uri));
		default:
			break;
		}
		return null;
	}

	public static PathLayer parseURI(String stringURI) throws URISyntaxException {
		PathLayer parsed = null;
		URI parsedURI = URIHelper.encodeToURI(stringURI);
		parsed = parseURI(parsedURI);
		return parsed;
	}

	public static Set<FilePathLayer> getParentsFiles(List<File> sonPaths) {
		return sonPaths.stream().filter(p -> p != null).map(p -> new FilePathLayer(p.toPath().getParent().toFile()))
				.collect(Collectors.toSet());
	}

	public static Set<PathLayer> getParentsPaths(List<? extends PathLayer> sonPaths) {
		return sonPaths.stream().filter(p -> p != null).map(p -> p.getParentPath()).collect(Collectors.toSet());
	}

	/**
	 * Stream parameter should not be terminated
	 *
	 * @param sonPaths
	 * @return a set parent path absolute string using {@link PathLayer#getParent()}
	 */
	public static Set<String> getParentsPathsAsString(Stream<? extends PathLayer> sonPaths) {
		return sonPaths.filter(p -> p != null).map(p -> p.getParent()).collect(Collectors.toSet());
	}

	public static Map<String, PathLayer> getAbsolutePathToPaths(List<PathLayer> listOfPaths) {
		Map<String, PathLayer> mapAbsoluteToPath = listOfPaths == null ? null
				: listOfPaths.stream().collect(Collectors.toMap(p -> p.getAbsolutePath(), p -> p));
		return mapAbsoluteToPath;
	}

	public static HashMap<PathLayer, List<PathLayer>> getParentTochildren(List<? extends PathLayer> sonsPaths) {
		HashMap<PathLayer, List<PathLayer>> parentToSons = new HashMap<>();
		sonsPaths.forEach(s -> {
			PathLayer parent = s.getParentPath();
			if (!parentToSons.containsKey(parent)) {
				parentToSons.put(parent, new ArrayList<PathLayer>());
			}
			parentToSons.get(parent).add(s);
		});
		return parentToSons;
	};

	/**
	 *
	 * @param start
	 * @param maxDepth
	 * @param includeHiddenFiles
	 * @param visitor
	 * @return
	 * @see Files#walkFileTree(java.nio.file.Path, Set, int,
	 *      java.nio.file.FileVisitor)
	 * @throws IOException
	 */
	public static PathLayer walkFileTree(PathLayer start, int maxDepth, boolean includeHiddenFiles,
			PathLayerVisitor<? super PathLayer> visitor) throws IOException {
		/**
		 * Create a FileTreeWalker to walk the file tree, invoking the visitor for each
		 * event.
		 */
		try (PathLayerTreeWalker walker = new PathLayerTreeWalker(maxDepth, includeHiddenFiles)) {
			PathLayerTreeWalker.Event ev = walker.walk(start);
			do {
				FileVisitResult result;
				switch (ev.type()) {
				case ENTRY:
					IOException ioe = ev.ioeException();
					if (ioe == null) {
						result = visitor.visitFile(ev.file());
					} else {
						result = visitor.visitFileFailed(ev.file(), ioe);
					}
					break;

				case START_DIRECTORY:
					result = visitor.preVisitDirectory(ev.file());

					// if SKIP_SIBLINGS and SKIP_SUBTREE is returned then
					// there shouldn't be any more events for the current
					// directory.
					if (result == FileVisitResult.SKIP_SUBTREE || result == FileVisitResult.SKIP_SIBLINGS) {
						walker.pop();
					}
					break;

				case END_DIRECTORY:
					result = visitor.postVisitDirectory(ev.file(), ev.ioeException());

					// SKIP_SIBLINGS is a no-op for postVisitDirectory
					if (result == FileVisitResult.SKIP_SIBLINGS) {
						result = FileVisitResult.CONTINUE;
					}
					break;

				default:
					throw new AssertionError("Should not get here");
				}

				if (Objects.requireNonNull(result) != FileVisitResult.CONTINUE) {
					if (result == FileVisitResult.TERMINATE) {
						break;
					} else if (result == FileVisitResult.SKIP_SIBLINGS) {
						walker.skipRemainingSiblings();
					}
				}
				ev = walker.next();
			} while (ev != null);
		}

		return start;
	}

	/**
	 * Evaluate and replace all occurrence of <code>%envVar%</code>  using {@link System#getenv(String)}
	 * @param path
	 * @return
	 */
	public static PathLayer evaluateEnvVariableInPath(String path){
		if (path == null) {
			return null;
		}
		String envVar;
		String evaluatedPath = path;
		Pattern p = Pattern.compile("%(.*?)%");
		Matcher m = p.matcher(evaluatedPath);
		while (m.find()) {
			envVar = System.getenv(m.group(1));
			if (envVar != null && !envVar.isEmpty())
				evaluatedPath = evaluatedPath.replace(m.group(), envVar);
		}
		return new FilePathLayer(new File(evaluatedPath));
	}
}
