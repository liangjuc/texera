package edu.uci.ics.textdb.perftest.sample;

import edu.uci.ics.textdb.api.field.StringField;
import edu.uci.ics.textdb.api.field.TextField;
import edu.uci.ics.textdb.api.tuple.Tuple;
import edu.uci.ics.textdb.dataflow.common.RegexPredicate;
import edu.uci.ics.textdb.dataflow.regexmatch.RegexMatcher;
import edu.uci.ics.textdb.dataflow.sink.TupleStreamSink;
import edu.uci.ics.textdb.dataflow.source.ScanBasedSourceOperator;
import edu.uci.ics.textdb.dataflow.source.TupleStreamSourceOperator;
import edu.uci.ics.textdb.dataflow.utils.DataflowUtils;
import edu.uci.ics.textdb.perftest.leagal.LegalSchema;
import edu.uci.ics.textdb.storage.DataWriter;
import edu.uci.ics.textdb.storage.RelationManager;
import edu.uci.ics.textdb.storage.constants.LuceneAnalyzerConstants;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by junm5 on 4/9/17.
 */
public class LegalExtraction {
    public static final String LEGAL_SAMPLE_TABLE = "legal";

    public static String legalIndexDirectory;
    public static String legalFilesDirectory;


    static {
        try {
            String referencePath = SampleExtraction.class.getResource("").toURI().toString();
            if (referencePath.substring(0, 3).equals("jar")) {
                legalFilesDirectory = "../textdb-perftest/src/main/resources/sample-data-files/legal/";
                legalIndexDirectory = "../textdb-perftest/src/main/resources/index/standard/legal/";
            } else {
                legalIndexDirectory = Paths.get(SampleExtraction.class.getResource("/index/standard")
                        .toURI())
                        .toString() + "/legal";

                legalFilesDirectory = Paths.get(SampleExtraction.class.getResource("/sample-data-files")
                        .toURI())
                        .toString() + "/legal";
                System.out.println(legalFilesDirectory);
            }
        } catch (URISyntaxException | FileSystemNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        // write the index of data files
        // index only needs to be written once, after the first run, this function can be commented out
        testExtraction();
        // perform the extraction task
        // extractPersonLocation();
    }

    public static void writeSampleIndex() throws Exception {
        // parse the original file
        File sourceFileFolder = new File(legalFilesDirectory);
        ArrayList<Tuple> fileTuples = new ArrayList<>();

        for (File pdfFile : sourceFileFolder.listFiles()) {
            String text = readPDFDocument(pdfFile);
            if (text != null) {
                fileTuples.add(new Tuple(LegalSchema.LEGAL_SCHEMA, new StringField(pdfFile.getName()), new TextField(text)));
            }
        }
        // write tuples into the table
        RelationManager relationManager = RelationManager.getRelationManager();

        relationManager.deleteTable(LEGAL_SAMPLE_TABLE);
        relationManager.createTable(LEGAL_SAMPLE_TABLE, legalIndexDirectory,
                LegalSchema.LEGAL_SCHEMA, LuceneAnalyzerConstants.standardAnalyzerString());

        DataWriter dataWriter = relationManager.getTableDataWriter(LEGAL_SAMPLE_TABLE);
        dataWriter.open();
        for (Tuple tuple : fileTuples) {
            dataWriter.insertTuple(tuple);
        }
        dataWriter.close();
    }

    private static String readPDFDocument(File file) {
        PDDocument doc;
        try {
            doc = PDDocument.load(file);
            return new PDFTextStripper().getText(doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void testExtraction() throws Exception {
        ScanBasedSourceOperator scanSource = new ScanBasedSourceOperator("legal");

        String s = readPDFDocument(
                new File("/Users/junm5/workplace/textdb/textdb/textdb-perftest/src/main/resources/sample-data-files/legal/107.pdf"));

        Tuple legalDocTuple = new Tuple(LegalSchema.LEGAL_SCHEMA,
                new StringField("107"),
                new TextField(new String(s.replace("\n", "").getBytes(), Charset.forName("UTF-8"))));

        TupleStreamSourceOperator tupleSource = new TupleStreamSourceOperator(
                Arrays.asList(legalDocTuple), LegalSchema.LEGAL_SCHEMA);

        RegexMatcher regexMatcher = new RegexMatcher(new RegexPredicate(
                ",*Plaintiff", Arrays.asList("content"), LuceneAnalyzerConstants.getStandardAnalyzer()));

        TupleStreamSink sink = new TupleStreamSink();

        regexMatcher.setInputOperator(tupleSource);
        sink.setInputOperator(regexMatcher);

        sink.open();

        List<Tuple> results = sink.collectAllTuples();

        sink.close();

        System.out.println(results.size());

        System.out.println(DataflowUtils.getTupleListString(results));
    }

}
