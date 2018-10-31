package edu.columbia.historylab.ner.ontology;

import java.io.IOException;

import com.xyonix.mayetrix.mayu.text.FoundEntity;

import junit.framework.TestCase;

public class TestHLOntology extends TestCase {
    
    public void testWorking() throws IOException {
    	System.out.println("slow");
    	FoundEntity e = HLOntology.getInstance().search("seattle");
		System.out.println(e.toReadableString());
		//TODO: Assert something in this test
    }
    
}
