package edu.columbia.historylab.ner.models;

import java.util.Comparator;

/*
 * Compares two entities based on their length and index.
 */
public class MentionComparator implements Comparator<Mention> {
	
    public int compare(Mention mention1, Mention mention2) {
    	
    	if(mention1.getIndex() == mention2.getIndex()){
    		return new Double(mention2.getLength()).compareTo(new Double((mention1.getLength())));
    	}
    	
    	return new Double(mention1.getIndex()).compareTo(new Double((mention2.getIndex())));
    }
   
}
