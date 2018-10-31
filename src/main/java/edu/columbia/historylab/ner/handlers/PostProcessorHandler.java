package edu.columbia.historylab.ner.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import edu.columbia.historylab.ner.constants.Constants;
import edu.columbia.historylab.ner.constants.MentionType;
import edu.columbia.historylab.ner.constants.NERType;
import edu.columbia.historylab.ner.models.Mention;
import edu.stanford.nlp.util.StringUtils;

/*
 * Post-processor for NER recognition and Wikification.
 */
public class PostProcessorHandler {
	
	/**
	 * Post-processing for special cases (2)
	 */
	public static List<Mention> postProcessCandidateMentions2(List<Mention> mentions, String text) {
		
		//Handle apostrophes 
		for(int i=0; i<mentions.size(); i++) {
			if(i>0 && mentions.get(i).getIndex() == mentions.get(i-1).getIndex()) {
				if(mentions.get(i).getTokenizedText().toUpperCase().replace(" ", "").equals(mentions.get(i-1).getTokenizedText().toUpperCase().replace(" ", "")+"'S")) {
					mentions.get(i-1).setIndex(-1);
				} else if((mentions.get(i).getTokenizedText().toUpperCase().replace(" ", "")+"'S").equals(mentions.get(i-1).getTokenizedText().toUpperCase().replace(" ", ""))) {
					mentions.get(i).setIndex(-1);
				} else if(mentions.get(i).getTokenizedText().toUpperCase().replace(" ", "").equals(mentions.get(i-1).getTokenizedText().toUpperCase().replace(" ", "")+"'")) {
					mentions.get(i-1).setIndex(-1);
				} else if((mentions.get(i).getTokenizedText().toUpperCase().replace(" ", "")+"'").equals(mentions.get(i-1).getTokenizedText().toUpperCase().replace(" ", ""))) {
					mentions.get(i).setIndex(-1);
				}
			}
			if(i>0 && mentions.get(i).getIndex() == mentions.get(i-1).getIndex()+1) {
				for (int j=0; j<Constants.CLOSED_CLASSES.length; j++) {
					if(mentions.get(i).getTokenizedText().toUpperCase().replace(" ", "").equals(Constants.CLOSED_CLASSES[j]+mentions.get(i-1).getTokenizedText().toUpperCase().replace(" ", ""))) {
						mentions.get(i-1).setIndex(-1);
						break;
					} else if((Constants.CLOSED_CLASSES[j]+mentions.get(i).getTokenizedText().toUpperCase().replace(" ", "")).equals(mentions.get(i-1).getTokenizedText().toUpperCase().replace(" ", ""))) {
						mentions.get(i).setIndex(-1);
						break;
					}
				}
			}
			
			//Handle special titles
			if(mentions.get(i).getTokenizedText().matches("^(MR|MRS|MISS|DR|SIR)\\.?$")) {
				mentions.get(i).setIndex(-1);
			}
		}
		
		List<Mention> updatedMentions = new ArrayList<Mention>();
		for(Mention mention : mentions) {
			if(mention.getIndex()==-1) {
				continue;
			}
			updatedMentions.add(mention);
		}
		return updatedMentions;
	}
	
	/**
	 * Post-processing for special cases (3)
	 */
	public static List<Mention> postProcessCandidateMentions3(List<Mention> mentions, String text) {
		for(Mention mention : mentions) {
			if(!mention.getNERType().equals(NERType.NONE)) {
				if(mention.getTokenizedText().matches("^((THE|The|the)\\s)?US$") && (mention.getNERType().equals(NERType.GPE_NATION) || mention.getNERType().equals(NERType.GPE_SPECIAL) || mention.getNERType().equals(NERType.LOC))) {
					mention.setWikiURL("United_States");
				}
				if(mention.getTokenizedText().matches("^((THE|The|the)\\s)?UN$")) {
					mention.setWikiURL("United_Nations");
				}
				if(mention.getWikiURL() != null && (mention.getWikiURL().matches("^\\#?Government.*$") || mention.getWikiURL().matches("^\\#?Politics.*$"))) {
					mention.setNERType(NERType.GPE_NATION);
				}
			}
		}
		return mentions;
	}
	
	public static List<Mention> postProcessCandidateMentions4(List<Mention> mentions) {
		for(Mention mention : mentions) {
			if(mention.getNERType().equals(NERType.PER_IND)) {
				for(Mention otherMention : mentions) {
					if(otherMention.getNERType().equals(NERType.PER_IND) && !otherMention.getTokenizedText().equals(mention.getTokenizedText()) && otherMention.getTokenizedText().matches("^.*\\b"+Pattern.quote(mention.getTokenizedText()).replace("$", "\\$")+"\\b.*$")) {
						mention.setWikiURL(otherMention.getWikiURL());
					}
				}
			}
			if(mention.getWikiURL() != null && mention.getWikiURL().equals("#United_States")) {
				mention.setWikiURL("United_States");
			}
		}
		return mentions;
	}
	
	public static List<Mention> postProcessForcedMentionToNERTypeMappings(List<Mention> mentions) {
		for(Mention mention : mentions) {
			for(MentionType mt:Config.getInstance().getForcedMentionTypeMappings()) {
				if(mention.getMentionType()==mt) {
					mention.setNERType(Config.getInstance().getForcedNERTypeMapping(mt));					
				}	
			}
		}
		return mentions;
	}
	
	//Resolving co-references and their Wiki URLs (1)
	public static void applyCoreferences1(List<Mention> mentions, List<Set<String>> coreferences) {
		Map<NERType, String> typeWikiMap = new HashMap<NERType, String>();		
		Map<String, String> textWikiMap = new HashMap<String, String>();

		for(Mention mention : mentions) {
						
			if(mention.getNERType().equals(NERType.NONE)) {
				continue;
			}

			String mentionText = mention.getTokenizedText();

			if(mentionText.toUpperCase().matches(Constants.REGEX_PRONOUNS_1)) {
				//None
			} else if(mentionText.toUpperCase().matches(Constants.REGEX_PRONOUNS_2)) {
				//None
			} else if(mentionText.toUpperCase().matches("^(IT|ITS|ITSELF)($|\\s.*$)")) {
				//None
			} else {
				if(mention.getMentionType().equals(MentionType.NONE) && mention.getWikiURL() == null) {
					if(textWikiMap.get(mentionText) != null) {
						mention.setWikiURL(textWikiMap.get(mentionText));
					} else if(typeWikiMap.get(mention.getNERType()) != null) {
						mention.setWikiURL(typeWikiMap.get(mention.getNERType()));
					} else {
						setDefaultWikiURL(mention);
					}
				}
				typeWikiMap.put(mention.getNERType(), mention.getWikiURL());
				textWikiMap.put(mentionText, mention.getWikiURL());

			}
		}
	}
	
	/**
	 * Resolving co-references and their Wiki URLs (1)
	 */
	public static void applyCoreferences2(List<Mention> mentions, List<Set<String>> coreferences) {
		Map<NERType, String> typeWikiMap = new HashMap<NERType, String>();
		Map<String, String> textWikiMap = new HashMap<String, String>();

		NERType lastNonPersonType = null;
		String lastNonPersonWiki = null;
		
		for(Mention mention : mentions) {
					
			if(mention.getNERType().equals(NERType.NONE)) {
				continue;
			}
			
			String mentionText = mention.getTokenizedText();

			if(mentionText.toUpperCase().matches(Constants.REGEX_PRONOUNS_1)) {
				mention.setNERType(NERType.PER_IND);
				if(typeWikiMap.get(NERType.PER_IND) != null) {
					mention.setWikiURL(typeWikiMap.get(NERType.PER_IND));	
				}
			} else if(mentionText.toUpperCase().matches(Constants.REGEX_PRONOUNS_2)) {
				if(typeWikiMap.get(mention.getNERType()) != null) {
					mention.setWikiURL(typeWikiMap.get(mention.getNERType()));	
				}
			} else if(mentionText.toUpperCase().matches("^(IT|ITS|ITSELF)($|\\s.*$)")) {
				if(lastNonPersonType != null) {
					mention.setNERType(lastNonPersonType);
					mention.setWikiURL(lastNonPersonWiki);
				}
			} else {
				if(mention.getMentionType().equals(MentionType.NONE) && mention.getWikiURL() == null) {
					if(textWikiMap.get(mentionText) != null) {
						mention.setWikiURL(textWikiMap.get(mentionText));
					} else if(typeWikiMap.get(mention.getNERType()) != null) {
						mention.setWikiURL(typeWikiMap.get(mention.getNERType()));
					}
				}
				
				if(mention.getWikiURL() == null) {
					setDefaultWikiURL(mention);
				}
				
				typeWikiMap.put(mention.getNERType(), mention.getWikiURL());
				textWikiMap.put(mentionText, mention.getWikiURL());

				if(mention.getNERType() != NERType.PER_IND && mention.getNERType() != NERType.PER_GROUP) {
					lastNonPersonType = mention.getNERType();
					lastNonPersonWiki = mention.getWikiURL();
				}
			}
		}
	}
	
	/**
	 * Set default Wiki URLs for mentions without Wiki URLs
	 */
	public static void applyCoreferences3(List<Mention> mentions, List<Set<String>> coreferences) {
		for(Mention mention : mentions) {		
			if(mention.getNERType().equals(NERType.NONE)) {
				continue;
			}
			if(mention.getWikiURL() == null) {
				setDefaultWikiURL(mention);
			}	
		}
	}
	
	/**
	 * A default Wiki URL is the same as the mention but after upper-casing the initials and removing the determiners
	 */
	private static void setDefaultWikiURL(Mention mention) {
		if ( mention.getRegularText()==null ) {
			mention.setWikiURL("#UNKNOWN");
			return;
		}
		String wikiURL = mention.getRegularText().replace(" ", "_").toLowerCase();
		if(wikiURL.length()>2) {
			wikiURL = uppercaseInitials(mention.getRegularText());
		    wikiURL = wikiURL.replace("-_", "-");
		    wikiURL = wikiURL.replaceAll("\\'s$", "");
		} else {
			wikiURL = wikiURL.toUpperCase();
		}
		wikiURL = wikiURL.replaceAll("^(The|A|An)\\_", "");
	    mention.setWikiURL("#"+wikiURL);
	}
	
	//Upper-casing initials
	private static String uppercaseInitials(String str) {
		String[] words = str.toLowerCase().split("[\\s\\_]");
		String output = "";
		for(int i=0; i<words.length; i++) {
			output+=StringUtils.capitalize(words[i])+(i<words.length-1?"_":"");
		}
		return output;
	}

}
