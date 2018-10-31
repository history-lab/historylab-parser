package edu.columbia.historylab.ner.ontology;

import com.xyonix.mayetrix.mayu.text.BasicTagger;

public class HLTagger extends BasicTagger {
	
	 private static HLTagger instance = null;
		
	 protected HLTagger(HLOntology hlo) {
		 super(hlo);
	 }
	 
	 public static HLTagger getInstance() {
		 if(instance==null) {
			 instance=new HLTagger(HLOntology.getInstance());
		 }
		 return instance;
	 }
}
