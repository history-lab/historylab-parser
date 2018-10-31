package edu.columbia.historylab.ner.ontology;

import java.io.IOException;

import com.xyonix.mayetrix.mayu.text.FoundEntity;

import junit.framework.TestCase;

public class TestHLTagger extends TestCase {
    
    public void testTagger() throws IOException {
    	boolean foundPlant=false;
    	for (FoundEntity e:HLTagger.getInstance().tag("Seattle is a city in the U.S.")) {
    		System.out.println(e.toReadableString());
    		foundPlant=e.getName().toLowerCase().equals("seattle");
    	}
    	assertTrue(foundPlant);
    }
    
}
