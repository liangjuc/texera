package edu.uci.ics.textdb.exp.aggregation;

import edu.uci.ics.textdb.api.constants.SchemaConstants;
import edu.uci.ics.textdb.api.exception.DataFlowException;
import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.field.DoubleField;
import edu.uci.ics.textdb.api.field.IDField;
import edu.uci.ics.textdb.api.field.IntegerField;
import edu.uci.ics.textdb.api.schema.Attribute;
import edu.uci.ics.textdb.api.schema.AttributeType;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.tuple.Tuple;
import edu.uci.ics.textdb.exp.common.AbstractSingleInputOperator;

public class AggregationOperator extends AbstractSingleInputOperator {
    
    private final AggregationPredicate predicate;
    private AttributeType inputAttrType;
    
    private boolean isResultInt;
    private Integer count;
    private Integer intSum;
    private Integer intMin;
    private Integer intMax;
    private Double doubleSum;
    private Double doubleMin;
    private Double doubleMax;
    private Double average;
    
    private Boolean returned = false;
    
    public AggregationOperator(AggregationPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    protected void setUp() throws TextDBException {
        Schema inputSchema = inputOperator.getOutputSchema();
        
        if (predicate.getAggregationType() == AggregationType.COUNT) {
            outputSchema = new Schema(SchemaConstants._ID_ATTRIBUTE, 
                    new Attribute(predicate.getResultAttributeName(), AttributeType.INTEGER));
            isResultInt = true;
        } else {
            if (! inputSchema.containsField(predicate.getInputAttributeName())) {
                throw new DataFlowException(String.format(
                        "attribute %s is not in the input schema %s", 
                        predicate.getInputAttributeName(),
                        inputSchema.getAttributeNames()));
            }
            if (predicate.getInputAttributeName() == null) {
                throw new DataFlowException("input attribute name is null");
            }
            inputAttrType = inputSchema.getAttribute(predicate.getInputAttributeName()).getAttributeType();
            isResultInt = isIntResult(predicate.getAggregationType(), inputAttrType);
            if (isResultInt) {
                outputSchema = new Schema(SchemaConstants._ID_ATTRIBUTE, 
                        new Attribute(predicate.getResultAttributeName(), AttributeType.INTEGER));
            } else {
                outputSchema = new Schema(SchemaConstants._ID_ATTRIBUTE, 
                        new Attribute(predicate.getResultAttributeName(), AttributeType.DOUBLE));
            }
        }

        this.count = 0;
        this.intSum = 0;
        this.intMax = Integer.MIN_VALUE;
        this.intMin = Integer.MAX_VALUE;
        this.doubleSum = 0.0;
        this.doubleMax = Double.MIN_VALUE;
        this.doubleMin = Double.MAX_VALUE;
        this.average = 0.0;
    }

    @Override
    protected Tuple computeNextMatchingTuple() throws TextDBException {
        if (returned) {
            return null;
        }
        
        returned = true;
        
        Tuple tuple;
        while ((tuple = inputOperator.getNextTuple()) != null) {
            this.count++;
            if (inputAttrType == AttributeType.INTEGER) {
                int tupleValue = (int) tuple.getField(predicate.getInputAttributeName()).getValue();
                this.intSum += tupleValue;
                this.intMin = tupleValue < this.intMin ? tupleValue : this.intMin;
                this.intMax = tupleValue > this.intMax ? tupleValue : this.intMax;
            } else if (inputAttrType == AttributeType.DOUBLE) {
                double tupleValue = (double) tuple.getField(predicate.getInputAttributeName()).getValue();
                this.doubleSum += tupleValue;
                this.doubleSum += tupleValue;
                this.doubleMin = tupleValue < this.doubleMin ? tupleValue : this.doubleMin;
                this.doubleMax = tupleValue > this.doubleMax ? tupleValue : this.doubleMax;
            }
        }
        if (inputAttrType == AttributeType.INTEGER) {
            this.average = ((double) this.intSum) / ((double) this.count);
        } else if (inputAttrType == AttributeType.DOUBLE) {
            this.average = this.doubleSum / ((double) this.count);
        }    
        
        
        switch (predicate.getAggregationType()) {
        case AVERAGE:
            return new Tuple(outputSchema, IDField.newRandomID(), new DoubleField(this.average)); 
        case COUNT:
            return new Tuple(outputSchema, IDField.newRandomID(), new IntegerField(this.count));
        case MAX:
            if (isResultInt) {
                return new Tuple(outputSchema, IDField.newRandomID(), new IntegerField(this.intMax));
            } else {
                return new Tuple(outputSchema, IDField.newRandomID(), new DoubleField(this.doubleMax));
            }
        case MIN:
            if (isResultInt) {
                return new Tuple(outputSchema, IDField.newRandomID(), new IntegerField(this.intMin));
            } else {
                return new Tuple(outputSchema, IDField.newRandomID(), new DoubleField(this.doubleMin));
            }
        case SUM:
            if (isResultInt) {
                return new Tuple(outputSchema, IDField.newRandomID(), new IntegerField(this.intSum));
            } else {
                return new Tuple(outputSchema, IDField.newRandomID(), new DoubleField(this.doubleSum));
            }
        default:
            return null;
        
        }
    }

    @Override
    public Tuple processOneInputTuple(Tuple inputTuple) throws TextDBException {
        throw new DataFlowException("process one tuple not supported");
    }

    @Override
    protected void cleanUp() throws TextDBException {        
    }
        
    private static boolean isIntResult(AggregationType aggregationType, AttributeType inputAttrType) {
        switch (aggregationType) {
        case AVERAGE:
            return false;
        case COUNT:
            return true;
        case MAX:
            return inputAttrType.equals(AttributeType.INTEGER);
        case MIN:
            return inputAttrType.equals(AttributeType.INTEGER);
        case SUM:
            return inputAttrType.equals(AttributeType.INTEGER);
        default:
            return false;
        }
    }

}
