package edu.columbia.historylab.ner.models;

import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * WekaModel representation.
 */
public class WekaModel {
	
	private Classifier classifier;
	private Instances data;
	
	public WekaModel(Classifier classifier, Instances data) {
		this.classifier = classifier;
		this.data = data;
	}
		
	public Classifier getClassifier() {
		return classifier;
	}
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
	
	public Instances getData() {
		return data;
	}
	
	public void setData(Instances data) {
		this.data = data;
	}

}
