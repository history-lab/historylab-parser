package edu.columbia.historylab.ner.handlers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Reads and records word embeddings.
 */
public class GloveHandler {
	
	//These are special cases for equivalent words
	private static String[] from = {"AMEMBASSY", "FRG", "USG", "EMBOFF", "EMBOFFS", "USLO", "HCR", "FORMIN", "FORMINS", "NEB", "US"};
	private static String[] to = {"AMERICAN EMBASSY", "FEDERAL REPUBLIC OF GERMANY", "USA GOVERNMENT", "EMBASSY OFFICER", "EMBASSY OFFICERS", "USA LIAISON OFFICER", "HOUSE CONCURRENT RESOLUTION", "FOREIGN MINISTER", "FOREIGN MINISTERS", "NEBRASKA", "USA"};

	private String filePath;
	private Map<String, String[]> vectors;
	private Map<String, String> conversions;
	
	public GloveHandler(String filePath){
		this.filePath = filePath;
		conversions = new HashMap<String, String>();
		//Handle special cases
		for(int i=0; i<from.length; i++){
			conversions.put(from[i], to[i]);
		}
	}
	
	/**
	 * Loads word embeddings
	 */
	public Map<String, String[]> load(int size) throws IOException{
		System.out.println("Loading " + size + " word embeddings");
		vectors = new HashMap<String, String[]>();
		BufferedReader br = null;

		int count = 0;
		br = new BufferedReader(new FileReader(filePath));
		String line;
		while ((line = br.readLine()) != null && (count++<size || size == -1)) {
			if(line.split("\\s").length==301){
				String word = line.replaceAll("^(\\S+)\\s(.*)$", "$1");
				String[] vector = line.replaceAll("^(\\S+)\\s(.*)$", "$2").split("\\s");
				vectors.put(word, vector);
			}
		}
		br.close();
		String[] zeroVector = new String[300];
		for(int i=0; i<300; i++){
			zeroVector[i]="0";
		}
		vectors.put("NONE", zeroVector);
		
		String[] nullVector = new String[300];
		for(int i=0; i<300; i++){
			nullVector[i]="?";
		}
		vectors.put("NULL", nullVector);
			
		return vectors;
	}
	
	public Map<String, String[]> getVectors() {
		return vectors;
	}
	
	public String[] getVectorOfWord(String word){
		if(word==null){
			return vectors.get("NONE");
		}
		word = word.toLowerCase().trim();
		if(!vectors.containsKey(word)){
			return vectors.get("NONE");
		}
		return vectors.get(word);
	}
	
	/**
	 * Getting the vectors of a list of words by minimum values across the word vectors
	 */
	public String[] getVectorOfWordsByMinimum(String words){
		for(int i=0; i<to.length; i++){
			words = words.replaceAll("\\b"+from[i]+"\\b", to[i]);
		}
		if(!words.matches("^(THE|\\'S|A|AN)$")){
			words = words.replaceAll("\\b(THE|\\'S|A|AN)\\b", "");
			words = words.replaceAll("\\s+", " ");	
		}
		String[] wordList = words.split("\\s");
		List<String[]> vectors = new ArrayList<String[]>();
		for(int i=0; i<wordList.length; i++){
			vectors.add(getVectorOfWord(wordList[i]));
		}
		String[] outputVector = new String[300];
		for(int i=0; i<300; i++){
			double minimum = 100;
			for(String[] vector : vectors){
				if(Double.parseDouble(vector[i])<minimum){
					minimum = Double.parseDouble(vector[i]);
				}	
			}
			outputVector[i] = minimum+"";
		}
		return outputVector;
	}
	
	/**
	 * Return word vector by maximum values across the word vectors
	 */
	public String[] getVectorOfWordsByMaximum(String words){
		for(int i=0; i<to.length; i++){
			words = words.replaceAll("\\b"+from[i]+"\\b", to[i]);
		}
		if(!words.matches("^(THE|\\'S|A|AN)$")){
			words = words.replaceAll("\\b(THE|\\'S|A|AN)\\b", "");
			words = words.replaceAll("\\s+", " ");	
		}
		String[] wordList = words.split("\\s");
		List<String[]> vectors = new ArrayList<String[]>();
		for(int i=0; i<wordList.length; i++){
			vectors.add(getVectorOfWord(wordList[i]));
		}
		String[] outputVector = new String[300];
		for(int i=0; i<300; i++){
			double maximum = -100;
			for(String[] vector : vectors){
				if(Double.parseDouble(vector[i])>maximum){
					maximum = Double.parseDouble(vector[i]);
				}	
			}
			outputVector[i] = maximum+"";
		}
		return outputVector;
	}
	
	/**
	 * Return by mean values across the word vectors
	 */
	public String[] getVectorOfWordsByMean(String words){
		for(int i=0; i<to.length; i++) {
			words = words.replaceAll("\\b"+from[i]+"\\b", to[i]);
		}
		if(!words.matches("^(THE|\\'S|A|AN)$")) {
			words = words.replaceAll("\\b(THE|\\'S|A|AN)\\b", "");
			words = words.replaceAll("\\s+", " ");	
		}
		String[] wordList = words.split("\\s");
		List<String[]> vectors = new ArrayList<String[]>();
		for(int i=0; i<wordList.length; i++){
			vectors.add(getVectorOfWord(wordList[i]));
		}
		String[] outputVector = new String[300];
		for(int i=0; i<300; i++){
			double sum = 0;
			for(String[] vector : vectors){
				sum+=Double.parseDouble(vector[i]);
			}
			if(sum == 0) {
				outputVector[i] = "0";
			} else {
				outputVector[i] = new DecimalFormat("#.##").format((float)sum/(float)vectors.size());
			}
		}
		return outputVector;
	}
}
