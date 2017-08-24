package edu.uci.ics.textdb.exp.dictionarymatcher;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uci.ics.textdb.exp.common.PropertyNameConstants;
import edu.uci.ics.textdb.exp.utils.DataflowUtils;

/**
 * @author Sudeep [inkudo]
 */
public class Dictionary {

    private final ArrayList<String> dictionaryEntries;
    private Iterator<String> dictionaryIterator;
    /**
     * These three arraylists are used to prepare the tokens of each dictionary entry
     * in the setup() of the DictionaryMatcher for phrase and conjunction matching.
     * tokenSetsNoStopwords is for the conjunction matching type.
     * tokenListsNoStopwords and tokenListsWithStopwords are for the phrase matching type.
     * Example: to tokenize dictionary entry "lin lin to be panda"
     * tokenSetsNoStopwords: [{"lin", "panda"}]
     * tokenListsNoStopwords: [["lin", "lin", "panda"]]
     * tokenListsWithStopwords: [["lin", "lin", "to", "be", "panda"]]
     */
    private ArrayList<Set<String>> tokenSetsNoStopwords;
    private ArrayList<List<String>> tokenListsWithStopwords;
    private ArrayList<List<String>> tokenListsNoStopwords;

    /**
     * Create a dictionary using a collection of entries.
     *
     * @param dictionaryEntries, a collection of dictionary entries
     */
    @JsonCreator
    public Dictionary(
            @JsonProperty(value = PropertyNameConstants.DICTIONARY_ENTRIES, required = true)
                    Collection<String> dictionaryEntries) {

        this.dictionaryEntries = new ArrayList<>();
        for (String entry : dictionaryEntries) {
            String entryTrimed = entry.trim();
            if (!entryTrimed.equals("")) {
                if (!this.dictionaryEntries.isEmpty() && this.dictionaryEntries.contains(entryTrimed)) {
                    continue;
                }
                this.dictionaryEntries.add(entryTrimed);
            }
        }
        this.dictionaryIterator = this.dictionaryEntries.iterator();
        this.tokenSetsNoStopwords = null;
        this.tokenListsNoStopwords = null;
        this.tokenListsWithStopwords = null;
    }

    @JsonProperty(value = PropertyNameConstants.DICTIONARY_ENTRIES)
    public ArrayList<String> getDictionaryEntries() {
        return this.dictionaryEntries;
    }

    /**
     * Gets next dictionary entry from the dictionary
     */
    @JsonIgnore
    public String getNextEntry() {
        if (dictionaryIterator.hasNext()) {
            return dictionaryIterator.next();
        }
        return null;
    }


    public boolean isEmpty() {
        return (dictionaryEntries == null || dictionaryEntries.isEmpty());
    }

    /***
     * To generate a set of tokens for each dictionary entry with removal
     * of duplicate tokens in the setup() of DictionaryMather for conjunction matching type.
     * @param luceneAnalyzerStr
     */

    public void setDictionaryTokenSetList(String luceneAnalyzerStr) {
        this.tokenSetsNoStopwords = new ArrayList<>();
        for (int i = 0; i < dictionaryEntries.size(); i++) {
            tokenSetsNoStopwords.add(new HashSet<>(DataflowUtils.tokenizeQuery(luceneAnalyzerStr, dictionaryEntries.get(i))));
        }
    }

    /**
     * To generate a list of tokens for each dictionary entry without removal of duplicate tokens:
     * and a list of tokens for each dictionary entry without removal of stopwords and duplicate tokens.
     * Used by Phrase matching type in the setup() of DictionaryMatcher.
     *
     * @param luceneAnalyzerStr
     */

    public void setDictionaryTokenListWithStopwords(String luceneAnalyzerStr) {
        this.tokenListsWithStopwords = new ArrayList<>();
        this.tokenListsNoStopwords = new ArrayList<>();
        for (int i = 0; i < dictionaryEntries.size(); i++) {
            tokenListsNoStopwords.add(DataflowUtils.tokenizeQuery(luceneAnalyzerStr, dictionaryEntries.get(i)));
            tokenListsWithStopwords.add(DataflowUtils.tokenizeQueryWithStopwords(dictionaryEntries.get(i)));
        }
    }

    @JsonIgnore
    public ArrayList<Set<String>> getTokenSetsNoStopwords() {
        return this.tokenSetsNoStopwords;
    }

    @JsonIgnore
    public ArrayList<List<String>> getTokenListsWithStopwords() {
        return this.tokenListsWithStopwords;
    }

    @JsonIgnore
    public ArrayList<List<String>> getTokenListsNoStopwords() {
        return this.tokenListsNoStopwords;
    }

    @JsonIgnore
    public void resetCursor() {
        dictionaryIterator = dictionaryEntries.iterator();
        ;
    }
}


