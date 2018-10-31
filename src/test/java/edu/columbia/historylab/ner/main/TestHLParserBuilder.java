package edu.columbia.historylab.ner.main;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.columbia.historylab.ner.handlers.Config;

public class TestHLParserBuilder {

	@Test
    public void testPrepTestTrainData() throws IOException {
    	Config.getInstance("conf/hl.config").setGloveSize(1000);
    	String modelPath = Config.getInstance().getModelPath();

		Config.getInstance().setModelPath("temp-hlparserbuildertest");
		Config.getInstance().setTrainDataDir("src/test/resources/gt/unittest");
		Config.getInstance().setDevDataDir("src/test/resources/gt/unittest");
		Config.getInstance().setTestDataDir("src/test/resources/gt/unittest");
    	File tempModelPath = new File(Config.getInstance().getModelPath());
    	FileUtils.deleteDirectory(tempModelPath); //in case someone killed something mid run last time
    	tempModelPath.mkdirs();
    	assertTrue(tempModelPath.listFiles().length==0);
    	HLParserBuilder.prepTestTrainData("conf/hl.config", true);    	
    	assertTrue(tempModelPath.listFiles().length==4);
    	FileUtils.deleteDirectory(tempModelPath);
		Config.getInstance().setModelPath(modelPath);
	}

}
