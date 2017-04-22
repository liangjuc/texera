package edu.uci.ics.textdb.perftest.sample;

import edu.uci.ics.textdb.api.constants.SchemaConstants;
import edu.uci.ics.textdb.api.constants.DataConstants.KeywordMatchingType;
import edu.uci.ics.textdb.api.engine.Engine;
import edu.uci.ics.textdb.api.engine.Plan;
import edu.uci.ics.textdb.api.field.StringField;
import edu.uci.ics.textdb.api.field.TextField;
import edu.uci.ics.textdb.api.tuple.Tuple;
import edu.uci.ics.textdb.dataflow.common.IJoinPredicate;
import edu.uci.ics.textdb.dataflow.common.JoinDistancePredicate;
import edu.uci.ics.textdb.dataflow.common.KeywordPredicate;
import edu.uci.ics.textdb.dataflow.common.RegexPredicate;
import edu.uci.ics.textdb.dataflow.join.Join;
import edu.uci.ics.textdb.dataflow.keywordmatch.KeywordMatcherSourceOperator;
import edu.uci.ics.textdb.dataflow.nlpextrator.NlpExtractor;
import edu.uci.ics.textdb.dataflow.nlpextrator.NlpPredicate;
import edu.uci.ics.textdb.dataflow.projection.ProjectionOperator;
import edu.uci.ics.textdb.dataflow.projection.ProjectionPredicate;
import edu.uci.ics.textdb.dataflow.regexmatch.RegexMatcher;
import edu.uci.ics.textdb.dataflow.sink.FileSink;
import edu.uci.ics.textdb.dataflow.utils.DataflowUtils;
import edu.uci.ics.textdb.perftest.medline.MedlineIndexWriter;
import edu.uci.ics.textdb.perftest.promed.PromedSchema;
import edu.uci.ics.textdb.storage.DataWriter;
import edu.uci.ics.textdb.storage.RelationManager;
import edu.uci.ics.textdb.storage.constants.LuceneAnalyzerConstants;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class SampleExtraction {
    
    public static final String MEDLINE_SAMPLE_TABLE = "medline";
        
    public static String medlineFilesDirectory;
    public static String medlineIndexDirectory;
    public static String sampleDataFilesDirectory;

    static {
        try {
            // Finding the absolute path to the sample data files directory and index directory

            // Checking if the resource is in a jar
            String referencePath = SampleExtraction.class.getResource("").toURI().toString();
            if(referencePath.substring(0, 3).equals("jar")) {
                medlineFilesDirectory = "../textdb-perftest/src/main/resources/sample-data-files/medline/";
                medlineIndexDirectory = "../textdb-perftest/src/main/resources/index/standard/medline/";
                sampleDataFilesDirectory = "../textdb-perftest/src/main/resources/sample-data-files/";
            }
            else {
                medlineFilesDirectory = Paths.get(SampleExtraction.class.getResource("/sample-data-files/medline")
                        .toURI())
                        .toString();
                medlineIndexDirectory = Paths.get(SampleExtraction.class.getResource("/index/standard")
                        .toURI())
                        .toString() + "/medline";
                System.out.println(medlineIndexDirectory);
                sampleDataFilesDirectory = Paths.get(SampleExtraction.class.getResource("/sample-data-files")
                        .toURI())
                        .toString();
            }
        }
        catch(URISyntaxException | FileSystemNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception {
        // write the index of data files
        // index only needs to be written once, after the first run, this function can be commented out
        writeSampleIndex();

        // perform the extraction task
    }

    public static Tuple parsePromedHTML(String fileName, String content) {
        try {
            Document parsedDocument = Jsoup.parse(content);
            String mainText = parsedDocument.getElementById("preview").text();
            Tuple tuple = new Tuple(PromedSchema.PROMED_SCHEMA, new StringField(fileName), new TextField(mainText));
            return tuple;
        } catch (Exception e) {
            return null;
        }
    }

    public static void writeSampleIndex() throws Exception {
        // parse the original file
        File sourceFileFolder = new File(medlineFilesDirectory);
        ArrayList<Tuple> fileTuples = new ArrayList<>();
        for (File htmlFile : sourceFileFolder.listFiles()) {
            StringBuilder sb = new StringBuilder();
            Scanner scanner = new Scanner(htmlFile);
            while (scanner.hasNext()) {
				Tuple tuple = MedlineIndexWriter.recordToTuple(scanner.nextLine());
				if (tuple != null) {
                        fileTuples.add(tuple);
                }
            }
            scanner.close();
        }
        
        // write tuples into the table
        RelationManager relationManager = RelationManager.getRelationManager();
        
        relationManager.deleteTable(MEDLINE_SAMPLE_TABLE);
        relationManager.createTable(MEDLINE_SAMPLE_TABLE, medlineIndexDirectory, 
                MedlineIndexWriter.SCHEMA_MEDLINE, LuceneAnalyzerConstants.standardAnalyzerString());
        
        DataWriter dataWriter = relationManager.getTableDataWriter(MEDLINE_SAMPLE_TABLE);
        dataWriter.open();
        for (Tuple tuple : fileTuples) {
            dataWriter.insertTuple(tuple);
        }
        dataWriter.close();
    }



}
