package edu.uci.ics.textdb.exp.nlp.sentiment;
import edu.uci.ics.textdb.api.field.TextField;
import edu.uci.ics.textdb.api.schema.Attribute;
import edu.uci.ics.textdb.api.schema.AttributeType;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.tuple.Tuple;

public class NltkSentimentTestConstants {
    public static String TEXT = "text";
    
    public static Attribute TEXT_ATTRIBUTE = new Attribute("text", AttributeType.TEXT);
    
    public static Schema SENTIMENT_SCHEMA = new Schema(TEXT_ATTRIBUTE);
    
    public static Tuple POSITIVE_TUPLE = new Tuple(SENTIMENT_SCHEMA,
            new TextField("Have a great weekend guys! https/t.co/DLDKLpeTAb"));
    
    public static Tuple NEGATIVE_TUPLE = new Tuple(SENTIMENT_SCHEMA, new TextField("Bugs are always annoying."));
}
