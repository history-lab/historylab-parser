package edu.columbia.historylab.ner.models;

import java.util.List;
import java.util.Set;

/*
 * Represents Stanford CoreNLP extracted mentions and their co-references.
 */
public class StanfordOutput {
	
	private List<String> mentions;
	private List<Set<String>> coreferences;
	
	public StanfordOutput() {
	}
	
	public StanfordOutput(List<String> mentions, List<Set<String>> coreferences) {
		this.mentions = mentions;
		this.coreferences = coreferences;
	}
		
	public List<String> getMentions() {
		return mentions;
	}
	
	public void setMentions(List<String> mentions) {
		this.mentions = mentions;
	}
	
	public List<Set<String>> getCoreferences() {
		return coreferences;
	}
	
	public void setCoreferences(List<Set<String>> coreferences) {
		this.coreferences = coreferences;
	}

}
