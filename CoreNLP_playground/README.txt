

FAQ: https://nlp.stanford.edu/software/crf-faq.shtml

1/ Get javanlp-core.jar
	Download prebuit javanlp-core.jar or  Stanford CoreNLP: https://stanfordnlp.github.io/CoreNLP/#download
	Or
	Clone/Fork https://github.com/stanfordnlp/CoreNLP and follow instructions in "Build with Maven" section

2/ Change directory to \historylab-parser\CoreNLP_playground\hl_training\train_data_set

3/ Check hl.prop for configuration

4/ Train and serialize a model:
	java -cp <PathTo_javanlp-core.jar> edu.stanford.nlp.ie.crf.CRFClassifier -prop <PathToPropertyFile>
	E.g. java -Xmx12288M -cp C:\dev\CoreNLP\javanlp-core.jar edu.stanford.nlp.ie.crf.CRFClassifier -prop hl.prop

5/ Test the serialized model:
	java -cp <PathTo_javanlp-core.jar> edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier <PathToSerializedModel> -testFile <PathToTestFile>
	E.g. java -cp C:\dev\CoreNLP\javanlp-core.jar edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier hl.ser.gz -testFile 1979USUNN04958-reformated.tsv