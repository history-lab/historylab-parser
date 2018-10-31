package edu.columbia.historylab.ner.ontology;

import java.io.IOException;

import com.xyonix.mayetrix.mayu.text.FoundEntity;

import junit.framework.TestCase;

public class TestHLOntology extends TestCase {

    public void testWorking() throws IOException {
      System.out.println("##### Heap utilization statistics [MB] #####");

      int mb = 1024*1024;
		  Runtime runtime = Runtime.getRuntime();
      System.out.println("Max Memory:" + runtime.maxMemory() / mb);
      //Print used memory
		    System.out.println("Used Memory:"
			+ (runtime.totalMemory() - runtime.freeMemory()) / mb);
		  //Print free memory
		  System.out.println("Free Memory:"
			+ runtime.freeMemory() / mb);
  		//Print total available memory
  		System.out.println("Total Memory:" + runtime.totalMemory() / mb);
    	System.out.println("slow");
    	FoundEntity e = HLOntology.getInstance().search("seattle");
		  System.out.println(e.toReadableString());

      //TODO: Assert something in this test
    }
}
