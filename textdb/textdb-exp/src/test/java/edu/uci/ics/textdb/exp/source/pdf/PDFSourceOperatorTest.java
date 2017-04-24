package edu.uci.ics.textdb.exp.source.pdf;

import edu.uci.ics.textdb.api.tuple.Tuple;
import junit.framework.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by junm5 on 4/17/17.
 */
public class PDFSourceOperatorTest {


    private static Path tempFolderPath = Paths.get("./index/test_tables/pdfsource/");
    public static Path tempFile1Path = tempFolderPath.resolve("107.pdf");


    /*
     * Test PDFSourceOperator with a single "txt" file.
     * Optional parameters are all set to default.
     */
    @Test
    public void should_generate_one_tuple_when_input_one_pdf_document() throws Exception {
        String attrName = "content";

        PDFSourcePredicate predicate = new PDFSourcePredicate(tempFile1Path.toString(), attrName);
        PDFSourceOperator pdfSourceOperator = new PDFSourceOperator(predicate);

        Tuple tuple;
        ArrayList<Tuple> exactResults = new ArrayList<>();
        pdfSourceOperator.open();
        while ((tuple = pdfSourceOperator.getNextTuple()) != null) {
            exactResults.add(tuple);
//            System.out.println(tuple.toString());
        }
        pdfSourceOperator.close();

        Assert.assertTrue(exactResults.size() == 1);
    }

    /*
     * Test PDFSourceOperator with a Directory.
     * Optional parameters are all set to default. (only list files directly in this folder)     *
     * Only the files directly under this directory will be used.
     */
    @Test
    public void should_generate_multiple_tuples_when_input_a_folder_path() throws Exception {
        String attrName = "content";

        PDFSourcePredicate predicate = new PDFSourcePredicate(
                tempFolderPath.toString(), attrName);
        PDFSourceOperator fileSource = new PDFSourceOperator(predicate);

        Tuple tuple;
        ArrayList<Tuple> exactResults = new ArrayList<>();
        fileSource.open();
        while ((tuple = fileSource.getNextTuple()) != null) {
            exactResults.add(tuple);
        }
        fileSource.close();

        Assert.assertTrue(exactResults.size() == 2);
    }
}