package edu.columbia.historylab.ner.handlers;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.columbia.historylab.ner.constants.MentionType;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * Stanford Core NER executor.
 */
public class StanfordNER {

	private static StanfordNER instance;
	private static boolean caselessText = false;
	private AbstractSequenceClassifier<CoreLabel> classifier;
	
	public static StanfordNER getInstance(boolean caseless) {
		if (instance == null || caseless != caselessText) {
			caselessText = caseless;
			try {
				instance = new StanfordNER();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return instance;
	}
	
	private StanfordNER() throws ClassCastException, ClassNotFoundException, IOException {
		if (caselessText) {  //for caseless text
			classifier = CRFClassifier.getClassifier(Config.getInstance().getStanfordNERCaselessModel());
		} else {  //for true-case text
			classifier = CRFClassifier.getClassifier(Config.getInstance().getStanfordNERModel());
		}
	}
	
	/**
	 * Running Stanford CoreNLP NER
	 */
	public String applyNER(String inputText) {
		try {
			String[] lines = inputText.split("\\n");
			String outputText = "";
			for(int i=0; i<lines.length; i++) {
				String text = classifier.classifyToString(lines[i]);
			    outputText+=text.trim()+(i<lines.length-1?"\n":"");
			}
			outputText = TextTokenizer.normalize(outputText, caselessText);
			outputText = outputText.replaceAll("\\s\\/\\s(O|PERSON|LOCATION|ORGANIZATION)\\b", "/$1");
			return outputText;
		} catch(Exception e) {
			e.printStackTrace();
			return inputText;
		}
	}
	
	/**
	 * Extracts named entities given generated tags
	 */
	public static Map<String, MentionType> getNamedEntities(String text) {
		Map<String, MentionType> map = new HashMap<String, MentionType>();
		String[] words = text.split("\\s+");
		MentionType previousWordType = MentionType.NONE;
		String currentMention = "";
		for(int i=0; i<words.length; i++) {
			MentionType currentWordType = MentionType.NONE;
			String word = words[i];
			if (word.endsWith("/ORGANIZATION")) {
				currentWordType = MentionType.STANFORDNER_ORG;
			} else if (word.endsWith("/LOCATION")) {
				currentWordType = MentionType.STANFORDNER_LOC;
			} else if (word.endsWith("/PERSON")) {
				currentWordType = MentionType.STANFORDNER_PER;
			}
			if (currentWordType == previousWordType) {
				if (previousWordType != MentionType.NONE) {
					currentMention += word.replaceAll("\\/(O|PERSON|LOCATION|ORGANIZATION)\\b", "") + " ";
				}
			} else {
				if (previousWordType != MentionType.NONE) {
					map.put(currentMention.trim(), previousWordType);
					currentMention = "";
				}
				if (currentWordType != MentionType.NONE) {
					currentMention = word.replaceAll("\\/(O|PERSON|LOCATION|ORGANIZATION)\\b", "") + " ";
				}
			}
			previousWordType = currentWordType;
		}
		if (currentMention.length()>0) {
			map.put(currentMention.trim(), previousWordType);
		}
		return map;
	}

}
