package edu.uci.ics.textdb.exp.aggregation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.exp.common.PredicateBase;
import edu.uci.ics.textdb.exp.common.PropertyNameConstants;

public class AggregationPredicate extends PredicateBase {
    
    private final String inputAttributeName;
    private final AggregationType aggregationType;
    private final String resultAttributeName;

    @JsonCreator()
    public AggregationPredicate(
            @JsonProperty(value = PropertyNameConstants.ATTRIBUTE_NAME, required = false)
            String inputAttributeName,
            @JsonProperty(value = PropertyNameConstants.AGGREGATION_TYPE, required = true)
            AggregationType aggregationType,
            @JsonProperty(value = PropertyNameConstants.RESULT_ATTRIBUTE_NAME, required = false)
            String resultAttributeName) {
        this.inputAttributeName = inputAttributeName;
        this.aggregationType = aggregationType;
        this.resultAttributeName = resultAttributeName;
    }
    
    @JsonProperty(value = PropertyNameConstants.ATTRIBUTE_NAME)
    public String getInputAttributeName() {
        return inputAttributeName;
    }

    @JsonProperty(value = PropertyNameConstants.AGGREGATION_TYPE)
    public AggregationType getAggregationType() {
        return aggregationType;
    }

    @JsonProperty(value = PropertyNameConstants.RESULT_ATTRIBUTE_NAME, required = false)
    public String getResultAttributeName() {
        return resultAttributeName;
    }

    @Override
    public IOperator newOperator() {
        return new AggregationOperator(this);
    }

}
