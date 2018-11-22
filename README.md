# historylab-parser

History Lab Text NER Parser

## Dependencies

1. git-lfs, see: https://git-lfs.github.com/ and install. Used to version large model files.

Note: remember to use

```
git lfs pull
```

in addition to your usual git sync approach like

```
git pull
```

to make sure you pull down any new actual model files. If you don't git-lfs will leave just the marker files, causing test problems.

2. Make sure you have a glove models directory containing this file:

```
glove.model
```
Note: glove.model can be obtained here: https://nlp.stanford.edu/projects/glove/

You will want the 300d vectors trained on Wikipedia depicted something like this: Wikipedia 2014 + Gigaword 5 (6B tokens, 400K vocab, uncased, 300d vectors, 822 MB download): glove.6B.zip.

Note: you will need to point the conf/hl.config param: gloveModelPath to this directory.

## Build

Create a proper configuration file `conf/hl.config` based on the provided template file.
Then build and launch tests via

```
mvn clean install
```

or less thorough, you can just compile but we recommend the former:

```
mvn clean compile package
```

Note: presumes you have maven installed and will pull down all jar and model dependencies.
Note: You will likely need to bump your Maven memory settings, something like:

```
export MAVEN_OPTS="-Xms4G -Xmx15G"
```

## Help

```
./hlparser -h
```

## Run

Execute the Run class via the CLI called hlparser.

1. You will need to define an hl.config file. You can use the hl.config-template as a guide. See your admin regarding proper settings for your hl.config file. The following examples presume this file is in conf/hl.config though there is a CLI option to point to the file.

2. Parse a sentence like:

```
./hlparser -p "Iran is a country. So is India."
```

4. Launch a parsing job using the DB via

```
./hlparser -pj
```

Note: you will need to define hl.config appropriately.

### Generating train/dev/test data files

In order to generate the training, dev (or validation) and test data files given annotated documents, run the following

```
./hlparser -pt
```

This will produce `train.arff`, `dev.arff` and `test.arff` datafiles in your configured output directory that can be used as input to the model training in the next step.

### Training a model given a prepared training data file

In order to train and evaluate a model use:

```
./hlparser -t
```

Note: this will look inside your modelPath for the latest train.arff file, and use it to build a new train.model file. If the new model file is trusted to outperform prior models, you can check it in.

### Evaluating entity extraction performance

To evaluate performance of entity extraction without type validation, run
```
./hlparser -e
```
To enable strict type validation during the evaluation , enable strict mode ( -s )
```
./hlparser -e -s
```
To ignore candidate mentions without type assignment, use the ( -i ) flag
```
./hlparser -e -s -i
```
To enable verbose output, add the verbose flag ( -v )
```
./hlparser -e -s -i -v
```

### Force mention to NER type mappings

The system by default uses only the results of its NERType classification for final NERType assignments. You may, however, wish to override this to increase recall. See the --force-ner-mappings for details. This will allow you, for example, to map all mentions recognized by an underlying mention extractor like the Stanford NER system like STANFORDNER_PER to the NERType PER_Ind.

### Adding Ontology entries

All ontology, or knowledgebase files are located in src/main/resources/ontology/mayu. You can add individual entries into your own .onto file. For example, you may wish to add specific org or people names and/or synonyms.

Note: All ontology entities must be mapped to an NERType in the edu.columbia.historylab.ner.constants.MentionType transformOntologyTypes method in order to be visible in final Mention output from the HLParser. If you add an entity with one of the supported taxonomical paths, e.g. entity/occupation, entity/person/title, etc this is taken care of for you. If, however, you introduce a new taxonomical path, you will need to modify the MentionType class.

Example:

Here is an example highlighting the underlying ontology.

```
./hlparser -p "Seattle is a nice city. Kansas is a nice state." -fm "STATE>LOC"
```

Note: in this case we add the -fm flag to force all states to be mapped to a NERType location. From the output, we can see that Seattle was recognized as a NERType GPE_SPECIAL by the underlying classifier, but KANSAS is not (visible if you remove the -fm flag). You can play around w/ various settings to understand what yields the optimal efficacy for your document subset.

### Notes

- To launch the weka GUI use
```
mvn exec:java -Dexec.mainClass="weka.gui.GUIChooser"
```

- To run individual tests use
```
mvn -Dtest=TestHLParser test
```
