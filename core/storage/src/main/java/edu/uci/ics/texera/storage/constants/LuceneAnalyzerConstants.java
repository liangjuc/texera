package edu.uci.ics.texera.storage.constants;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.ngram.NGramTokenizerFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import edu.uci.ics.texera.api.exception.DataflowException;

/**
 * LuceneAnalyzerConstants contains helper functions specifically
 *   used when dealing with different Lucene analyzers.
 * 
 * @author Zuozhi Wang
 *
 */
public class LuceneAnalyzerConstants {
    
    
    public static String standardAnalyzerString() {
        return "standard";
    }
    
    public static String nGramAnalyzerString(int gramNum) {
        return gramNum + "-gram";
    }
    
    public static String chineseAnalyzerString() {
        return "smartchinese";
    }
    
    /**
     * Gets the lucene analyzer based on the string, currently analyzers supported are:
     *   "standard", same as calling standardAnalyzerString().
     *   
     *   "n-gram", n represents the number of grams, for example, "3-gram",
     *     same as calling nGramAnalyzerString(3).
     * 
     * @param luceneAnalyzerString
     * @return
     * @throws DataflowException, if the luceneAnalyzerString is invalid
     */
    public static Analyzer getLuceneAnalyzer(String luceneAnalyzerString) throws DataflowException {
        if (luceneAnalyzerString.equals("standard")) {
            return LuceneAnalyzerConstants.getStandardAnalyzer();
        }
        else if (luceneAnalyzerString.endsWith("-gram")) {
            try {
                Integer gramNum = Integer.parseInt(
                        luceneAnalyzerString.substring(0, luceneAnalyzerString.indexOf('-')));
                return getNGramAnalyzer(gramNum);
            } catch (NumberFormatException e) {
                throw new DataflowException(luceneAnalyzerString + " is not a valid lucene analyzer");
            }
        } else if (luceneAnalyzerString.equals("smartchinese")) {
            return new SmartChineseAnalyzer();
        }
        throw new DataflowException(luceneAnalyzerString + " is not a valid lucene analyzer");
    }


    public static Analyzer getStandardAnalyzer() {
        return new StandardAnalyzer();
    }

    /**
     * @return a n-gram analyzer that tokenizes the text into grams of length n.
     * @throws DataflowException
     */
    public static Analyzer getNGramAnalyzer(int gramNum) throws DataflowException {
        try {
            return CustomAnalyzer.builder()
                    .withTokenizer(NGramTokenizerFactory.class, 
                            new String[] { "minGramSize", Integer.toString(gramNum), "maxGramSize", Integer.toString(gramNum) })
                    .addTokenFilter(LowerCaseFilterFactory.class).build();
        } catch (IOException e) {
            throw new DataflowException(e.getMessage(), e);
        }
    }

}
