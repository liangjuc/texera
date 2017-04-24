package edu.uci.ics.textdb.exp.comparablematcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.exp.common.PredicateBase;
import edu.uci.ics.textdb.exp.common.PropertyNameConstants;

/**
 *
 * @author Adrian Seungjin Lee
 *
 */
public class ComparablePredicate extends PredicateBase {

    private String attributeName;
    private Number threshold;
    private NumberMatchingType matchingType;

    @JsonCreator
    public ComparablePredicate(
            @JsonProperty(value = PropertyNameConstants.ATTRIBUTE_NAME, required = true)
            String attributeName,
            @JsonProperty(value = PropertyNameConstants.NUMBER_MATCHING_TYPE, required = true)
            NumberMatchingType matchingType,
            @JsonProperty(value = PropertyNameConstants.NUMBER_THRESHOLD, required = true)
            Number threshold) {
        this.threshold = threshold;
        this.attributeName = attributeName;
        this.matchingType = matchingType;
    }
    
    @JsonProperty(value = PropertyNameConstants.ATTRIBUTE_NAME)
    public String getAttributeName() {
        return attributeName;
    }

    @JsonProperty(value = PropertyNameConstants.NUMBER_MATCHING_TYPE)
    public NumberMatchingType getMatchingType() {
        return matchingType;
    }

    @JsonProperty(value = PropertyNameConstants.NUMBER_THRESHOLD)
    public Number getThreshold() {
        return threshold;
    }

    @Override
    public IOperator newOperator() {
        return new ComparableMatcher(this);
    }

}
