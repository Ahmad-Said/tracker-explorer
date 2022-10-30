package said.ahmad.javafx.tracker.system.pdf;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class PdfMerger {

    /**
     *
     * @param filesToMerge
     * @param outputCombinedFile
     */
    public static void mergePdfFiles(List<File> filesToMerge, File outputCombinedFile) throws IOException {
        // Instantiating PDFMergerUtility class
        PDFMergerUtility obj = new PDFMergerUtility();
        // Setting the destination file path
        obj.setDestinationFileName(outputCombinedFile.toString());

        //  Add all source files, to be merged
        for (File file : filesToMerge) {
            obj.addSource(createInnerBookmarkedPDF(file));
        }
        // Merging documents
        obj.mergeDocuments(null);
    }

    /**
     * Create bookmark of file name having inner bookmark as it was before.
     *
     * created file will have a temporary file name and deteled on exit.<br>
     *
     * example: my_file.pdf having as bookmarks
     * <ul>
     *     <li> 1. firstPage </li>
     *     <li> 2. secondPage
     *     <ul>
     *         <li>
     *             2.1 inner second page
     *         </li>
     *         <li>
     *             2.2 inner second page
     *         </li>
     *     </ul>
     *     </li>
     *     <li> 3. ThirdPage </li>
     * </ul>
     *
     * New Created file bookmark will be:
     * <ul>
     *     <li>my File name
     *     <ul>
     *         <li> 1. firstPage </li>
     *         <li> 2. secondPage
     *         <ul>
     *             <li>
     *                 2.1 inner second page
     *             </li>
     *             <li>
     *                 2.2 inner second page
     *             </li>
     *         </ul>
     *         </li>
     *         <li> 3. ThirdPage </li>
     *     </ul>
     *     </li>
     * </ul>
     * @param srcFile
     * @return
     * @throws IOException
     */
    public static File createInnerBookmarkedPDF(File srcFile) throws IOException {
        File tempPDFFile = File.createTempFile(FilenameUtils.getBaseName(srcFile.getName()), ".pdf");
        tempPDFFile.deleteOnExit();
        PDFParser parser = new PDFParser(new RandomAccessReadBuffer(new FileInputStream(srcFile)));
        PDDocument document = parser.parse();

        PDDocumentOutline documentOutlineOriginal = document.getDocumentCatalog().getDocumentOutline();
        PDDocumentOutline documentOutline = new PDDocumentOutline();
        document.getDocumentCatalog().setDocumentOutline(documentOutline);
        PDOutlineItem singleFileOutline = new PDOutlineItem();
        singleFileOutline.setTitle(FilenameUtils.getBaseName(srcFile.getName()));
        singleFileOutline.setDestination(document.getPage(0));
        documentOutline.addLast(singleFileOutline);

        if (documentOutlineOriginal!= null && documentOutlineOriginal.hasChildren()) {
            for (PDOutlineItem item : documentOutlineOriginal.children()) {
                PDOutlineItem bookmark = new PDOutlineItem();
                bookmark.setTitle(item.getTitle());
                bookmark.setDestination(item.getDestination());
                bookmark.setAction(item.getAction());
                bookmark.setBold(item.isBold());
                bookmark.setItalic(item.isItalic());
                bookmark.setTextColor(item.getTextColor());
                singleFileOutline.addLast(bookmark);
            }
        }


        document.save(tempPDFFile);

        return tempPDFFile;
    }
}
