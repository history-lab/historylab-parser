package edu.columbia.historylab.ner.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.columbia.historylab.ner.constants.Constants;
import edu.columbia.historylab.ner.constants.NERType;
import edu.columbia.historylab.ner.handlers.AnnotationDocumentReader;
import edu.columbia.historylab.ner.handlers.Config;
import edu.columbia.historylab.ner.handlers.GloveHandler;
import edu.columbia.historylab.ner.handlers.MentionExtractor;
import edu.columbia.historylab.ner.handlers.PostProcessorHandler;
import edu.columbia.historylab.ner.handlers.StanfordNER;
import edu.columbia.historylab.ner.handlers.TextTokenizer;
import edu.columbia.historylab.ner.handlers.WekaHandler;
import edu.columbia.historylab.ner.models.Mention;
import edu.columbia.historylab.ner.models.MentionComparator;
import edu.columbia.historylab.ner.models.WekaModel;

/**
 * History Lab Parser.
 */
public class HLParser {
	
	private static HLParser instance = null;
	private GloveHandler gloveHandler = null;
	private Map<String, String> baseline = null;
	private WekaModel wekaModel = null;
	
	private HLParser(String configPath) throws Exception {
		
		//Initialize Stanford CoreNLP NER
		System.out.println("Initializing Stanford NER...");
		try {
			Config.getInstance(configPath);
		} catch (IllegalArgumentException e) {
			throw new Exception(e);
		}
		
		StanfordNER.getInstance(Config.getInstance(configPath).getCaseless());
		
		//Load word embeddings (Glove)
		System.out.println("Reading the word embeddings...");
		this.gloveHandler = new GloveHandler(Config.getInstance().getGloveModel());
		this.gloveHandler.load(Config.getInstance().getGloveSize());
		
		//Read the baseline
		System.out.println("Reading the baseline...");
		this.baseline = loadTwoColumnList(Config.getInstance().getBaselineModel());
		
		//Read the WEKA model
		System.out.println("Reading the WEKA model...");
		this.wekaModel = WekaHandler.buildModel(Config.getInstance().getTrainData(), Config.getInstance().getTrainModel());
		if ( this.wekaModel == null ) {
			throw new Exception("Failed loading WEKA model from "+
					Config.getInstance().getTrainData()+","+
					Config.getInstance().getTrainModel() + " Make sure model file is from a compatible training.");
		}
		
	} //disable public constructor access.
	
	/**
	 * Returns instance of the parser.
	 */
	public static HLParser getInstance(String configPath) {
		if (instance==null) {
			synchronized(HLParser.class) {
				if ( instance==null ) {
					try {
						instance = new HLParser(configPath);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return instance;
	}
	
	public List<Mention> parse(String text) throws IllegalArgumentException, FileNotFoundException {
		return parse(text, "-1");
	}
	
	public List<Mention> parse(String text, String documentId) throws IllegalArgumentException, FileNotFoundException {

		if(text == null || text.trim().isEmpty() || text.contains(Constants.TEXT_ERROR)){
			throw new IllegalArgumentException("Non null text of some length required.");
		}
		//Process the text
		text = text.replace("\n", " ").trim();
		text = getTokenizedText(text);	
		//Extract the mentions
		MentionExtractor mentionExtractor = new MentionExtractor(text);
		mentionExtractor.extract(Config.getInstance().getCaseless());
		text = mentionExtractor.getContent();

		if(Config.getInstance().isVerbose()) {
			System.out.println(text);
		}
		List<Set<String>> coreferences = mentionExtractor.getCoreferences();
		AnnotationDocumentReader parsedDocumentReader = new AnnotationDocumentReader(text, documentId);
		List<Mention> mentions = parsedDocumentReader.execute(Config.getInstance().includeNER(), Config.getInstance().ignorePronouns(), Config.getInstance().getCaseless());

		if(mentions.size() == 0){
			return mentions;
		}

		//Reset all entity types
		for(Mention mention : mentions){
			mention.setRegularText(mention.getTokenizedText().replaceAll("\\s(\\p{P})", "$1").replaceAll("^[^a-zA-Z\\d]+", "").replaceAll("^([\\[\\(\\{])\\s", "$1").trim());
			mention.setNERType(NERType.NONE);
		}

		//Sort the mentions
		Collections.sort(mentions, new MentionComparator());
		//Apply preprocessing 2
		// TODO: This says "preprocessing", but method is "postProcess" ?
		mentions = PostProcessorHandler.postProcessCandidateMentions2(mentions, text);

		// System.out.println("Generating the Weka file...");
		String queryFile = Config.getInstance().getOutputPath()+"/NER-"+documentId+".arff";
		WekaHandler.generateDataFile(mentions, gloveHandler, queryFile, Config.getInstance().getWindowSize(), Config.getInstance().getIncludeMentionType(), false);
		// System.out.println("Running the classification...");
		WekaHandler.query(wekaModel, Config.getInstance().getTrainModel(), queryFile, Config.getInstance().getClassifier(), mentions, baseline);
		new File(queryFile).deleteOnExit();

		//Apply preprocessing 3
		// TODO: This says "preprocessing", but method is "postProcess" ?
		PostProcessorHandler.postProcessCandidateMentions3(mentions, text);

		//Co-reference Resolution - Step 1
		PostProcessorHandler.applyCoreferences1(mentions, coreferences);
		//Co-reference Resolution - Step 2
		PostProcessorHandler.applyCoreferences2(mentions, coreferences);
		//Co-reference Resolution - Step 3
		PostProcessorHandler.applyCoreferences3(mentions, coreferences);

		//Extra Wiki adjustments
		//Apply preprocessing 4
		// TODO: This says "preprocessing", but method is "postProcess" ?
		PostProcessorHandler.postProcessCandidateMentions4(mentions);

		if (Config.getInstance().hasForcedMentionTypeMappings()) {
			PostProcessorHandler.postProcessForcedMentionToNERTypeMappings(mentions);
		}
		return mentions;
	}

	private static String getTokenizedText(String content){
		content = content.replaceAll("[\\r\\”]", "");
		content = content.replaceAll("\\n\\h*", "\n");
		content = content.replaceAll("\\h*\\n", "\n");
		content = content.replaceAll("\\n+", "\n");
		content = content.replaceAll("^\\n*", "");
		content = content.replaceAll("\\n*$", "");
		content = content.replaceAll("(\\|\\|?|\\[\\[|\\]\\])", " $1 ");
		content = content.replaceAll("\\h+", " ");
		content = content.	replaceAll("\\[\\[", "").
							replaceAll("\\]\\]", "").
							replaceAll("\\h*\\|\\|?\\h*\\d+\\h*", " ").
							replaceAll("\\h+", " ");
		content = TextTokenizer.execute(content, true);
		return content;
	}

	/**
	 * Reading two-column tabular files into a map
	 */
	private static Map<String, String> loadTwoColumnList(String filePath) throws FileNotFoundException, IOException {
		Map<String, String> out = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if(line.length() > 0){
				String[] lineParts = line.split("\\t+");
				out.put(lineParts[0], lineParts[1]);
			}
		}
		br.close();
		return out;
	}
}
