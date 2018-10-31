package edu.columbia.historylab.ner.constants;

/*
 * Constants used throughout the application.
 */
public class Constants {
	
	
	// SQL
	public static final String DB_QUERY_SELECT = "SELECT id, body FROM docs WHERE ";
	public static final String TEXT_ERROR = "ERROR READING TEXT INDEX";
	
	//Parameters
	public static final String PARAMETER_MODEL_PATH = "modelPath";
	public static final String PARAMETER_GLOVE_MODEL_PATH = "gloveModelPath";
	public static final String PARAMETER_RESOURCES_PATH = "resourcesPath";
	public static final String PARAMETER_DOCUMENT_PATH = "documentPath";
	public static final String PARAMETER_OUTPUT_PATH = "outputPath";
	public static final String PARAMETER_DATABASE_URL = "databaseUrl";
	public static final String PARAMETER_DATABASE_NAME = "databaseName";
	public static final String PARAMETER_DATABASE_USERNAME = "databaseUsername";
	public static final String PARAMETER_DATABASE_PASSWORD = "databasePassword";

	public static final String PARAMETER_CLASSIFIER = "classifier";
	public static final String PARAMETER_GLOVE_SIZE = "gloveSize";
	public static final String PARAMETER_WINDOW_SIZE = "windowSize";
	public static final String PARAMETER_NUM_FOLDS = "numFolds";
	public static final String PARAMETER_INCLUDE_MENTION_TYPE = "includeMentionType";
	public static final String PARAMETER_RUN_BASELINE = "runBaseline";
	
	public static final String PARAMETER_CASELESS = "caseless";
	public static final String PARAMETER_OFFSET = "offset";
	public static final String PARAMETER_INCLUDE_NER = "includeNER";
	public static final String PARAMETER_IGNORE_PRONOUNS = "ignorePronouns";
	public static final String PARAMETER_LIMIT = "limit";
	
	public static final String PARAMETER_TRAIN_DATA_DIR = "trainDataDir";
	public static final String PARAMETER_DEV_DATA_DIR = "devDataDir";
	public static final String PARAMETER_TEST_DATA_DIR = "testDataDir";
	public static final String PARAMETER_WEIGHTED_TRAINING = "weightedTraining";

	//Files
	public static final String GLOVE_MODEL = "glove.model";
	public static final String TRAIN_DATA = "train.arff";
	public static final String TEST_DATA = "test.arff";
	public static final String DEV_DATA = "dev.arff";
	public static final String TRAIN_MODEL = "train.model";
	public static final String BASELINE_MODEL = "baseline.model";
	public static final String STANFORD_POS_MODEL = "english-left3words-distsim.tagger";
	public static final String STANFORD_POS_MODEL_CASELESS = "english-caseless-left3words-distsim.tagger";
	public static final String STANFORD_PARSE_MODEL = "englishPCFG.ser.gz";
	public static final String STANFORD_PARSE_MODEL_CASELESS = "englishPCFG.caseless.ser.gz";
	public static final String STANFORD_NER_MODEL = "english.all.3class.distsim.crf.ser.gz";
	public static final String STANFORD_NER_MODEL_CASELESS = "english.all.3class.caseless.distsim.crf.ser.gz";
	
	//Regex
	public static final String REGEX_DETERMINERS = "^(THE|The|the|A|a|AN|An|an)\\_";
	public static final String[] CLOSED_CLASSES = {"THE", "A", "AN", "THAT", "THIS", "THOSE", "THESE", "MISS", "MS", "MS.", "MRS", "MRS.", "DR", "DR.", "SIR"};
	public static final String REGEX_PRONOUNS_0 = "^(I|ME|MINE|MYSELF|YOU|YOUR|YOURS|YOURSELF|HE|HIS|HIM|HIMSELF|SHE|HER|HERS|HERSELF|WE|OUR|OURS|OURSELVES|THEY|THEIR|THEIRS|THEM|THEMSELVES)$";
	public static final String REGEX_PRONOUNS_1 = "^(I|ME|MINE|MYSELF|YOU|YOUR|YOURS|YOURSELF|HE|HIS|HIM|HIMSELF|SHE|HER|HERS|HERSELF)($|\\s.*$)";
	public static final String REGEX_PRONOUNS_2 = "^(WE|OUR|OURS|OURSELVES|THEY|THEIR|THEIRS|THEM|THEIRSELVES)($|\\s.*$)";
	
}
