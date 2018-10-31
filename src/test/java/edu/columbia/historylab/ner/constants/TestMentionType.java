package edu.columbia.historylab.ner.constants;

import junit.framework.TestCase;

public class TestMentionType extends TestCase {

    public void testCreate() throws Exception {
    	assertTrue(MentionType.CITY==MentionType.create("city"));
    	try {
    		MentionType.create("shmity");
    		fail("failed to throw error");
    	} catch(IllegalArgumentException e) {
    		assertTrue(e.getMessage().contains("no MentionType corresponding"));
    	}
    }
}
