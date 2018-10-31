package edu.columbia.historylab.ner.handlers;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class TestAnnotationDocumentReader  extends TestCase{

    public void testGetTokenizedText() throws IOException {
    	String t = AnnotationDocumentReader.getTextFromAnnotated(new File("src/test/resources/gt/unittest/1977LONDON02410.txt"));
    	//System.out.println(t);
    	assertTrue(t.contains("DENNIS SANDBERG"));
    }
    
}
