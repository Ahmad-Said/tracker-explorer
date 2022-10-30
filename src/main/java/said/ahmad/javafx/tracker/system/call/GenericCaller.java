package said.ahmad.javafx.tracker.system.call;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.system.call.inner.CallBackContext;
import said.ahmad.javafx.tracker.system.call.inner.FunctionName;
import said.ahmad.javafx.tracker.system.call.inner.InnerFunctionCall;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.PathLayerHelper;

public class GenericCaller {

	/**
	 * @param source
	 * @param prefix
	 * @param suffix
	 * @return temporary created file containing list of absolute paths, one on each
	 *         line.
	 * @throws IOException
	 */
	public static File getTempFileList(List<PathLayer> source, String prefix, String suffix) throws IOException {
		return getTempFileList(source, false, prefix, suffix);
	}

	/**
	 * 
	 * @param source
	 * @param useNamesOnly
	 *            <code>true</code> will print name of file only. <br>
	 *            <code>false</code> will print absolute path of file
	 * @param prefix
	 * @param suffix
	 * @return temporary created file containing info of files, one file on each
	 *         line.
	 * @throws IOException
	 */
	public static File getTempFileList(List<PathLayer> source, boolean useNamesOnly, String prefix, String suffix)
			throws IOException {
		File tempFileList = File.createTempFile(prefix, suffix);
		// was using UTF 8 here but give problem with arabic letter since windows do not
		// use it as default!
		OutputStreamWriter p = new OutputStreamWriter(new FileOutputStream(tempFileList), Charset.defaultCharset());
		for (PathLayer path : source) {
			if (useNamesOnly) {
				p.write(path.getName() + "\n");
			} else {
				p.write(path.toString() + "\n");
			}
		}
		p.close();
		tempFileList.deleteOnExit();
		return tempFileList;
	}

	/**
	 * Execute Command contained in UserContextMenu on the provided list.<br>
	 * This fucntion does not check if context menu is compatible with the list.<br>
	 * User {@link UserContextMenu#isCompatibleWithList(List)} to do so.
	 * 
	 * @param selections
	 * @param con
	 * @return
	 * @throws IOException
	 */
	public static Process call(List<PathLayer> selections, UserContextMenu con) throws IOException {
		Process p = null;
		
		/**
		 * 	optional to do later if it is worth combining all list of files in case of recursive mode and use most common root
		 * 	{@link Path#relativize}.
		 * 	Currently working in single directory and working directory is same as this directory.
 		 */
		boolean isUsingRelatifPath = con.isCallUsingRelatifPath()
									 && PathLayerHelper.getParentsPathsAsString(selections.stream()).size() == 1;
		switch (con.getCallMethod()) {
			case COMBINED_CALL :
				StringBuilder combinedCallCMD = new StringBuilder();
				combinedCallCMD.append(con.getPathToExecutableAsPath().toString()).append(" ")
						.append(con.getPrefixCommandOptionsEvaluated(selections, null));

				selections.stream().map(path -> isUsingRelatifPath ? path.getName() : path.toString())
						.forEach(entry -> combinedCallCMD.append(" \"").append(entry).append("\""));

				combinedCallCMD.append(" ").append(con.getPostfixCommandOptionsEvaluated(selections, null));
				p = Runtime.getRuntime().exec(combinedCallCMD.toString(), null,
						selections.get(0).getParentPath().toFileIfLocal());
				break;

			case SEPARATE_CALL :
				for (PathLayer selection : selections) {
					StringBuilder singleCallCMD = new StringBuilder();
					singleCallCMD.append(con.getPathToExecutableAsPath().toString()).append(" ")
							.append(con.getPrefixCommandOptionsEvaluated(selections, selection));

					singleCallCMD.append("\"")
							.append(isUsingRelatifPath ? 
									selection.getName():
									selection.toString())
							.append("\"");
				
					singleCallCMD.append(" ").append(con.getPostfixCommandOptionsEvaluated(selections, selection));
					p = Runtime.getRuntime().exec(singleCallCMD.toString(), null,
							selection.getParentPath().toFileIfLocal());
				}
				break;

			case TXT_FILE_CALL :
				StringBuilder txtFileCallCMD = new StringBuilder();
				txtFileCallCMD.append(con.getPathToExecutableAsPath().toString()).append(" ")
						.append(con.getPrefixCommandOptionsEvaluated(selections, null));

				File tempFileList = getTempFileList(selections, isUsingRelatifPath,"contextTracker", ".txt");
				txtFileCallCMD.append("\"").append(tempFileList).append("\"");
				txtFileCallCMD.append(" ").append(con.getPostfixCommandOptionsEvaluated(selections, null));
				p = Runtime.getRuntime().exec(txtFileCallCMD.toString(), null,
						selections.get(0).getParentPath().toFileIfLocal());
				break;
			case INNER_FUNCTION:
				try {
					FunctionName functionName = FunctionName.valueOf(con.getPathToExecutable().toUpperCase());
					if(InnerFunctionCall.getFunctionCalls().containsKey(functionName)){
						// function name isn't implemented
						InnerFunctionCall.getFunctionCalls().get(functionName).call(selections, con);
					}
				} catch (IllegalArgumentException exception){
					// function name doesn't exist
					exception.printStackTrace();
				}
				break;
		}
		return p;
	}
}
