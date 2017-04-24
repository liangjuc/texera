package edu.uci.ics.textdb.exp.source.file;

import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.field.IDField;
import edu.uci.ics.textdb.api.field.TextField;
import edu.uci.ics.textdb.api.tuple.Tuple;

import java.nio.file.Files;

/**
 * FileSourceOperator reads a file or files under a directory and converts one file to one tuple.
 * <p>
 * The filePath in the predicate must be 1) a text file or 2) a directory
 * <p>
 * In case of a directory, FileSourceOperator supports recursively reading files
 * and specifying a max recursive depth.
 * <p>
 * The files must have one of the supported extensions: {@code supportedExtensions}
 * <p>
 * FileSourceOperator reads all content of one file and convert them to one tuple.
 * The tuple will have one column, the attributeName as defined in {@code FileSourcePredicate},
 * with the attributeType as TEXT.
 * <p>
 * In case of a directory, if the directory doesn't contain any file that
 * matches the allowed extensions, then an exception will be thrown.
 *
 * @author Zuozhi Wang
 * @author Jun Ma
 */
public class FileSourceOperator extends AbstractSourceOperator {

    public FileSourceOperator(FileSourcePredicate predicate) {
        super(predicate);
    }

    @Override
    public Tuple getNextTuple() throws TextDBException {
        if (cursor == CLOSED || cursor >= pathList.size()) {
            return null;
        }
        // keep iterating until
        //   1) a file is converted to a tuple successfully
        //   2) the cursor reaches the end
        while (pathIterator.hasNext()) {
            try {
                String content = new String(Files.readAllBytes(pathIterator.next()));
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
}
