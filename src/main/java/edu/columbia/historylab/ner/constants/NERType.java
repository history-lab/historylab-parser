package edu.columbia.historylab.ner.constants;

/*
 * Represents NER entity types in the system
 */
public enum NERType {
	
	NONE(0.0),
	LOC(1.0),
	GPE_NATION(2.0),
	GPE_SPECIAL(3.0),
	ORG_NGO(4.0),
	ORG_COM(5.0),
	ORG_GOV(6.0),
	ORG_MED(7.0),
	PER_GROUP(8.0),
	PER_IND(9.0);
		
	private double value;
	
	NERType(double value){
		this.value = value;
	}
	
	public static NERType create(String s) {
		for (NERType nt:NERType.class.getEnumConstants()) {
			if(s.equals(nt.toString())) {
				return nt;
			}
		}
		throw new IllegalArgumentException("no NERType corresponding to " + s + " exists.");
	}
	
	/**
	 * Returns value.
	 */
	public double getValue(){
		return value;
	}
	
	/**
	 * Returns enum member by value
	 */
	public static NERType getByValue(double value){
	    for(NERType type : values()){
	        if(type.getValue() == value){
	            return type;
	        }
	    }
	    return NERType.NONE;
	}

}
