package edu.columbia.historylab.ner.handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xyonix.mayetrix.mayu.text.FoundEntity;

import edu.columbia.historylab.ner.constants.Constants;
import edu.columbia.historylab.ner.constants.MentionType;
import edu.columbia.historylab.ner.constants.NERType;
import edu.columbia.historylab.ner.models.Entity;
import edu.columbia.historylab.ner.models.Mention;
import edu.columbia.historylab.ner.ontology.HLOntology;

/*
 * Reads an annotated file (either a manual one or an automatic as part of the tagging process) into a list of entities 
 * and mentions.
 */
public class AnnotationDocumentReader {

	private String fileName;
	private String content;
	private boolean caseless;
	private Map<String, Entity> entities = new HashMap<String, Entity>();
	private String annotatedText;
	private String originalText;
	private String tokenizedText;
	private String[] words;
	private String[] nerTypes;
	private Map<String, List<Integer>> indexedWords = new HashMap<String, List<Integer>>();
	private List<Mention> mentions = new ArrayList<Mention>();

	public AnnotationDocumentReader(File file){
		try {
			readContent(file);
			fileName = file.getName();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public AnnotationDocumentReader(String content, String fileName) {
		this.content = content;
		this.fileName = fileName;
	}

	/**
	 * Read annotations
	 * 
	 * Steps: 
	 * 
	 * 1- Read the entity section (if exists)
	 * 2- Read the text
	 * 3- Extract the mentions
	 * 4- Extract the context of the mentions
	 * 5- Assign mention types
	 */
	public List<Mention> execute(boolean includeNER, boolean ignorePronouns, boolean caseless){
		this.caseless = caseless;
		readEntities();
		readText();
		extractWordsAndNER(includeNER);
		extractIndexedWords();

		extractMentions(annotatedText, ignorePronouns);
		extractMentionContexts();
		extractMentionTypes();

		return mentions;
	}

	private void readContent(File file) throws IOException{
		byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
		content = new String(encoded, "UTF-8");
		content = content.replace("\r", "");
	}

	//Reading the entity section if exists
	private void readEntities(){
		if(!content.contains("@@@"))
			return;
		String entityRegex = "^(\\d+\\.?)\\s+(\\S[\\s\\S]*\\S)\\s+(LOC|GPE\\.NATION|GPE\\.SPECIAL|ORG\\.NGO|ORG\\.COM|ORG\\.GOV|ORG\\.MED|PER\\.GROUP|PER\\.INDIVIDUAL|PER\\.IND|\\?)$";
		String upperContent = content.split("@@+")[0];
		String[] lines = upperContent.split("\\n");
		for(String line : lines){
			line = line.trim();
			if(line.matches(entityRegex)){
				String index = line.replaceAll(entityRegex, "$1").replace(".",  "");
				String name = line.replaceAll(entityRegex, "$2");
				String nerTypeStr = line.replaceAll(entityRegex, "$3").replace("INDIVIDUAL", "IND").replace(".", "_"); //Ad-hoc replacements
				NERType nerType = NERType.valueOf(nerTypeStr);
				if(nerType == null){
					nerType = NERType.NONE;
				}
				entities.put(index, new Entity(index, name, nerType));
			}
		}
	}

	//Reading the text
	private void readText(){
		annotatedText = content.contains("@@@")?content.split("@@+")[1]:content;
		//Processing
		annotatedText = annotatedText.replaceAll("[\\r\\”]", "");
		annotatedText = annotatedText.replaceAll("\\n\\h*", "\n");
		annotatedText = annotatedText.replaceAll("\\h*\\n", "\n");
		annotatedText = annotatedText.replaceAll("\\n+", "\n");
		annotatedText = annotatedText.replaceAll("^\\n*", "");
		annotatedText = annotatedText.replaceAll("\\n*$", "");
		annotatedText = annotatedText.replaceAll("(\\|\\|?|\\[\\[|\\]\\])", " $1 ");
		annotatedText = annotatedText.replaceAll("\\h+", " ");
		originalText = annotatedText.
				replaceAll("\\[\\[", "").
				replaceAll("\\]\\]", "").
				replaceAll("\\h*\\|\\|?\\h*\\d+\\h*", " ").
				replaceAll("\\h+", " ");
	}

	//Extracting words and NER information
	private void extractWordsAndNER(boolean includeNER){
		tokenizedText = TextTokenizer.execute(originalText, caseless);
		String nerText = includeNER? StanfordNER.getInstance(caseless).applyNER(tokenizedText) : tokenizedText.replaceAll("(\\s)", "/O$1");
		String[] nerWords = nerText.split("\\s+");
		words = new String[nerWords.length];
		nerTypes = new String[nerWords.length];
		for(int i=0; i<nerWords.length; i++){
			String word = nerWords[i].replaceAll("^(.*)\\/(ORGANIZATION|LOCATION|PERSON|O)$", "$1"); //by Stanford CoreNLP
			String ner = nerWords[i].replaceAll("^(.*)\\/(ORGANIZATION|LOCATION|PERSON|O)$", "$2"); //by Stanford CoreNLP
			words[i] = word;
			nerTypes[i] = ner;
		}
	}

	//Indexing words
	private void extractIndexedWords(){
		for(int i=0; i<words.length; i++){
			String word = words[i];
			if(!indexedWords.containsKey(word)){
				indexedWords.put(word, new ArrayList<Integer>());
			}
			indexedWords.get(word).add(i);
		}
	}

	//Extracting the mentions from the annotations
	private void extractMentions(String content, boolean ignorePronouns){
		Pattern regex = Pattern.compile("\\[\\[[^\\[\\]]+\\]\\]");
		String updatedContent = content;
		Matcher regexMatcher = regex.matcher(content);

		//Mention extractions is done in many iterations because of the nested mentions.
		while (regexMatcher.find()) {
			String mentionAnnotation = regexMatcher.group().trim().replaceAll("[\\[\\]]", "");
			String entityIndex = mentionAnnotation.replaceAll("^.*\\|\\|?\\s*(\\d+)", "$1").trim();
			String text = mentionAnnotation.replaceAll("\\|\\|?\\s*\\d+", "").trim();
			String mentionTokenizedText = TextTokenizer.execute(text, caseless);
			if(!(ignorePronouns && text.toUpperCase().matches(Constants.REGEX_PRONOUNS_0))){
				Mention mention = new Mention(text, entityIndex, 
						entities.containsKey(entityIndex)?entities.get(entityIndex).getName():null,
								entities.containsKey(entityIndex)?entities.get(entityIndex).getNERType():NERType.NONE,
										mentionTokenizedText, fileName);
				FoundEntity fe = HLOntology.getInstance().search(text);
				if (fe!=null && fe.getMetadata().containsKey("wikiurl")) {
					mention.setWikiURL("#"+fe.getMetadata().get("wikiurl"));
				}
				try {
					mention.setMentionType(MentionType.getByValue(Integer.parseInt(entityIndex)));
					mentions.add(mention);
				} catch ( Exception e ) {
					System.err.println("** Invalid entity index human annotation in GT, please correct: " + mentionAnnotation);
				}
			}
			updatedContent = updatedContent.replaceAll("\\[\\[\\s*"+Pattern.quote(text)+"\\s*\\|\\|?\\s*\\d+\\s*\\]\\]", text);
		}
		if(updatedContent.equals(content)) {
			return;
		} else {
			extractMentions(updatedContent, ignorePronouns);
		}
	}

	//Extracting the context of the mentions (a window size of 5)
	private void extractMentionContexts() {
		Map<Integer, List<String>> visitedIndexes = new HashMap<Integer, List<String>>();
		for (Mention mention : mentions) {
			String[] mentionWords = mention.getTokenizedText().split("\\s");
			List<Integer> indexes = indexedWords.get(mentionWords[0]);
			if (indexes == null) {
				continue;
			}
			for (int index =0; index<indexes.size(); index++) {
				try {
					int currentIndex = indexes.get(index);
					if(visitedIndexes.containsKey(currentIndex) && visitedIndexes.get(currentIndex).contains(mention.getTokenizedText())){
						continue;
					}
					boolean valid = true;
					for(int i=1; i<mentionWords.length; i++){
						if ( indexedWords.containsKey(mentionWords[i])
								&& !indexedWords.get(mentionWords[i]).contains(currentIndex+i)) {
							valid = false;
							break;
						}
					}

					if(valid) {
						if(!visitedIndexes.containsKey(currentIndex)){
							visitedIndexes.put(currentIndex, new ArrayList<String>());
						}
						visitedIndexes.get(currentIndex).add(mention.getTokenizedText());
						mention.setIndex(currentIndex);
						//Record the previous two and next two words
						mention.setPpWord(mention.getIndex()<=1?"NULL":words[mention.getIndex()-2]);
						mention.setPWord(mention.getIndex()==0?"NULL":words[mention.getIndex()-1]);
						mention.setNWord(mention.getIndex()+mention.getLength()-1==words.length-1?"NULL":words[mention.getIndex()+mention.getLength()]);
						mention.setNnWord(mention.getIndex()+mention.getLength()-1>=words.length-2?"NULL":words[mention.getIndex()+mention.getLength()+1]);
						break;
					}
				} catch(Exception e){
					//e.printStackTrace();
					System.err.println("Problems processing mention: " + mention.getText() +", skipping");
				}
			}
		}
	}

	private void extractMentionTypes() {
		for (Mention mention : mentions) {
			try{
				int startIndex = mention.getIndex();
				String types = "";
				for(int i=startIndex; i<startIndex+mention.getLength(); i++){
					if(!words[i].toUpperCase().matches("^(THE|\\'S|A|AN)$")){
						types+=nerTypes[i];
					}
				}
				String type = "?";
				if (types.contains("PERSON")) {
					type="P";
				} else if(types.matches("^(LOCATION)+$")) {
					type="L";
				} else if(types.matches("^(ORGANIZATION)+$")) {
					type="O";
				}
				mention.setType(type);
			} catch(Exception e){
				throw new RuntimeException(e);
			}
		}
	}

	//Getters

	public String getContent() {
		return content;
	}

	public String getTokenizedText() {
		return tokenizedText;
	}

	public List<Mention> getMentions() {
		return mentions;
	}
	
	public static String getTextFromAnnotated(File file) {
    	AnnotationDocumentReader adr = new AnnotationDocumentReader(new File(file.getAbsolutePath()));
        adr.execute(false, false, true);
        return adr.getTokenizedText();
	}

}
