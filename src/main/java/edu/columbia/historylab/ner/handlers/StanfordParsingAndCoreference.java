package edu.columbia.historylab.ner.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.columbia.historylab.ner.models.StanfordOutput;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

/*
 * Stanford CoreNLP parsing and co-reference resolution (the latter is currently disabled for inefficiency). 
 */
public class StanfordParsingAndCoreference {

	private static StanfordParsingAndCoreference instance;
	private static boolean caselessText = false;
	private StanfordCoreNLP pipeline;
	
	public static StanfordParsingAndCoreference getInstance(boolean caseless) {
		if(instance == null || caseless != caselessText) {
			caselessText = caseless;
			instance = new StanfordParsingAndCoreference();
		}
		return instance;
	}
	
	private StanfordParsingAndCoreference() {
		Properties props = new Properties();
		props.put("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
		if(caselessText) { //for caseless text
			props.put("pos.model", Config.getInstance().getStanfordPOSCaselessModel());
			props.put("parse.model", Config.getInstance().getStanfordParseCaselessModel());
			props.put("ner.model", Config.getInstance().getStanfordNERCaselessModel());
			props.put("serializeTo", Config.getInstance().getStanfordNERCaselessModel());
		}else{ //for true-case text
			props.put("pos.model", Config.getInstance().getStanfordPOSModel());
			props.put("parse.model", Config.getInstance().getStanfordParseModel());;
			props.put("ner.model", Config.getInstance().getStanfordNERModel());
			props.put("serializeTo", Config.getInstance().getStanfordNERModel());
		}
		pipeline = new StanfordCoreNLP(props);
	}

	/**
	 * Executes parsing and co-reference resolution
	 */
	public StanfordOutput execute(String inputText) {
		StanfordOutput StanfordOutput = new StanfordOutput();
		List<String> mentions = new ArrayList<String>();
		List<Set<String>> coreferences = new ArrayList<Set<String>>();
		
		Annotation document = new Annotation(inputText);
		pipeline.annotate(document);
		
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		Tree tree;
		
		//Generate the noun phrases
		for (CoreMap sentence : sentences) {
            tree = sentence.get(TreeAnnotation.class);
            for (Tree subtree : tree) {
            	if (subtree.label().value().matches("NP|NNP|ADJP|PRP|PRP\\$")) {
            		String mention = subtree.flatten().toString().replaceAll("\\(\\S+", "").replaceAll("\\)", "");
            		processMention(mention, mentions);
            	}
            }
		}
		StanfordOutput.setMentions(mentions);

		/**
		 * Stanford CoreNLP co-referrence resolution is disabled because of its low accuracy
		Map<String, Set<String>> coreferences = new HashMap<String, Set<String>>();
		for (CorefChain cc : document.get(CorefChainAnnotation.class).values()) {
			String referenceMention = TextTokenizer.normalize(cc.getRepresentativeMention().toString().replaceAll("in\\ssentence\\s\\d+$", "").replaceAll("^\\s*\\\"", "").replaceAll("\\\"\\s*$", ""));
			for(CorefMention mention : cc.getMentionsInTextualOrder()) {
				String referringMention = TextTokenizer.normalize(mention.toString().replaceAll("in\\ssentence\\s\\d+$", "").replaceAll("^\\s*\\\"", "").replaceAll("\\\"\\s*$", ""));
				if(!referringMention.equals(referenceMention)) {
					if(!coreferences.containsKey(referringMention)) {
						coreferences.put(referringMention, new HashSet<String>());
					}
					coreferences.get(referringMention).add(referenceMention);
				}
			}
		}
		StanfordOutput.setCoreferences(coreferences);
				
		for (CorefChain cc : document.get(CorefChainAnnotation.class).values()) {
			Set<String> current = new HashSet<String>();
			for(CorefMention mention : cc.getMentionsInTextualOrder()) {
				String mentionStr = TextTokenizer.normalize(mention.toString().replaceAll("in\\ssentence\\s\\d+$", "").replaceAll("^\\s*\\\"", "").replaceAll("\\\"\\s*$", ""));
				current.add(mentionStr);
			}
			if(current.size()>1) {
				coreferences.add(current);
			}
		}
		*/
		StanfordOutput.setCoreferences(coreferences);		
		return StanfordOutput;
	}
	
	/**
	 * Extracts the mentions of interest
	 */
	public List<String> extractMentions(String inputText) {
		List<String> mentions = new ArrayList<String>();
		Annotation document = new Annotation(inputText);
		pipeline.annotate(document);

		for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
			List<Mention> corefMentions = sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class);
			for (Mention mention : corefMentions) {
				processMention(mention.toString(), mentions);
				//Handle special cases
				if(mention.toString().toUpperCase().matches("^(.*)(WHICH|THAT|WHO)(.*)$")) {
					processMention(mention.toString().replaceAll("^(.*)(WHICH|which|THAT|that|WHO|who)(\\s.*)$", "$1").trim(), mentions);
				}
				if(mention.toString().split("\\s").length>=3 && mention.toString().toUpperCase().matches("^(\\S+\\s.*)\\s\\S+(ING|ED)\\s.*$")) {
					processMention(mention.toString().replaceAll("^(\\S+\\s.*)\\s\\S+(ING|ing|ED|ed)\\s.*$", "$1"), mentions);
				}
			}
		}
		return mentions;
	}
	
	/**
	 * Generic mention handling
	 */
	private void processMention(String mention, List<String> mentions) {
		mention = TextTokenizer.normalize(mention, caselessText);
    	if(	!mentions.contains(mention) &&
    		!mention.toUpperCase().matches("^(\\'|IN|ON|AT|OF|TO|FROM|ABOUT|AND|OR)($|\\s.*$)") &&
    		(!caselessText || !mention.matches("^.*[a-z].*$")) &&
    		mention.matches("^.*[a-zA-Z].*$") && 
    		!mention.endsWith("'") && ! mention.matches("^.*\\d\\s?\\/\\s?\\d.*$") &&
    		!mention.contains("*") &&
    		mention.split("\\s").length<=8) {
    		mentions.add(mention);
    	}
	}

}
