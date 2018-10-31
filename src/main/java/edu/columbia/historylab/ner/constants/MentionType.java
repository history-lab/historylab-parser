package edu.columbia.historylab.ner.constants;

/*
 * Mention types read from the lookups and by Stanford coreNLP
 */
public enum MentionType {
	
	NONE(0),
	COUNTRY(1),
	CITY(2),
	STATE(3),
	REGION(4),
	NATIONALITY(5),
	ORGANIZATION(6),
	PERSON(7),
	OCCUPATION(8),
	TITLE(9),
	STANFORDNER_ORG(10),
	STANFORDNER_LOC(11),
	STANFORDNER_PER(12);
	
	private int value;
	
	MentionType(int value){
		this.value = value;
	}
	
	public static MentionType create(String s) {
		for (MentionType mt:MentionType.class.getEnumConstants()) {
			if(s.equalsIgnoreCase(mt.toString())) {
				return mt;
			}
		}
		throw new IllegalArgumentException("no MentionType corresponding to " + s + " exists.");
	}
	
	/**
	 * Returns value.
	 */
	public int getValue(){
		return value;
	}
	
	/**
	 * Returns enum member by value
	 */
	public static MentionType getByValue(int value){
	    for(MentionType type : values()){
	        if(type.getValue() == value){
	            return type;
	        }
	    }
	    return MentionType.NONE;
	}

	public static MentionType transformOntologyTypes(String ontologyType) {
		if (ontologyType.equals("entity/georegion/city/name")) {
			return MentionType.CITY;
		} else if (ontologyType.equals("entity/georegion/country/name")) {
			return MentionType.COUNTRY;
		} else if (ontologyType.equals("entity/georegion/province/name")) {
			return MentionType.STATE;
		} else if (ontologyType.equals("entity/georegion/name")) {
			return MentionType.REGION;
		} else if (ontologyType.equals("entity/georegion/country/nationality/name")) {
			return MentionType.NATIONALITY;
		} else if (ontologyType.equals("entity/organization/name")) {
			return MentionType.ORGANIZATION;
		} else if (ontologyType.equals("entity/person/name")) {
			return MentionType.PERSON;
		} else if (ontologyType.equals("entity/occupation")) {
			return MentionType.OCCUPATION;
		} else if (ontologyType.equals("entity/person/title")) {
			return MentionType.TITLE;
		}
		return MentionType.NONE;
	}
}
