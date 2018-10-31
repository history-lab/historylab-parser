package edu.columbia.historylab.ner.main;

import java.util.List;

import edu.columbia.historylab.ner.constants.MentionType;
import edu.columbia.historylab.ner.constants.NERType;
import edu.columbia.historylab.ner.handlers.Config;
import edu.columbia.historylab.ner.models.Mention;
import junit.framework.TestCase;

public class TestHLParser extends TestCase {

    public void testParse() throws Exception {
    	Config.getInstance("conf/hl.config").setGloveSize(1000);
    	Config.getInstance().addMentionTypeToNERTypeForceMap(MentionType.STANFORDNER_PER, NERType.PER_IND);
		Config.getInstance().addMentionTypeToNERTypeForceMap(MentionType.STANFORDNER_LOC, NERType.LOC);

		String text = "Iran is a country. Israel is too. Richard Nixon was a president.";

		List<Mention> mentions =  HLParser.getInstance("conf/hl.config").parse(text);
    	System.out.println("\n"+text+"\n");
        Mention.printList(mentions);
    	assertTrue(mentions!=null && mentions.size()>0);
    }
}
