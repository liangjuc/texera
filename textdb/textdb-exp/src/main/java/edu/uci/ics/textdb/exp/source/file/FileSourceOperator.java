package edu.uci.ics.textdb.exp.source.file;

import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.field.IDField;
import edu.uci.ics.textdb.api.field.TextField;
import edu.uci.ics.textdb.api.tuple.Tuple;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    private List<String> allowedExtension = Arrays.asList("txt", "json", "xml", "csv", "html", "md");

    public FileSourceOperator(FileSourcePredicate predicate) {
        super(predicate);
        System.out.println(predicate.getAllowedExtensions());
        this.pathList = pathList.stream()
                .filter(path -> isExtensionAllowed(predicate.getAllowedExtensions(), path))
                .collect(Collectors.toList());

        // check if the path list is empty
        if (pathList.isEmpty()) {
            // TODO: change it to TextDB RuntimeException
            throw new RuntimeException(String.format(
                    "the filePath: %s doesn't contain any valid text files. " +
                            "File extension must be one of %s .",
                    predicate.getFilePath(), allowedExtension));
        }
    }

    @Override
    public Tuple getNextTuple() throws TextDBException {
        if (cursor == CLOSED || cursor >= pathList.size()) {
            return null;
        }
        // keep iterating until 
        //   1) a file is converted to a tuple successfully
        //   2) the cursor reaches the end
        while (cursor < pathList.size()) {
            try {
                String content = new String(Files.readAllBytes(pathList.get(cursor)));
                // create a tuple according to the string
                // and assign a random ID to it
                Tuple tuple = new Tuple(outputSchema, IDField.newRandomID(), new TextField(content));
                cursor++;
                return tuple;
            } catch (IOException e) {
                // if reading the current path fails, increment the cursor and continue
                e.printStackTrace();
            }
        }
        return null;
    }
}
