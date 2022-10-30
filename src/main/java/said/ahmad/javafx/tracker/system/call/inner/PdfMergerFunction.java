package said.ahmad.javafx.tracker.system.call.inner;

import javafx.application.Platform;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.pdf.PdfMerger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class PdfMergerFunction implements CallBackContext {

	@Override
	public void call(List<PathLayer> selections, UserContextMenu con) {
		try {
			File targetFile = null;
			int i = 1;
			Platform.runLater(() -> DialogHelper.showWaitingScreen("Merging PDF", "Merging pdfs..."));
			do {
				// later get name from input option in context menus
				targetFile = selections.get(0).getParentPath().resolve("AIO_PDF_"+i+".pdf").toFileIfLocal();
				i++;
			} while (targetFile.exists());
			PdfMerger.mergePdfFiles(selections.stream().map(s -> s.toFileIfLocal()).collect(Collectors.toList()),
					targetFile);
			Platform.runLater(() -> DialogHelper.closeWaitingScreen());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
