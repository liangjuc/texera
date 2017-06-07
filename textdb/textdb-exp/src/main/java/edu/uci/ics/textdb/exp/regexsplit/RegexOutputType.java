package edu.uci.ics.textdb.exp.regexsplit;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RegexOutputType {
    ONE_TO_ONE("oneToOne"),
    ONE_TO_MANY("oneToMany");
    
    private final String name;
    
    private RegexOutputType(String name) {
        this.name = name;
    }
    
    // use the name string instead of enum string in JSON
    @JsonValue
    public String getName() {
        return this.name;
    }
    
    @Override
    public String toString() {
        return this.name;
    }

}
