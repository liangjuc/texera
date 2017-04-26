package edu.uci.ics.textdb.exp.source.file;

import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.field.IDField;
import edu.uci.ics.textdb.api.field.TextField;
import edu.uci.ics.textdb.api.tuple.Tuple;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

/**
 * PDFFileSourceOperator reads a file or files under a directory and converts one file to one tuple.
 * <p>
 * The filePath in the predicate must be 1) a text file or 2) a directory
 * <p>
 * In case of a directory, PDFFileSourceOperator supports recursively reading files
 * and specifying a max recursive depth.
 * <p>
 * The files must have one of the supported extensions: {@code supportedExtensions}
 * <p>
 * PDFFileSourceOperator reads all content of one file and convert them to one tuple.
 * The tuple will have one column, the attributeName as defined in {@code PDFSourcePredicate},
 * with the attributeType as TEXT.
 * <p>
 * In case of a directory, if the directory doesn't contain any file that
 * matches the allowed extensions, then an exception will be thrown.
 *
 * @author Jun Ma
 */
public class PDFFileSourceOperator extends AbstractFileSourceOperator {

    public PDFFileSourceOperator(FileSourcePredicate predicate) {
        super(predicate);
    }


    @Override
    public Tuple getNextTuple() throws TextDBException {
        if (cursor == CLOSED) {
            return null;
        }
        while (pathIterator.hasNext()) {
            try {
                String path = pathIterator.next().toString();
                String content = readPDFDocument(path);
                // and assign a random ID to it
                Tuple tuple = null;
                if (content != null) {
                    tuple = new Tuple(outputSchema, IDField.newRandomID(), new TextField(content));
                }
                cursor++;
                return tuple;
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }
        return null;
    }

    private static String readPDFDocument(String path) {
        PDDocument doc = null;
        try {
            doc = PDDocument.load(new File(path));
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
