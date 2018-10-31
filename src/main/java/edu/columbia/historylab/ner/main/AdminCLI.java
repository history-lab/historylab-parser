package edu.columbia.historylab.ner.main;

import java.io.File;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.PropertyConfigurator;

import edu.columbia.historylab.ner.constants.MentionType;
import edu.columbia.historylab.ner.constants.NERType;
import edu.columbia.historylab.ner.evaluation.IMentionExtractionEvaluator;
import edu.columbia.historylab.ner.evaluation.MentionExtractionEvaluator;
import edu.columbia.historylab.ner.handlers.AnnotationDocumentReader;
import edu.columbia.historylab.ner.handlers.Config;
import edu.columbia.historylab.ner.models.Mention;

public class AdminCLI {
	
	private static void help(Options options) {
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("\nHLParser Admin CLI", options);
		System.out.println("\nNote: defaults where not explicitly stated are defined in config file\n");
		System.exit(0);
	}

	 
	public static void main( String[] args ) {
		PropertyConfigurator.configure("log4j.properties");
		CommandLineParser parser = new DefaultParser();

		Options options = new Options();
		Option parseOption = Option.builder("p").longOpt( "parse" ).desc( "parse specified text" ).hasArg().argName( "text" ).build();
		Option dbUsernameOption = Option.builder("dbu").longOpt( "db-username" ).desc( "db username to use, use with: -pj" ).hasArg().argName( "db username" ).build();		
		Option dbPasswordOption = Option.builder("dbp").longOpt( "db-password" ).desc( "db password to use, use with: -pj" ).hasArg().argName( "db password" ).build();		
		Option offsetOption = Option.builder("o").longOpt( "offset" ).desc( "offset index of start docid in text file to parse, use w/ -pj" ).hasArg().argName( "offset" ).build();		
		Option limitOption = Option.builder("li").longOpt( "limit" ).desc( "total number of docs to parse, use alongside -o w/ -pj" ).hasArg().argName( "limit" ).build();		
		Option confOption = Option.builder("co").longOpt( "config-path" ).desc( "filepath to config, default=conf/hl.config" ).hasArg().argName( "config path" ).build();			
		Option gloveSizeOption = Option.builder("gs").longOpt( "glove-size" ).desc( "glove vector size (number of word embeddings)" ).hasArg().argName( "glove size" ).build();		
		Option numFoldsOption = Option.builder("nf").longOpt( "num-folds" ).desc( "number of folds for cross validation" ).hasArg().argName( "num folds" ).build();		
		Option forcedMentionToNERTypeMappingsOption = Option.builder("fm").longOpt( "force-ner-mappings" ).desc( "forces mention to NER type mappings. Use to force mention types to be used as a NER type. Examples: STANFORDNER_PER>PER_IND, STANFORDNER_LOC>LOC. See -vn and -vm for types to use." ).hasArg().argName( "mappings" ).build();		
		Option fileOption = Option.builder("f").longOpt( "file" ).desc( "filepath to a file, for use w/ -p pointing to an annotated file" ).hasArg().argName( "filepath" ).build();		

		options.addOption("h", "help", false, "prints detailed help");
		options.addOption("cl", "caseless", false, "applies caseless model");
		options.addOption("pj", "parse-job", false, "runs full parse job pulling documents from db, control num docs using -o, -li");
		options.addOption("pt", "prep-test-train", false, "prepares test/train data files from annotated docs, use w/ -rb");
		options.addOption("rb", "run-baseline", false, "runs baseline, use w/ -pt");
		options.addOption("t", "train-model", false, "trains and evaluates a new model using train.arff located in modelPath, use w/ -nf");
		options.addOption("e", "evaluate-tagging", false, "evaluates entity tagging efficacy on test data dir");
		options.addOption("v", "verbose", false, "verbose mode");
		options.addOption("s", "strict", false, "strict mode, enforces type comparison during evaluation");
		options.addOption("i", "ignore-no-type", false, "ignore entities without type assignment");
		options.addOption("vn", "view-ner-types", false, "view ner types, output for use w/ -fm");
		options.addOption("vm", "view-mention-types", false, "view mention types, output for use w/ -fm");

		options.addOption(parseOption);
		options.addOption(offsetOption);
		options.addOption(limitOption);
		options.addOption(confOption);
		options.addOption(gloveSizeOption);
		options.addOption(dbUsernameOption);
		options.addOption(dbPasswordOption);
		options.addOption(numFoldsOption);
		options.addOption(forcedMentionToNERTypeMappingsOption);
		options.addOption(fileOption);

		try {
		    CommandLine cli = parser.parse( options, args );

		    String configPath = cli.getOptionValue("config-path", "conf/hl.config");
		    Config conf = Config.getInstance(configPath);
		    if (cli.hasOption("offset")) {
		    	conf.setOffset(Integer.parseInt(cli.getOptionValue("offset", "0")));
		    }
		    if (cli.hasOption("limit")) {
		    	conf.setLimit(Integer.parseInt(cli.getOptionValue("limit", "10")));
		    }
		    if (cli.hasOption("caseless")) {
		    	conf.setCaseless(cli.hasOption("caseless"));
		    }
		    if (cli.hasOption("glove-size")) {
		    	conf.setGloveSize(Integer.parseInt(cli.getOptionValue("glove-size", "200000")));
		    }
		    if (cli.hasOption("num-folds")) {
		    	conf.setGloveSize(Integer.parseInt(cli.getOptionValue("num-folds", "2")));
		    }
		    if (cli.hasOption("db-username")) {
		    	conf.setDatabaseUsername(cli.getOptionValue("db-username"));
		    }
		    if (cli.hasOption("db-password")) {
		    	conf.setDatabasePassword(cli.getOptionValue("db-password"));
		    }
			if (cli.hasOption("verbose")) {
				conf.setVerbose(true);
			}
			if (cli.hasOption("view-ner-types")) {
				for (NERType nt:NERType.class.getEnumConstants()) {
					System.out.println(nt.toString());
				}
			}
			if (cli.hasOption("view-mention-types")) {
				for (MentionType mt:MentionType.class.getEnumConstants()) {
					System.out.println(mt.toString());
				}
			}
			if (cli.hasOption("force-ner-mappings")) {
				conf.addMentionTypesToNERTypeForceMap(cli.getOptionValue("force-ner-mappings"));
			}
		    
		    // validate that block-size has been set
		    if( cli.hasOption( "h" ) ) {
		        AdminCLI.help(options);
		    } else if (cli.hasOption("parse")) {
		    	List<Mention> mentions =  null;
		    	if (cli.hasOption("file")) {
		    		String text = AnnotationDocumentReader.getTextFromAnnotated(new File(cli.getOptionValue("file")));
			    	System.out.println("Parsing text: " + text);
			    	mentions = HLParser.getInstance(configPath).parse(text);
		    	} else {
			    	System.out.println("Parsing text: " + cli.getOptionValue("parse"));
			    	mentions = HLParser.getInstance(configPath).parse(cli.getOptionValue("parse"));
		    	}
		        Mention.printList(mentions);
		    } else if (cli.hasOption("parse-job")) {
		    	ParseJob.run(configPath);
		    } else if (cli.hasOption("prep-test-train")) {
		    	HLParserBuilder.prepTestTrainData(configPath, cli.hasOption("run-baseline"));
		    } else if (cli.hasOption("train-model")) {
		    	HLParserBuilder.train(configPath);
		    } else if (cli.hasOption("evaluate-tagging")) {
                IMentionExtractionEvaluator evaluator = new MentionExtractionEvaluator();
                evaluator.evaluate(configPath, conf.getTestDataDir(),1000,
						cli.hasOption("strict"),
						cli.hasOption("verbose"),
						cli.hasOption("ignore-no-type"));
			}

		} catch( Exception e ) {
		    System.err.println( e.getMessage() );
		    e.printStackTrace();
		}
	}
}
