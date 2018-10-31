package edu.columbia.historylab.ner.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.columbia.historylab.ner.constants.Constants;
import edu.columbia.historylab.ner.constants.NERType;
import edu.columbia.historylab.ner.handlers.AnnotationDocumentReader;
import edu.columbia.historylab.ner.handlers.Config;
import edu.columbia.historylab.ner.handlers.GloveHandler;
import edu.columbia.historylab.ner.handlers.MentionExtractor;
import edu.columbia.historylab.ner.handlers.PostProcessorHandler;
import edu.columbia.historylab.ner.handlers.StanfordNER;
import edu.columbia.historylab.ner.handlers.StanfordParsingAndCoreference;
import edu.columbia.historylab.ner.handlers.WekaHandler;
import edu.columbia.historylab.ner.models.Mention;
import edu.columbia.historylab.ner.models.MentionComparator;

public class HLParserBuilder {

	public static void prepTestTrainData(String configPath, boolean runBaseline) throws IOException {
		Config conf = Config.getInstance(configPath);

		String trainFile = conf.getModelPath()+"/"+ Constants.TRAIN_DATA;
		String devFile = conf.getModelPath()+"/"+ Constants.DEV_DATA;
		String testFile = conf.getModelPath()+"/"+ Constants.TEST_DATA;
		String baselineFile = conf.getModelPath()+"/baseline.txt";

		//Initialize Stanford CoreNLP NER and parsing/co-reference resolution.
		System.out.println("Initializing Stanford NER...");
		StanfordNER.getInstance(true);
		System.out.println("Initializing Stanford Parsing/Coreference Resolution...");
		StanfordParsingAndCoreference.getInstance(true);

		//Load word embeddings (Glove)
		System.out.println("Reading the word embeddings...");
		GloveHandler gloveHandler = new GloveHandler(conf.getGloveModel());
		gloveHandler.load(conf.getGloveSize());

		//Read the training daa
		System.out.println("Reading the training data from: " + conf.getTrainDataDir());
		// TODO: Make "300" a parameter - maximum number of files to read
		List<List<Mention>> trainOutput = readData(conf.getTrainDataDir(), 300);
		System.out.println("Number of mentions: "+trainOutput.get(1).size());

		//Generate the baseline
		Map<String, NERType> baseline = new HashMap<String, NERType>();
		if(runBaseline){
			try {
				baseline = getBaseline(trainOutput.get(0));
				PrintWriter pw = new PrintWriter(baselineFile);
				for(Entry<String, NERType> entry : baseline.entrySet()){
					pw.write(entry.getKey()+"\t"+entry.getValue().toString().replace("_", ".")+"\n");
				}
				pw.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		//Read the development data
		System.out.println("Reading the development data...");
		List<List<Mention>> devOutput = readData(conf.getDevDataDir(), 200);
		System.out.println("Number of mentions: "+devOutput.get(1).size());

		//Read the test data
		System.out.println("Reading the test data...");
		List<List<Mention>> testOutput = readData(conf.getTestDataDir(), 200);
		System.out.println("Number of mentions: "+testOutput.get(1).size());
		
		//Generate the WEKA files
		System.out.println("Generating the WEKA files...");
		WekaHandler.generateDataFile(trainOutput.get(1), gloveHandler, trainFile, conf.getWindowSize(), conf.getIncludeMentionType(), conf.getWeightedTraining());
		WekaHandler.generateDataFile(devOutput.get(1), gloveHandler, devFile, conf.getWindowSize(), conf.getIncludeMentionType(), false);
		WekaHandler.generateDataFile(testOutput.get(1), gloveHandler, testFile, conf.getWindowSize(), conf.getIncludeMentionType(), false);

		System.out.println("Train/Dev/Test prep completed. Data in: " + conf.getModelPath());
	}

	/**
	 * Read data, generate mentions and filter the files
	 */
	private static List<List<Mention>> readData(String dataDirectory, int size){
		List<List<Mention>> output = new ArrayList<List<Mention>>();
		try{
			List<Mention> allActualMentions = new ArrayList<Mention>();
			List<Mention> allCandidateMentions = new ArrayList<Mention>();
			int count=0;
			File[] files = new File(new File(dataDirectory).getAbsolutePath()).listFiles(File::isFile);
			for(File file : Arrays.asList(files)){
				if(!file.getName().endsWith(".txt")){
					continue;
				}

				if(count++ > size){
					continue;
				}
				System.out.println("Processing "+file.getName());
				AnnotationDocumentReader annoDocumentReader = new AnnotationDocumentReader(new File(file.getAbsolutePath()));

				List<Mention> actualMentions = null;
				try {
					actualMentions = annoDocumentReader.execute(false, false, true);
				} catch (Exception e) {
					System.out.println("Problems getting mentions from AnnotationDocumentReader");
					e.printStackTrace();
					continue;
				}

				//Run parsing on the data and generate mentions
                String parsedText = annoDocumentReader.getTokenizedText();
                MentionExtractor mentionExtractor = new MentionExtractor(parsedText);
                mentionExtractor.extract(true);
                parsedText = mentionExtractor.getContent();
                AnnotationDocumentReader parsedDocumentReader = new AnnotationDocumentReader("@@@\n"+parsedText, file.getName());

				//Ignore cases with no mentions
				List<Mention> candidateMentions = parsedDocumentReader.execute(false, false, true);
				if(candidateMentions == null){
					continue;
				}

				//Ignore cases with bad alignments
				if(!annoDocumentReader.getTokenizedText().equals(parsedDocumentReader.getTokenizedText())){
					continue;
				}

				//Sort the mentions
				Collections.sort(candidateMentions, new MentionComparator());
				//Apply preprocessing 2
				PostProcessorHandler.postProcessCandidateMentions2(candidateMentions, parsedDocumentReader.getTokenizedText());

				int actual = 0;
				for(Mention actualMention : actualMentions){
					if(actualMention.getNERType() == null){
						continue;
					}
					actual++;
				}

				int match=0;
				for(Mention candidateMention : candidateMentions){
					boolean validMention = false;
					for(Mention actualMention : actualMentions){
						if(actualMention.getNERType() == null){
							continue;
						}
						if(actualMention.getIndex()==candidateMention.getIndex() && actualMention.getLength()==candidateMention.getLength()){
							candidateMention.setNERType(actualMention.getNERType()==null?NERType.NONE:actualMention.getNERType());
							validMention = true;
							break;
						}
					}
					if(!validMention){
						candidateMention.setNERType(NERType.NONE);
					}else{
						match++;
					}
				}

				// Only consider the files with high recall  ( >= 75% )
				if( (float)match/(float)actual < 0.75 ) {
                    System.out.println("#### Warning: low recall: "
                            +file.getName()
                            +", actual "+actual
                            +", match "+match+"/"+candidateMentions.size()
                            +", recall "+((float)match/(float)actual)
                    );
                }
				allActualMentions.addAll(actualMentions);
				allCandidateMentions.addAll(candidateMentions);
			}
			output.add(allActualMentions);
			output.add(allCandidateMentions);
		} catch(Exception e){
			e.printStackTrace();
		}

		return output;
	}

	/**
	 * Generating the baseline (MLE)
	 */
	private static Map<String, NERType> getBaseline(List<Mention> mentions){
		Map<String, NERType> mentionLookup = new HashMap<String, NERType>();

		Map<String, Map<NERType, Integer>> counts = new HashMap<String, Map<NERType,Integer>>();
		for(Mention mention : mentions){
			if(mention.getNERType() == null){
				continue;
			}
			String text = mention.getTokenizedText();
			if(text.trim().equals("")){
				continue;
			}
			if(!counts.containsKey(text)){
				counts.put(text, new HashMap<NERType, Integer>());
			}
			if(!counts.get(text).containsKey(mention.getNERType())){
				counts.get(text).put(mention.getNERType(), 0);
			}
			counts.get(text).put(mention.getNERType(), counts.get(text).get(mention.getNERType())+1);
		}

		for(Entry<String, Map<NERType, Integer>> entry1 : counts.entrySet()){
			int max = -1;
			NERType type = NERType.NONE;
			for(Entry<NERType, Integer> entry2 : entry1.getValue().entrySet()){
				if(entry2.getValue()>max){
					type = entry2.getKey();
					max = entry2.getValue();
				}
			}
			if(max >= 2){
				mentionLookup.put(entry1.getKey(), type);
			}
		}
		return mentionLookup;
	}

	/**
	 * Trains a new HLParser using apriori generated train.arff.
	 */
	public static void train(String configPath) throws Exception {
		WekaHandler.trainModel(Config.getInstance(configPath).getTrainData(), 
    			Config.getInstance(configPath).getTrainModel(), Config.getInstance(configPath).getNumFolds(), 0L);
	}
	
	/**
	 * Evaluate predicted mentions against the actual ones
	 */
	public static void evaluate(List<Mention> actualMentions, List<Mention> predictedMentions, String label){

		int mention = 0;
		int type=0;
		int super_type = 0;
		int totalActual = 0;
		int totalPredicted = predictedMentions.size();
		for(Mention actual : actualMentions){
			if(actual.getNERType() == null){
				continue;
			}
			totalActual++;
			for(Mention predicted : predictedMentions){
				if(actual.getFileName().equals(predicted.getFileName()) && actual.getIndex() == predicted.getIndex() && actual.getLength() == predicted.getLength()){
					mention++;
					double actualClsLabel = actual.getNERType().getValue();
					double predictedClsLabel = predicted.getNERType().getValue();
					if(actualClsLabel==predictedClsLabel){
						type++;
					}
					if(predictedClsLabel > 0){
						if(	(actualClsLabel <= 3 && predictedClsLabel <= 3) ||
								(actualClsLabel >= 4 &&  actualClsLabel <= 7 && predictedClsLabel >= 4 && predictedClsLabel <= 7) ||
								(actualClsLabel >= 8 && predictedClsLabel >= 8)){
							super_type++;
						} 
					}
					break;
				}
			}
		}
		int onlyActual = totalActual-mention;
		int onlyPredicted = totalPredicted-mention;

		float mentionRecall = (float)mention/(float)(mention+onlyActual);
		float mentionPrecision = (float)mention/(float)(mention+onlyPredicted);
		double mentionFScore = (float)(2*mentionRecall*mentionPrecision)/(float)(mentionRecall+mentionPrecision);

		float typeLocalAccuracy = (float)(type)/(float)mention;
		float typeRecall = (float)type/(float)(mention+onlyActual);
		float typePrecision = (float)type/(float)(mention+onlyPredicted);
		double typeFScore = (float)(2*typeRecall*typePrecision)/(float)(typeRecall+typePrecision);
		float superTypeLocalAccuracy = (float)(super_type)/(float)mention;
		float superTypeRecall = (float)super_type/(float)(mention+onlyActual);
		float superTypePrecision = (float)super_type/(float)(mention+onlyPredicted);
		double superTypeFScore = (float)(2*superTypeRecall*superTypePrecision)/(float)(superTypeRecall+superTypePrecision);

		DecimalFormat df = new DecimalFormat("#.####");
		String result = "#RESULTS2"+"\t"+label+"\t"+
				totalActual+"/"+totalPredicted+"/"+mention+"/"+type+"/"+super_type+"/"+onlyActual+"/"+onlyPredicted+"\t"+
				df.format(mentionRecall)+"\t"+
				df.format(mentionPrecision)+"\t"+
				df.format(mentionFScore)+"\t"+
				df.format(typeLocalAccuracy)+"\t"+
				df.format(typeRecall)+"\t"+
				df.format(typePrecision)+"\t"+
				df.format(typeFScore)+"\t"+
				df.format(superTypeLocalAccuracy)+"\t"+
				df.format(superTypeRecall)+"\t"+
				df.format(superTypePrecision)+"\t"+
				df.format(superTypeFScore);
		System.out.println(result);
	}
}
