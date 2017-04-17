package edu.uci.ics.textdb.perftest.leagal;

import edu.uci.ics.textdb.api.engine.Plan;
import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.dataflow.sink.IndexSink;
import edu.uci.ics.textdb.dataflow.source.PDFSourceOperator;

import java.util.List;

/**
 * Created by junm5 on 4/17/17.
 */
public class LeagalIndexWriter {

    public static String legalFilesDirectory = "../textdb-perftest/src/main/resources/sample-data-files/legal/";

    public static Plan getLegalPlan(List<String> filePaths, String tableName) throws TextDBException {
        IndexSink legalndexSink = new IndexSink(tableName, false);

        PDFSourceOperator pdfSourceOperator = new PDFSourceOperator(filePaths, , );
        legalndexSink.setInputOperator(pdfSourceOperator);
        Plan writeIndexPlan = new Plan(legalndexSink);
        return writeIndexPlan;
    }
}
