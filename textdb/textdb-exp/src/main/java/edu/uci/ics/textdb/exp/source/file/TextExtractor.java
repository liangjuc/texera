package edu.uci.ics.textdb.exp.source.file;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by junm5 on 5/3/17.
 */
public class TextExtractor {

    public static String extractCommonFile(Path path) {
        if (path == null) {
            return null;
        }
        try {
            return new String(Files.readAllBytes(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String extractPDFFile(Path path) {
        if (path == null) {
            return null;
        }
        PDDocument doc = null;
        try {
            doc = PDDocument.load(new File(path.toString()));
            return new PDFTextStripper().getText(doc);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (doc != null) {
                try {
                    doc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
