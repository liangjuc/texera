package edu.uci.ics.texera.dataflow.nlp.sentiment;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import edu.uci.ics.texera.dataflow.common.PredicateBase;
import edu.uci.ics.texera.dataflow.common.PropertyNameConstants;

/**
 * Created by Vinay on 22-05-2017.
 */
public class EmojiSentimentPredicate extends PredicateBase {
    private final String inputAttributeName;
    private final String resultAttributeName;

    public EmojiSentimentPredicate(
            @JsonProperty(value = PropertyNameConstants.ATTRIBUTE_NAME, required = true)
            String inputAttributeName,
            @JsonProperty(value = PropertyNameConstants.RESULT_ATTRIBUTE_NAME, required = true)
            String resultAttributeName
    ) {
        this.inputAttributeName = inputAttributeName;
        this.resultAttributeName = resultAttributeName;
    }

    @JsonProperty(PropertyNameConstants.ATTRIBUTE_NAME)
    public String getInputAttributeName() {
        return this.inputAttributeName;
    }

    @JsonProperty(PropertyNameConstants.RESULT_ATTRIBUTE_NAME)
    public String getResultAttributeName() {
        return this.resultAttributeName;
    }

    @Override
    public EmojiSentimentOperator newOperator() {
        return new EmojiSentimentOperator(this);
    }
    
    public static Map<String, Object> getOperatorMetadata() {
        return ImmutableMap.<String, Object>builder()
            .put(PropertyNameConstants.USER_FRIENDLY_NAME, "Emoji Sentiment Analysis")
            .put(PropertyNameConstants.OPERATOR_DESCRIPTION, "Sentiment analysis with the emojis in consideration")
            .put(PropertyNameConstants.OPERATOR_GROUP_NAME, "Other")
            .build();
    }
    
}
