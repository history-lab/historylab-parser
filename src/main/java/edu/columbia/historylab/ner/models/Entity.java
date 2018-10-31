package edu.columbia.historylab.ner.models;

import edu.columbia.historylab.ner.constants.NERType;

/*
 *Represents an annotation entity: index + name + NER type.
 */
public class Entity {
	
	private String index;
	private String name;
	private NERType nerType;
	
	public Entity(String index, String name, NERType nerType) {
		this.index = index;
		this.name = name;
		this.nerType = nerType;
	}
	
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public NERType getNERType() {
		return nerType;
	}
	
	public void setNERType(NERType nerType) {
		this.nerType = nerType;
	}
}
