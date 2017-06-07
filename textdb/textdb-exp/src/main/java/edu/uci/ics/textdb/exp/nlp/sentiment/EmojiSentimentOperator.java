package edu.uci.ics.textdb.exp.nlp.sentiment;

import edu.uci.ics.textdb.api.constants.ErrorMessages;
import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.api.exception.DataFlowException;
import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.field.IField;
import edu.uci.ics.textdb.api.field.IntegerField;
import edu.uci.ics.textdb.api.schema.Attribute;
import edu.uci.ics.textdb.api.schema.AttributeType;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.tuple.Tuple;
import edu.uci.ics.textdb.api.utils.Utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Operator performs sentiment analysis using Simple Emoji and Emoticon analysis targeted specifically for tweets
 * using unicode, browser escape character and smiley regex matching.
 *
 * The result is an integer indicating the sentiment score, which represents:
 *
 * 3 - positive
 * 2 - neutral
 * 1 - negative
 *
 *
 * The result will be put into an attribute with resultAttributeName specified in predicate, and type Integer.
 *
 * @author Vinay Bagade
 *
 */
public class EmojiSentimentOperator implements IOperator {
    private final EmojiSentimentPredicate predicate;
    private IOperator inputOperator;
    private Schema outputSchema;
    private int cursor = CLOSED;
    //SMILEY_REGEX_PATTERN identifies all happiness related emoticons like :) :-) <3 etc in the given text. 
    //The regex is given below. 
    public static final Pattern SMILEY_REGEX_PATTERN = Pattern.compile(".*(:[)DdpP]|:[ -]\\)|<3)+.*");
    //FROWNY_REGEX_PATTERN identifies all sadness related emoticons like :( :-( etc.
    //The regex is given below.
    public static final Pattern FROWNY_REGEX_PATTERN = Pattern.compile(".*(:[(<]|:[ -]\\()+.*");
    //Sometimes in chats and tweets the regex typed by users get converted to javascript escape characters. 
    //The range of these escape characters is given below in the EMOJI_REGEX pattern. 
    public static final Pattern EMOJI_REGEX = Pattern.compile(".*([\uD83C-\uDBFF\uDC00-\uDFFF])+.*");
    //Below is the list of all happy emoticon unicode characters.
    static ArrayList<String> happy = new ArrayList<String>(Arrays.asList("1f601", "1f602", "1f603", "1f604", "1f605",
            "1f606", "1f609", "1f60A", "1f60B", "1f60D", "1f618", "1f61A", "1f61C", "1f61D", "1f624", "1f632", "1f638",
            "1f639", "1f63A", "1f63B", "1f63D", "1f647", "1f64B", "1f64C", "1f64F", "U+270C", "U+2728", "U+2764", "U+263A",
            "U+2665", "U+3297", "1f31F", "1f44F", "1f48B", "1f48F", "1f491", "1f492", "1f493", "1f495", "1f496", "1f497",
            "1f498", "1f499", "1f49A", "1f49B", "1f49C", "1f49D", "1f49D", "1f49F", "1f4AA", "1f600", "1f607",
            "1f608", "1f60E", "1f617", "1f619", "1f61B", "1f31E", "1f60C", "1f60F", "1f633", "1f63C", "1f646",
            "U+2B50", "1f44D", "1f44C"));
    //Below is the list of all neutral emoticon unicode characters.
    static ArrayList<String> neutral = new ArrayList<String>(Arrays.asList("1f614", "1f623", "U+2753", "U+2754", "1f610", "1f611",
            "1f62E", "1f636"));
    //Below is the list of all unhappy emoticon unicode characters. 
    //The list can be found at http://unicode.org/emoji/charts/emoji-ordering.html
    static ArrayList<String> unhappy = new ArrayList<String>(Arrays.asList("1f612", "1f613", "1f616", "1f61E", "1f625", "1f628",
            "1f62A", "1f62B", "1f637", "1f635", "1f63E", "U+26A0", "1f44E", "1f4A4",
            "1f615", "1f61F", "1f62F", "1f634","1f620", "1f621", "1f622", "1f629",
            "1f62D", "1f630", "1f631", "1f63F", "1f640", "1f645", "1f64D", "1f64E",
            "U+274C", "U+274E", "1f494", "1f626", "1f627", "1f62C","U+2639","1f641",
            "1f616","1f61E","1f61F","1f624","1f622","1f62D","1f626","1f627",
            "1f628","1f629","1f92F","1f62C","1f630","1f631","1f633","1f92A",
            "1f635","1f621","1f620","1f92C"));
    
    public EmojiSentimentOperator(EmojiSentimentPredicate predicate) {
        this.predicate = predicate;
    }

    public void setInputOperator(IOperator operator) {
        if (cursor != CLOSED) {
            throw new RuntimeException("Cannot link this operator to other operator after the operator is opened");
        }
        this.inputOperator = operator;
    }
    
    private Schema transformSchema(Schema inputSchema) {
        if (inputSchema.containsField(predicate.getResultAttributeName())) {
            throw new RuntimeException(String.format(
                    "result attribute name %s is already in the original schema %s",
                    predicate.getResultAttributeName(),
                    inputSchema.getAttributeNames()));
        }
        return Utils.addAttributeToSchema(inputSchema,
                new Attribute(predicate.getResultAttributeName(), AttributeType.INTEGER));
    }
    
    @Override
    public void open() throws TextDBException {
        if (cursor != CLOSED) {
            return;
        }
        if (inputOperator == null) {
            throw new DataFlowException(ErrorMessages.INPUT_OPERATOR_NOT_SPECIFIED);
        }
        inputOperator.open();
        Schema inputSchema = inputOperator.getOutputSchema();

        // check if input schema is present
        if (! inputSchema.containsField(predicate.getInputAttributeName())) {
            throw new RuntimeException(String.format(
                    "input attribute %s is not in the input schema %s",
                    predicate.getInputAttributeName(),
                    inputSchema.getAttributeNames()));
        }

        // check if attribute type is valid
        AttributeType inputAttributeType =
                inputSchema.getAttribute(predicate.getInputAttributeName()).getAttributeType();
        boolean isValidType = inputAttributeType.equals(AttributeType.STRING) ||
                inputAttributeType.equals(AttributeType.TEXT);
        if (! isValidType) {
            throw new RuntimeException(String.format(
                    "input attribute %s must have type String or Text, its actual type is %s",
                    predicate.getInputAttributeName(),
                    inputAttributeType));
        }

        // generate output schema by transforming the input schema
        outputSchema = transformSchema(inputOperator.getOutputSchema());

        cursor = OPENED;
    }

    @Override
    public Tuple getNextTuple() throws TextDBException {
        if (cursor == CLOSED) {
            return null;
        }
        Tuple inputTuple = inputOperator.getNextTuple();
        if (inputTuple == null) {
            return null;
        }

        List<IField> outputFields = new ArrayList<>();
        outputFields.addAll(inputTuple.getFields());
        outputFields.add(new IntegerField(computeSentimentScore(inputTuple)));

        return new Tuple(outputSchema, outputFields);

    }

    @Override
    public void close() throws TextDBException {
        if (cursor == CLOSED) {
            return;
        }
        if (inputOperator != null) {
            inputOperator.close();
        }
        cursor = CLOSED;
    }

    @Override
    public Schema getOutputSchema() {
        return this.outputSchema;
    }
    
    /*The following function computes the sentiment score of the given field of a tuple.The function first checks if 
    there is a smiley related regex pattern in the text followed by a frowny regex pattern it adds a point if smiley 
    pattern is found and subtracts point for frowny regex pattern. If none of them are found it checks for javascript
    escape characters in range defined by EMOJI_REGEX . If escape characters are found it converts them into unicode
    string to check which if the unicode string is contained in the happy list, sad list or neutral Arraylist of unicode
    strings and increments or decrements score appropriately.*/ 
    
    
    private Integer computeSentimentScore(Tuple inputTuple) {
        String inputText = inputTuple.<IField>getField(predicate.getInputAttributeName()).getValue().toString();
        Matcher matcher = null;
        Integer matchedStringScore = 2;
        if(SMILEY_REGEX_PATTERN!= null){
            matcher = SMILEY_REGEX_PATTERN.matcher(inputText);
            if(matcher.matches()){
                matchedStringScore++;
            }
        }
        if(FROWNY_REGEX_PATTERN!= null){
            matcher = FROWNY_REGEX_PATTERN.matcher(inputText);
            if(matcher.matches()){
                matchedStringScore--;
            }
        }
        if (EMOJI_REGEX != null) {
            matcher = EMOJI_REGEX.matcher(inputText);
            if(matcher.matches()) {
                for( int i = 0; i < matcher.groupCount(); i++ ) {
                    String matchedString = matcher.group(i);
                    char[] ca = matchedString.toCharArray();
                    //if javascript escape characters in range of EMOJI_REGEX are found it loops through the entire strings to check 
                    // for presence of emoticon unicode in corrosponding arraylists. A unicodestring is made of two adjacent chars.
                    for(int j = 0; j < ca.length-1; j++  ) {
                        String unicodeString = String.format("%04x", Character.toCodePoint(ca[j], ca[j+1]));
                        //check if the uncode string is present in the any one of the arraylists
                        if(happy.contains( unicodeString )) {
                            matchedStringScore++;
                        } else if(neutral.contains( unicodeString )){
                            // neutral doesn't affect the score
                        } else if(unhappy.contains( unicodeString )){
                            matchedStringScore--;
                        }
                    }
                }
            }
        }
        if(matchedStringScore<1){
            matchedStringScore = 1;
        }
        if(matchedStringScore>3){
            matchedStringScore = 3;
        }
        return matchedStringScore;
    }
}
