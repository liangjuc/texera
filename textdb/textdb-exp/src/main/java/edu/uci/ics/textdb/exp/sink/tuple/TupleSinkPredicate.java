package edu.uci.ics.textdb.exp.sink.tuple;

import com.fasterxml.jackson.annotation.JsonCreator;

import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.exp.common.PredicateBase;

public class TupleSinkPredicate extends PredicateBase {
    
    @JsonCreator
    public TupleSinkPredicate() {}
    
    @Override
    public IOperator getOperator() {
        return new TupleSink(this);
    }

}
