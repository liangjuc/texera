package edu.uci.ics.textdb.exp.aggregation;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AggregationType {
    
    MIN("min"),
    MAX("max"),
    COUNT("count"),
    SUM("sum"),
    AVERAGE("average");
    
    private final String name;
    
    private AggregationType(String name) {
        this.name = name;
    }
    
    // use the name string instead of enum string in JSON
    @JsonValue
    public String getName() {
        return this.name;
    }

}
