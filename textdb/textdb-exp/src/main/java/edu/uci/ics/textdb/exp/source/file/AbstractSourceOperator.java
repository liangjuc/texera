package edu.uci.ics.textdb.exp.source.file;

import edu.uci.ics.textdb.api.constants.SchemaConstants;
import edu.uci.ics.textdb.api.dataflow.ISourceOperator;
import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.schema.Attribute;
import edu.uci.ics.textdb.api.schema.AttributeType;
import edu.uci.ics.textdb.api.schema.Schema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by junm5 on 4/24/17.
 */
public abstract class AbstractSourceOperator implements ISourceOperator {

    // cursor indicating the current position
    protected Integer cursor = CLOSED;
    protected final Schema outputSchema;
    protected FileSourcePredicate predicate;
    protected List<Path> pathList;
    protected Iterator<Path> pathIterator;


    public AbstractSourceOperator(FileSourcePredicate predicate) {
        this.predicate = predicate;
        this.outputSchema = new Schema(SchemaConstants._ID_ATTRIBUTE, new Attribute(predicate.getAttributeName(), AttributeType.TEXT));
        this.pathList = new ArrayList<>();

        Path filePath = Paths.get(predicate.getFilePath());
        if (!Files.exists(filePath)) {
            throw new RuntimeException(String.format("file %s doesn't exist", filePath));
        }

        if (Files.isDirectory(filePath)) {
            try {
                if (this.predicate.isRecursive()) {
                    pathList.addAll(Files.walk(filePath, this.predicate.getMaxDepth()).collect(Collectors.toList()));
                } else {
                    pathList.addAll(Files.list(filePath).collect(Collectors.toList()));
                }

            } catch (IOException e) {
                throw new RuntimeException(String.format(
                        "opening directory %s failed: " + e.getMessage(), filePath));
            }
        } else {
            pathList.add(filePath);
        }

        this.pathList = pathList.stream()
                .filter(path -> !Files.isDirectory(path))
                .filter(path -> !path.getFileName().startsWith("."))
                .filter(path -> isExtensionAllowed(predicate.getAllowedExtensions(), path))
                .collect(Collectors.toList());

        // check if the path list is empty
        if (pathList.isEmpty()) {
            // TODO: change it to TextDB RuntimeException
            throw new RuntimeException(String.format(
                    "the filePath: %s doesn't contain any valid text files. " +
                            "File extension must be one of %s .",
                    filePath, predicate.getAllowedExtensions()));
        }
        pathIterator = pathList.iterator();
    }

    /*
      * A helper function that returns if the file's extension is supported.
      * The extensions are expected to NOT have the dot "." in the string.
      * For example, extensions may contain "txt", but not ".txt"
      */
    protected static boolean isExtensionAllowed(List<String> allowedExtensions, Path path) {
        return allowedExtensions.stream()
                .map(ext -> "." + ext)
                .filter(ext -> path.getFileName().toString().toLowerCase().endsWith(ext))
                .findAny().isPresent();
    }

    @Override
    public void open() throws TextDBException {
        if (cursor != CLOSED) {
            return;
        }
        cursor = OPENED;
    }

    @Override
    public void close() throws TextDBException {
        if (cursor == CLOSED) {
            return;
        }
        cursor = CLOSED;
    }

    @Override
    public Schema getOutputSchema() {
        return outputSchema;
    }

    public FileSourcePredicate getPredicate() {
        return this.predicate;
    }

}
