package said.ahmad.javafx.tracker.system.call.inner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.system.call.CallMethod;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.pdf.PdfMerger;

public class PdfMergerFunction implements CallBackContext {

	@Override
	public void call(List<PathLayer> selections, UserContextMenu con) {
		try {
			PathLayer parent = selections.get(0).getParentPath();
			String parentName = parent.getName();
			String combinedPdfBaseName = "AIO_" + parentName;
			File targetFile = parent.resolve(combinedPdfBaseName + ".pdf").toFileIfLocal();
			Platform.runLater(() -> DialogHelper.showWaitingScreen("Merging PDF", "Merging pdfs..."));
			int i = 2;
			while (targetFile.exists()) {
				// later get name from input option in context menus
				targetFile = selections.get(0).getParentPath().resolve(combinedPdfBaseName + "_" + i + ".pdf")
						.toFileIfLocal();
				i++;
			}
			PdfMerger.mergePdfFiles(selections.stream().map(s -> s.toFileIfLocal()).collect(Collectors.toList()),
					targetFile);
			Platform.runLater(() -> DialogHelper.closeWaitingScreen());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public UserContextMenu createDefaultUserContextMenu() {
		UserContextMenu mergePdf = new UserContextMenu();
		mergePdf.setMenuOrder(2);
		mergePdf.setPathToExecutable(InnerFunctionName.MERGE_PDF.toString());

		mergePdf.getExtensions().add("pdf");

		mergePdf.setOnSingleSelection(false);
		mergePdf.setOnMultipleSelection(true);
		mergePdf.setAliasMultiple("Merge PDF");
		mergePdf.setParentMenuNames("Open With");

		mergePdf.setIconPath(UserContextMenu.INNER_ICON_CONVENTION + IconLoader.ICON_TYPE.MERGE_ARROW);

		mergePdf.setCallMethod(CallMethod.INNER_FUNCTION);
		return mergePdf;
	}
}
