package edu.columbia.historylab.ner.models;

import java.util.List;

import edu.columbia.historylab.ner.constants.MentionType;
import edu.columbia.historylab.ner.constants.NERType;
import edu.columbia.historylab.ner.handlers.Config;

/*
 * Represents an mention (with entity and contextual information).
 */
public class Mention {
	
	private String text;
	private String entityIndex;
	private String entityName;
	private NERType nerType;
	private MentionType mentionType;
	private String tokenizedText;
	private int index;
	private String type;
	private boolean selected;
	private int length;
	private String ppWord;
	private String pWord;
	private String nWord;
	private String nnWord;
	private String fileName;
	private String wikiURL;
	private String regularText;
	
	public Mention(String text, String entityIndex, String entityName, NERType nerType, String tokenizedText, String fileName) {
		this.text = text;
		this.entityIndex = entityIndex;
		this.entityName = entityName;
		this.nerType = nerType;
		this.tokenizedText = tokenizedText;
		this.fileName = fileName;
		this.length = tokenizedText.split("\\s").length;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public String getEntityIndexssss() {
		return entityIndex;
	}
	
	public void setEntityIndexssss(String entityIndex) {
		this.entityIndex = entityIndex;
	}

	public String getEntityName() {
		return entityName;
	}
	
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public NERType getNERType() {
		if(nerType == null){
			return NERType.NONE;
		}
		return nerType;
	}
	public void setNERType(NERType nerType) {
		this.nerType = nerType;
	}
	
	public MentionType getMentionType() {
		if(mentionType == null){
			return MentionType.NONE;
		}
		return mentionType;
	}
	
	public void setMentionType(MentionType mentionType) {
		this.mentionType = mentionType;
	}

	public String getTokenizedText() {
		return tokenizedText;
	}
	public void setTokenizedText(String tokenizedText) {
		this.tokenizedText = tokenizedText;
	}

	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public int getLength() {
		return length;
	}

	public String getPpWord() {
		return ppWord;
	}
	
	public void setPpWord(String ppWord) {
		this.ppWord = ppWord;
	}
	
	public String getPWord() {
		return pWord;
	}
	public void setPWord(String pWord) {
		this.pWord = pWord;
	}

	public String getNWord() {
		return nWord;
	}
	public void setNWord(String nWord) {
		this.nWord = nWord;
	}

	public String getNnWord() {
		return nnWord;
	}
	
	public void setNnWord(String nnWord) {
		this.nnWord = nnWord;
	}

	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getWikiURL() {
		return wikiURL;
	}
	
	public void setWikiURL(String wikiURL) {
		this.wikiURL = wikiURL;
	}

	public String getRegularText() {
		return regularText;
	}
	
	public void setRegularText(String regularText) {
		this.regularText = regularText;
	}
	
	public static void printList(List<Mention> mentions) {
		if(Config.getInstance().isVerbose()) {
			System.out.println("filename\tregular_text\tner_type\tmention_type\twikiurl");
		} else {
			System.out.println("regular_text\tner_type\tmention_type\twikiurl");
		}
		for(Mention mention : mentions){
			if(Config.getInstance().isVerbose()) {
				System.out.println(mention.getFileName()+"\t"+mention.getRegularText()+"\t"+mention.getNERType().toString()+"\t"+mention.getMentionType()+"\t"+mention.getWikiURL());
			} else if(!mention.getNERType().equals(NERType.NONE)){
				System.out.println(mention.getRegularText()+"\t"+mention.getNERType().toString()+"\t"+mention.getMentionType()+"\t"+mention.getWikiURL());
			}
		}
	}
}
