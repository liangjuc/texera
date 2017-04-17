package edu.uci.ics.textdb.dataflow.source;

import edu.uci.ics.textdb.api.dataflow.ISourceOperator;
import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.tuple.Tuple;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by junm5 on 4/16/17.
 */
public class PDFSourceOperator implements ISourceOperator {
    @FunctionalInterface
    public static interface ToTuple {
        Tuple convertToTuple(String str) throws Exception;
    }

    private Iterator<String> filePaths;
    private ToTuple toTupleFunc;
    private Schema outputSchema;
    private PDDocument pdDocument;

    public PDFSourceOperator(List<String> filePaths, ToTuple toTupleFunc, Schema schema) {
        this.filePaths = filePaths.iterator();
        this.toTupleFunc = toTupleFunc;
        this.outputSchema = schema;
    }


    @Override
    public void open() throws TextDBException {}

    @Override
    public Tuple getNextTuple() throws TextDBException {
        while (filePaths.hasNext()) {
            String path = filePaths.next();
            try {
                File file = new File(path);
                pdDocument = PDDocument.load(file);
                String text = new PDFTextStripper().getText(pdDocument);
                return this.toTupleFunc.convertToTuple(text);
            } catch (Exception exp) {
                exp.printStackTrace();
            } finally {
                close();
            }
        }
        return null;
    }

    @Override
    public void close() throws TextDBException {
        if (pdDocument != null) {
            try {
                pdDocument.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new TextDBException(e.getMessage());
            }
        }
    }

    @Override
    public Schema getOutputSchema() {
        return this.outputSchema;
    }
}
