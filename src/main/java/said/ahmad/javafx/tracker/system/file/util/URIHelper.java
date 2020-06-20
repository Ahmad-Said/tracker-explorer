package said.ahmad.javafx.tracker.system.file.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.jetbrains.annotations.Nullable;

public class URIHelper {

	public static URI getParent(URI uri) {
		if (getNameCount(uri) == 0) {
			return null;
		}
		URI parent = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
		return parent;
	}

	public static int getNameCount(URI uri) {
		return uri.getPath().split("/").length;
	}

	@Nullable
	public static URI resolveURI(URI baseURI, String resolvedPath, String encoding) {
		if (encoding == null) {
			encoding = "UTF-8";
		}
		try {
			return baseURI.resolve(URLEncoder.encode(resolvedPath, encoding));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static URI encodeToURI(String uri) throws URISyntaxException {
		return new URI(uri.replaceAll(" ", "%20"));
	}

}
