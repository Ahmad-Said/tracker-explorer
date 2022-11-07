package said.ahmad.javafx.tracker.system.call;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.system.call.inner.InnerFunctionCall;
import said.ahmad.javafx.tracker.system.call.inner.InnerFunctionName;
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
	public static List<CallReturnHolder> call(List<PathLayer> selections, UserContextMenu con, boolean executeProcess)
			throws IOException {
		List<CallReturnHolder> callReturn = new ArrayList<>();
		
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
				Process pComb = null;
				File workingDirComb = selections.get(0).getParentPath().toFileIfLocal();
				if (executeProcess)
					pComb = Runtime.getRuntime().exec(combinedCallCMD.toString(), null, workingDirComb);
				callReturn.add(new CallReturnHolder(pComb, combinedCallCMD.toString(), workingDirComb));
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
					Process pSing = null;
					File workingDirSing = selection.getParentPath().toFileIfLocal();
					if (executeProcess)
						pSing = Runtime.getRuntime().exec(singleCallCMD.toString(), null, workingDirSing);

					callReturn.add(new CallReturnHolder(pSing, singleCallCMD.toString(), workingDirSing));
				}
				break;

			case TXT_FILE_CALL :
				StringBuilder txtFileCallCMD = new StringBuilder();
				txtFileCallCMD.append(con.getPathToExecutableAsPath().toString()).append(" ")
						.append(con.getPrefixCommandOptionsEvaluated(selections, null));

				File tempFileList = getTempFileList(selections, isUsingRelatifPath,"contextTracker", ".txt");
				txtFileCallCMD.append("\"").append(tempFileList).append("\"");
				txtFileCallCMD.append(" ").append(con.getPostfixCommandOptionsEvaluated(selections, null));
				Process pTxt = null;
				File workingDirTxt = selections.get(0).getParentPath().toFileIfLocal();
				if (executeProcess)
					pTxt = Runtime.getRuntime().exec(txtFileCallCMD.toString(), null, workingDirTxt);
				callReturn.add(new CallReturnHolder(pTxt, txtFileCallCMD.toString(), workingDirTxt));
				break;
			case INNER_FUNCTION:
				try {
					InnerFunctionName innerFunctionName = InnerFunctionName
							.valueOf(con.getPathToExecutable().toUpperCase());
					if (InnerFunctionCall.FUNCTION_CALLS.containsKey(innerFunctionName)) {
						callReturn.add(new CallReturnHolder(null,
								innerFunctionName + ": " + innerFunctionName.getDescription(), null));
						if (executeProcess)
							InnerFunctionCall.FUNCTION_CALLS.get(innerFunctionName).call(selections, con);
					} else {
						// function name isn't implemented
						callReturn.add(new CallReturnHolder(null, innerFunctionName + ": "
								+ innerFunctionName.getDescription() + " --> is not implemented yet!", null));
					}
				} catch (IllegalArgumentException exception){
					// function name doesn't exist
					exception.printStackTrace();
				}
				break;
		}
		return callReturn;
	}
}
