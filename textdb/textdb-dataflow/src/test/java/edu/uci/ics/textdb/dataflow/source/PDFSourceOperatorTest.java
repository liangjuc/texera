package edu.uci.ics.textdb.dataflow.source;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;

import java.io.File;

/**
 * Created by junm5 on 4/16/17.
 */
public class PDFSourceOperatorTest {

    private String pathname = "/Users/junm5/workplace/textdb/textdb/textdb-dataflow/src/test/resource/p10-hall.pdf";

//    @Test
//    public void should_create_tuple_from_() throws Exception {
//
//    }

    @Test
    public void should_read_chineses_from_pdf_using_pdf_box() throws Exception {
        File file = new File(pathname);
        PDDocument pdDocument = PDDocument.load(file);
        String text = new PDFTextStripper().getText(pdDocument);
        System.out.println("before coding:" + text);

    }

//    public String changeCharset(String str, String newCharset)
//            throws UnsupportedEncodingException {
//        if (str != null) {
//            //用默认字符编码解码字符串。
//            byte[] bs = str.getBytes();
//            //用新的字符编码生成字符串
//            return new String(bs, newCharset);
//        }
//        return null;
//    }

    @Test
    public void should_read_chinese_from_pdf_using_tika() throws Exception {
//        BodyContentHandler handler = new BodyContentHandler();
//        Metadata metadata = new Metadata();
//        PDFParser pdfparser = new PDFParser();
//        FileInputStream fileInputStream = new FileInputStream(new File(pathname));
//        pdfparser.parse(fileInputStream, handler, metadata, new ParseContext());
//        System.out.println(handler.toString());
//
//        InputStream is = null;
//        try {
//            is = new FileInputStream(new File(pathname));
//            ContentHandler contenthandler = new BodyContentHandler();
//            Metadata metadata = new Metadata();
//            PDFParser pdfparser = new PDFParser();
//            pdfparser.parse(is, contenthandler, metadata, new ParseContext());
//            System.out.println(contenthandler.toString());
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        finally {
//            if (is != null) is.close();
//        }
    }

}