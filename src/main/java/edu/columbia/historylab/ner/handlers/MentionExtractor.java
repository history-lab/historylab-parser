package edu.columbia.historylab.ner.handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.xyonix.mayetrix.mayu.data.TaxPath;
import com.xyonix.mayetrix.mayu.text.FoundEntity;

import edu.columbia.historylab.ner.constants.MentionType;
import edu.columbia.historylab.ner.models.StanfordOutput;
import edu.columbia.historylab.ner.ontology.HLTagger;

/*
 * Extracts mentions from text given the lookups and by applying Stanford CoreNLP.
 */
public class MentionExtractor {
	
	private String content;
	private boolean caseless;
	private List<String> nounPhrases = new ArrayList<String>();
	private Map<String, MentionType> stanfordPhrases = new HashMap<String, MentionType>();
	private Map<String, MentionType> lookupPhrases = new HashMap<String, MentionType>();
	private List<Set<String>> coreferences = new ArrayList<Set<String>>();
	
	public MentionExtractor(File file) throws IOException{
		readContent(file);
	}

	public MentionExtractor(String content){
		this.content = content;
	}
	
	/**
	 * Extracts mentions. Steps:
	 * 1- Extract all the possible noun phrases
	 * 2- mark the phrases seen in the lookups
	 * 3- Apply Stanford CoreNLP NER
	 * 4- Mark the mentions given the information in (1), (2) and (3)			
	 */
	public void extract(boolean caseless){
			this.caseless = caseless;
			extractNounPhrases();
			extractLookupPhrases();
			extractStanfordPhrases();
			markMentions();
	}
	
	private void extractNounPhrases(){
		StanfordOutput stanfordOutput = StanfordParsingAndCoreference.getInstance(caseless).execute(content);
		nounPhrases = stanfordOutput.getMentions();
		coreferences = stanfordOutput.getCoreferences();
	}
	
	/**
	 * Apply Stanford CoreNLP NER
	 */
	private void extractStanfordPhrases(){
		String text = StanfordNER.getInstance(caseless).applyNER(content);
		stanfordPhrases = StanfordNER.getNamedEntities(text);
	}
	
	/**
	 * Look for lookups in the extracted noun phrases
	 */
	private void extractLookupPhrases(){
		
		Map<String, MentionType> tempLookupPhrases = new HashMap<String, MentionType>();
		Set<String> processedKeys = new HashSet<String>();

		for (FoundEntity fe:HLTagger.getInstance().tag(content)) {
			if (Config.getInstance().isVerbose()) {
				System.out.println("HLOntology found entity: " + fe.toReadableString());
			}
			for (TaxPath tp:fe.getPaths()) {
				if (tp.getName().contains("synonym")) {
					continue; //don't add synonym types or they will end up w/ NERType=None
				}
				//TODO last in wins, pick order or retain all.
				tempLookupPhrases.put(fe.getDisplayName(), MentionType.transformOntologyTypes(tp.getName()));
				processedKeys.add(fe.getMetadata().get("wikiurl"));
			}

		}
		for(Entry<String, MentionType> entry1 : tempLookupPhrases.entrySet()){
			if(!entry1.getKey().matches("^.*[a-zA-Z].*$")){
				continue;
			}
			lookupPhrases.put(entry1.getKey(), entry1.getValue());
		}

	}
	
	/**
	 * Marking the extracted mentions
	 */
	private void markMentions(){
		
		Map<String, MentionType> allPhrases = new HashMap<String, MentionType>();
		
		//Lookup mentions
		for(Entry<String, MentionType> entry : lookupPhrases.entrySet()){
			allPhrases.put(entry.getKey(), entry.getValue());
		}
		
		//Stanford mentions
		for(Entry<String, MentionType> entry1 : stanfordPhrases.entrySet()){
			boolean valid = true;
			if(valid && !allPhrases.containsKey(entry1.getKey())){
				allPhrases.put(entry1.getKey(), entry1.getValue());
			}
		}
		
		//All other mentions that are noun phrases
		for(String phrase : nounPhrases){
			boolean valid = true;
			if(valid && !allPhrases.containsKey(phrase)){
				allPhrases.put(phrase, MentionType.NONE);
			}
		}
		
		//Mark the mentions as if they are manual annotations
		allPhrases = sortMap(allPhrases);
		for(Entry<String, MentionType> entry : allPhrases.entrySet()){
			if(content.contains(entry.getKey())){
				content = content.replaceAll("(^|[^a-zA-Z]|\\s)"+Pattern.quote(entry.getKey()).replace("$", "\\$")+"($|[^a-zA-Z]|\\s)", "$1[["+entry.getKey()+" || "+entry.getValue().getValue()+" ]]$2");	                
			}
		}
	}
	
	private void readContent(File file) throws IOException{
		System.out.println("Reading file: "+file.getName());
		byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
		content = new String(encoded, "UTF-8");
		content = content.replace("\r", "");
		content = TextTokenizer.execute(content, caseless);
	}
	
	private Map<String, MentionType> sortMap(Map<String, MentionType> map){
		return map.entrySet().stream().sorted(new StringMentionTypeEntryComparator()).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));	
	}
	
	/**
	 * Compares mentions
	 */
	public class StringMentionTypeEntryComparator implements Comparator<Entry<String, MentionType>> {
	    @Override
	    public int compare(Entry<String, MentionType> e1, Entry<String, MentionType> e2) {
	    	return e2.getKey().length()-e1.getKey().length();
	    }
	}
	
	public String getContent() {
		return content;
	}

	public List<Set<String>> getCoreferences() {
		return coreferences;
	}

}
