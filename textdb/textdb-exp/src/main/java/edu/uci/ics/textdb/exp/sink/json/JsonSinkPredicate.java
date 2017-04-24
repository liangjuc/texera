package edu.uci.ics.textdb.exp.sink.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.exp.common.PredicateBase;
import edu.uci.ics.textdb.exp.common.PropertyNameConstants;

public class JsonSinkPredicate extends PredicateBase {
    
    private final String filePath;
    
    @JsonCreator
    public JsonSinkPredicate(
            @JsonProperty(value = PropertyNameConstants.FILE_PATH, required = true)
            String filePath
            ) {
        this.filePath = filePath;
    }
    
    @JsonProperty(value = PropertyNameConstants.FILE_PATH)
    public String getFilePath() {
        return this.filePath;
    }

    @Override
    public IOperator newOperator() {
        return new JsonSink(this);
    }

}
