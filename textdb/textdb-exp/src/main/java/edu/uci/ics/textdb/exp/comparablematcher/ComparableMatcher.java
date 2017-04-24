package edu.uci.ics.textdb.exp.comparablematcher;

import java.math.BigDecimal;

import edu.uci.ics.textdb.api.exception.DataFlowException;
import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.schema.AttributeType;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.tuple.*;
import edu.uci.ics.textdb.exp.common.AbstractSingleInputOperator;

/**
 * ComparableMatcher is matcher for comparison query on any field which deals with Comparable.
 *
 * @author Adrian Seungjin Lee
 */
public class ComparableMatcher extends AbstractSingleInputOperator {
    private ComparablePredicate predicate;
    private AttributeType inputAttrType;

    private Schema inputSchema;

    public ComparableMatcher(ComparablePredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    protected void setUp() throws DataFlowException {
        inputSchema = inputOperator.getOutputSchema();
        outputSchema = inputSchema;
        if (! inputSchema.containsField(predicate.getAttributeName())) {
            throw new DataFlowException(String.format(
                    "attribute %s not contained in input schema %s", 
                    predicate.getAttributeName(),
                    inputSchema.getAttributeNames()));
        }
        inputAttrType = inputOperator.getOutputSchema().getAttribute(predicate.getAttributeName()).getAttributeType();
        if (inputAttrType != AttributeType.INTEGER && inputAttrType != AttributeType.DOUBLE) {
            throw new DataFlowException(String.format(
                    "attribute type %s is not supported. Must be Integer or Double.", 
                    inputAttrType));
        }
    }

    @Override
    protected Tuple computeNextMatchingTuple() throws TextDBException {
        Tuple inputTuple = null;
        Tuple resultTuple = null;

        while ((inputTuple = inputOperator.getNextTuple()) != null) {
            resultTuple = processOneInputTuple(inputTuple);

            if (resultTuple != null) {
                break;
            }
        }
        return resultTuple;
    }

    @Override
    public Tuple processOneInputTuple(Tuple inputTuple) throws TextDBException {
        Tuple resultTuple = null;

        String attributeName = predicate.getAttributeName();
        NumberMatchingType operatorType = predicate.getMatchingType();
        
        BigDecimal value;
        BigDecimal threshold;
        try {
            if (inputAttrType.equals(AttributeType.INTEGER)) {
                value = new BigDecimal((int) inputTuple.getField(attributeName).getValue());
                Number thresholdNum = predicate.getThreshold();
                if (thresholdNum.getClass().equals(Integer.class)) {
                    threshold = new BigDecimal((Integer) predicate.getThreshold()); 
                } else {
                    threshold = new BigDecimal((Double) predicate.getThreshold()); 
                }
            } else {
                value = new BigDecimal((double) inputTuple.getField(attributeName).getValue());
                threshold = new BigDecimal((double) predicate.getThreshold()); 
            }
        } catch (ClassCastException e) {
            return null;
        }

        if (compareValues(value, threshold, operatorType)) {
            resultTuple = inputTuple;
        }
        return resultTuple;
    }

    private <T extends Comparable<T>>boolean compareValues(T value, T threshold, NumberMatchingType operatorType) {
        int compareResult = value.compareTo(threshold);
        switch (operatorType) {
            case EQUAL_TO:
                if (compareResult == 0) {
                    return true;
                }
                break;
            case GREATER_THAN:
                if (compareResult == 1) {
                    return true;
                }
                break;
            case GREATER_THAN_OR_EQUAL_TO:
                if (compareResult == 0 || compareResult == 1) {
                    return true;
                }
                break;
            case LESS_THAN:
                if (compareResult == -1) {
                    return true;
                }
                break;
            case LESS_THAN_OR_EQUAL_TO:
                if (compareResult == 0 || compareResult == -1) {
                    return true;
                }
                break;
            case NOT_EQUAL_TO:
                if (compareResult != 0) {
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    protected void cleanUp() throws DataFlowException {
    }

}
