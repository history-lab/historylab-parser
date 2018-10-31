package edu.columbia.historylab.ner.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.xyonix.mayetrix.mayu.text.FoundEntity;

import edu.columbia.historylab.ner.constants.NERType;
import edu.columbia.historylab.ner.models.Mention;
import edu.columbia.historylab.ner.models.WekaModel;
import edu.columbia.historylab.ner.ontology.HLOntology;
import edu.stanford.nlp.util.StringUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.lazy.LWL;
import weka.classifiers.rules.OneR;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;

/*
 * Utilizes WEKA for machine-learning training and classification.
 */
public class WekaHandler {
	
	/**
	 * Generates a WEkA file given instances
	 * @throws FileNotFoundException 
	 */
	public static void generateDataFile(List<Mention> mentions, GloveHandler gloveHandler, String outputFile, int windowSize, boolean includeMentionType, boolean duplicate) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new File(outputFile));
		writeHeader(pw, windowSize, includeMentionType);
		for(Mention mention : mentions) {
			if(mention.getNERType() == null) {
				continue;
			}
			StringBuilder featureSB = new StringBuilder();
			if(includeMentionType) {
				featureSB.append(mention.getMentionType().toString()+",");
			}
			if(windowSize>=4) {
				featureSB.append(StringUtils.join(gloveHandler.getVectorOfWord(mention.getPpWord()), ",")+",");
			}
			if(windowSize>=2) {
				featureSB.append(StringUtils.join(gloveHandler.getVectorOfWord(mention.getPWord()), ",")+",");
			}
			featureSB.append(StringUtils.join(gloveHandler.getVectorOfWordsByMean(mention.getTokenizedText()), ",")+",");
			if(windowSize>=3) {
				featureSB.append(StringUtils.join(gloveHandler.getVectorOfWord(mention.getNWord()), ",")+",");
			}
			if(windowSize>=5) {
				featureSB.append(StringUtils.join(gloveHandler.getVectorOfWord(mention.getNnWord()), ",")+",");
			}
			featureSB.append(mention.getNERType().toString().replace("_", "."));
			pw.write(featureSB.toString()+"\n");
			//For Balancing
			if(duplicate && !mention.getNERType().toString().equals("NONE")) {
				pw.write(featureSB.toString()+"\n");
				pw.write(featureSB.toString()+"\n");
				pw.write(featureSB.toString()+"\n");
				pw.write(featureSB.toString()+"\n");
			}
		}
		pw.close();
	}
	
	/**
	 * Generates a WEKA-file header
	 */
	private static void writeHeader(PrintWriter pw, int windowSize, boolean includeMentionType) {
		pw.write("@RELATION NER"+"\n");
		
		//Regular features
		if(includeMentionType) {
			pw.write("@ATTRIBUTE a"+0+" {NONE,COUNTRY,CITY,STATE,REGION,NATIONALITY,ORGANIZATION,PERSON,OCCUPATION,TITLE,STANFORDNER_ORG,STANFORDNER_LOC,STANFORDNER_PER}"+"\n");
		}
		for(int i=1; i<=(windowSize*300); i++) {
			pw.write("@ATTRIBUTE a"+i+" NUMERIC"+"\n");
		}
		
		//Class feature
		pw.write("@ATTRIBUTE class "+"{NONE,LOC,GPE.NATION,GPE.SPECIAL,ORG.NGO,ORG.COM,ORG.GOV,ORG.MED,PER.GROUP,PER.IND}"+"\n");
		pw.write("@DATA"+"\n");
	}
	
	/**
	 * Run classification on a query file given training data.
	 */
	public static void query(WekaModel wekaModel, String trainFile, String queryFile, String classifierStr, List<Mention> queryMentions, Map<String, String> baseline) {
		try{
			DataSource querySource = new DataSource(queryFile);
			Instances queryData = querySource.getDataSet();
			queryData.setClassIndex(queryData.numAttributes() - 1);

			for (int i = 0; i < queryData.numInstances(); i++) {
				boolean ignore = false;
				
				for(FoundEntity fe : HLOntology.getInstance().getAllEntitiesWithType("ignore")) {
					if(queryMentions.get(i).getTokenizedText().toLowerCase().contains(fe.getName())) {
						queryMentions.get(i).setNERType(NERType.NONE);
						ignore = true;
						break;
					}
				}
				if(ignore) {
					continue;
				}
				if(baseline.containsKey(queryMentions.get(i).getTokenizedText().toUpperCase())) {
					String entityType = baseline.get(queryMentions.get(i).getTokenizedText().toUpperCase());
					queryMentions.get(i).setNERType(NERType.valueOf(entityType.replace(".", "_")));
				} else{
					double predictedClsLabel = wekaModel.getClassifier().classifyInstance(queryData.instance(i));
					String entityType = NERType.getByValue(predictedClsLabel).toString().replace("_", ".");
					queryMentions.get(i).setNERType(NERType.valueOf(entityType.replace(".", "_")));
				}
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Building a WEKA model given a training file
	 * @throws Exception 
	 */
	public static WekaModel buildModel(String datafile, String modelFile) throws Exception {
		//Read the data
		DataSource dataSource = new DataSource(datafile);
		Instances data = dataSource.getDataSet();
		data.setClassIndex(data.numAttributes() - 1);
		//Read the model
		Classifier classifier = (Classifier) SerializationHelper.read(modelFile);
		return new WekaModel(classifier, data);
	}
	
	/**
	 * Gets the actual classifier given its enumeration type
	 */
	public static Classifier getClassifier(String classifierStr) {
		Classifier classifier = null;
		if(classifierStr.equals("NAIVE_BAYES")) {
			classifier = new NaiveBayes();
		} else if(classifierStr.equals("NAIVE_BAYES_MULTINOMINAL")) {
			classifier = new NaiveBayesMultinomial();
		} else if(classifierStr.equals("LOGISTIC")) {
			classifier = new Logistic();
		} else if(classifierStr.equals("SIMPLE_LOGISTIC")) {
			classifier = new SimpleLogistic();
		} else if(classifierStr.equals("LIB_SVM")) {
			classifier = new LibSVM();
			//((LibSVM)classifier).setKernelType(new SelectedTag(LibSVM.KERNELTYPE_POLYNOMIAL, LibSVM.TAGS_KERNELTYPE));
		} else if(classifierStr.equals("SMO")) {
			classifier = new SMO();
		} else if(classifierStr.equals("MULTILAYER_PERCEPTRON")) {
			classifier = new MultilayerPerceptron();
		} else if(classifierStr.equals("IBK")) {
			classifier = new IBk();
		} else if(classifierStr.equals("KSTAR")) {
			classifier = new KStar();
		} else if(classifierStr.equals("LWL")) {
			classifier = new LWL();
		} else if(classifierStr.equals("ONER")) {
			classifier = new OneR();
		} else if(classifierStr.equals("J48")) {
			classifier = new J48();
		} else if(classifierStr.equals("DECISION_STUMP")) {
			classifier = new DecisionStump();
		} else if(classifierStr.equals("RANDOM_FOREST")) {
			classifier = new RandomForest();
		} else if(classifierStr.equals("RANDOM_TREE")) {
			classifier = new RandomTree();
		}
		return classifier;
	}

	/**
	 * Train new model
	 *
	 * @param datafile Training data
	 * @param modelFile File the model will be written to
	 * @return Trained model as a Classifier
	 * @throws Exception 
	 */
	public static WekaModel trainModel(String datafile, String modelFile, int nfolds, long seed) throws Exception {
		//Read the data
		DataSource dataSource = new DataSource(datafile);
		Instances data = dataSource.getDataSet();
		data.setClassIndex(data.numAttributes() - 1);

		// randomize data
		Random rand = new Random(seed);
		Instances randData = new Instances(data);
		randData.randomize(rand);
		if (randData.classAttribute().isNominal() && nfolds > 1)
			randData.stratify(nfolds);

		// classifier
		String[] tmpOptions = { "" };
		String classname = "weka.classifiers.functions.LibSVM";
		Classifier cls = (Classifier) Utils.forName(Classifier.class, classname, tmpOptions);

		// build final classifier on all data
		System.out.println("Training classifier ...");
		cls.buildClassifier(data);
		//Save the model
		weka.core.SerializationHelper.write(modelFile, cls);

		if ( nfolds > 0) {
			System.out.println("Performing " + nfolds + "-fold Cross-validation\n");
		}
		// perform cross-validation
		Evaluation eval = new Evaluation(randData);
		for (int n = 0; n < nfolds; n++) {
			Instances train = randData.trainCV(nfolds, n, rand);
			Instances test = randData.testCV(nfolds, n);

			// build and evaluate classifier
			Classifier clsCopy = AbstractClassifier.makeCopy(cls);
			clsCopy.buildClassifier(train);
			eval.evaluateModel(clsCopy, test);
		}

		System.out.println(eval.toSummaryString("=== " + nfolds + "-fold Cross-validation ===", false));
		System.out.println(eval.toMatrixString());
		return new WekaModel(cls, data);
	}
}
