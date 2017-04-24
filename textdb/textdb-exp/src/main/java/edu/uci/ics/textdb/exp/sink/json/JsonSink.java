package edu.uci.ics.textdb.exp.sink.json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import edu.uci.ics.textdb.api.constants.ErrorMessages;
import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.api.dataflow.ISink;
import edu.uci.ics.textdb.api.exception.DataFlowException;
import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.tuple.Tuple;
import edu.uci.ics.textdb.dataflow.utils.DataflowUtils;

public class JsonSink implements ISink {
    
    private final JsonSinkPredicate predicate;
    private final BufferedWriter bufferedWriter;
    private IOperator inputOperator;
    private int cursor = CLOSED;
    
    public JsonSink(JsonSinkPredicate predicate) {
        try {
            this.predicate = predicate;
            this.bufferedWriter = Files.newBufferedWriter(
                    Paths.get(this.predicate.getFilePath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setInputOperator(IOperator inputOperator) {
        this.inputOperator = inputOperator;
    }

    @Override
    public Schema getOutputSchema() {
        return inputOperator.getOutputSchema();
    }

    @Override
    public void open() throws TextDBException {
        if (cursor != CLOSED) {
            return;
        }
        try {
            inputOperator.open();
            bufferedWriter.write("[");
            this.bufferedWriter.newLine();
            cursor = OPENED;
        } catch (IOException e) {
            throw new DataFlowException(e);
        }
    }

    @Override
    public void processTuples() throws TextDBException {
        if (cursor == CLOSED) {
            throw new DataFlowException(ErrorMessages.OPERATOR_NOT_OPENED);
        }
        Tuple tuple;
        while ((tuple = inputOperator.getNextTuple()) != null) {
            processOneTuple(tuple);
            cursor++;
        }
    }
    private void processOneTuple(Tuple tuple) throws TextDBException {
        try {
            this.bufferedWriter.write(",");
            this.bufferedWriter.write(DataflowUtils.getTupleJSON(tuple).toString());
            this.bufferedWriter.newLine();
        } catch (IOException e) {
            throw new DataFlowException(e);
        }
        
    }

    @Override
    public void close() throws TextDBException {
        if (cursor == CLOSED) {
            return;
        }
        try {
            inputOperator.close();
            bufferedWriter.write("]");
            cursor = CLOSED;
        } catch (IOException e) {
            throw new DataFlowException(e);
        }
    }


}
