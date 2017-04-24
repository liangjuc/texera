package edu.uci.ics.textdb.exp.source.file;

import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.field.IDField;
import edu.uci.ics.textdb.api.field.TextField;
import edu.uci.ics.textdb.api.tuple.Tuple;
import edu.uci.ics.textdb.exp.source.file.AbstractSourceOperator;
import edu.uci.ics.textdb.exp.source.file.FileSourcePredicate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PDFSourceOperator reads a file or files under a directory and converts one file to one tuple.
 * <p>
 * The filePath in the predicate must be 1) a text file or 2) a directory
 * <p>
 * In case of a directory, PDFSourceOperator supports recursively reading files
 * and specifying a max recursive depth.
 * <p>
 * The files must have one of the supported extensions: {@code supportedExtensions}
 * <p>
 * PDFSourceOperator reads all content of one file and convert them to one tuple.
 * The tuple will have one column, the attributeName as defined in {@code PDFSourcePredicate},
 * with the attributeType as TEXT.
 * <p>
 * In case of a directory, if the directory doesn't contain any file that
 * matches the allowed extensions, then an exception will be thrown.
 *
 * @author Jun Ma
 */
public class PDFSourceOperator extends AbstractSourceOperator {

    private Iterator<Path> pathIterator;

    public PDFSourceOperator(FileSourcePredicate predicate) {
        super(predicate);

        this.pathList = pathList.stream()
                .filter(path -> isExtensionAllowed(predicate.getAllowedExtensions(), path))
                .collect(Collectors.toList());

        if (pathList.isEmpty()) {
            // TODO: change it to TextDB RuntimeException
            throw new RuntimeException(String.format(
                    "the filePath: %s doesn't contain any valid text files. " +
                            "File extension must be one of %s .",
                    predicate.getFilePath(), this.predicate.getAllowedExtensions()));
        }
        pathIterator = pathList.iterator();
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
