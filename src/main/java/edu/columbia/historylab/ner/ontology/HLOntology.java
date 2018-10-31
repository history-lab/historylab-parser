package edu.columbia.historylab.ner.ontology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.xyonix.mayetrix.mayu.text.Ontology;

/**
 * Create Project specific ontology to ensure singleton super class Ontology is not shared between projects.
 */
public class HLOntology extends Ontology {
	
    private static HLOntology instance = null;

	private HLOntology() throws IOException {
		super();
	}
	
    public static HLOntology getInstance() {
        if(instance==null) {
        	try {
        		instance=new HLOntology();
        		List<String> ofiles = new ArrayList<String>();
        		for(String s:HLOntology.getInstance().getOntologyFiles("ontology/mayu")) {
        			ofiles.add("ontology/mayu/"+s);
        		}
        		HLOntology.getInstance().addEntitiesFromFile(ofiles.toArray(new String[0]));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
        }
        return instance;
    }

}
