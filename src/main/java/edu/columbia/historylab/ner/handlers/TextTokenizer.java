package edu.columbia.historylab.ner.handlers;

import java.io.StringReader;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

/*
 * Text tokenizer and normalization/clean-up
 */
public class TextTokenizer {
	
	/**
	 * Runs tokenization and normalization/clean-up
	 */
	public static String execute(String inputText, boolean caseless) {
		try {
			inputText = inputText.replaceAll("[\\r\\”]", "");
			inputText = inputText.replaceAll("\\n\\h*", "\n");
			inputText = inputText.replaceAll("\\h*\\n", "\n");
			inputText = inputText.replaceAll("\\n+", "\n");
			inputText = inputText.replaceAll("^\\n*", "");
			inputText = inputText.replaceAll("\\n*$", "");
			inputText = inputText.replaceAll("\\h+", " ");
			
			String[] lines = inputText.split("\\n");
			String outputText = "";
			for(int i=0; i<lines.length; i++) {
			    PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new StringReader(lines[i]), new CoreLabelTokenFactory(), "");
			    String text = "";
			    StringBuilder sb = new StringBuilder();
			    for (CoreLabel label; ptbt.hasNext();) {
			        label = ptbt.next();
			        String word = label.word();
			        if (word.startsWith("'")) {
			            sb.append(word);
			        } else {
			            if (sb.length() > 0)
			            	text+=sb.toString()+" ";
			            sb = new StringBuilder();
			            sb.append(word);
			        }
			    }
			    if (sb.length() > 0) {
			    	text+=sb.toString()+" ";
			    }
			    text = normalize(text, caseless);
			    outputText+=text.trim()+(i<lines.length-1?"\n":"");
			}
			return outputText;
		} catch(Exception e) {
			e.printStackTrace();
			return inputText;
		}
	}
	
	/**
	 * Normalize and cleaning-up special cases
	 */
	public static String normalize(String text, boolean caseless) {
	    text = text.replace("'", " ' ");
	    text = text.replaceAll("\\s'\\s+(s|S)($|[^a-zA-Z]|\\s)", " '$1$2");
	    text = text.replace("`", "'");
	    text = text.replace("''", "'");
	    text = text.replace("''", "'");
	    text = text.replace("-", " - ");
	    text = text.replace("/", " / ");
	    if (caseless) {
		    text = text.replaceAll("(^|[^A-Z]|\\s)([A-Z])\\s\\.", "$1$2.");
		    text = text.replaceAll("(^|[^A-Z]|\\s)(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|SEPT|OCT|NOV|DEC)\\s\\.", "$1$2.");
		    text = text.replaceAll("(^|[^A-Z]|\\s)(MT|GA|KG|REF|EQUIPMENT|USAG)\\s\\.", "$1$2.");
		    text = text.replaceAll("(^|[^A-Z]|\\s)(\\d+)\\s(\\.|\\,\\:)\\s(\\d+)($|[^A-Z]|\\s)", "$1$2$3$4$5");
		    text = text.replaceAll("(^|[^A-Z]|\\s)(\\d+)\\s(\\.|\\,\\:)\\s(\\d+)($|[^A-Z]|\\s)", "$1$2$3$4$5");
		    text = text.replaceAll("(^|[^A-Z]|\\s)(F)\\s(5E)($|[^A-Z]|\\s)", "$1$2$3$4");
		    text = text.replaceAll("(^|[^A-Z]|\\s)(R\\.|PLUS|M|FY)\\s(\\d+)($|[^A-Z]|\\s)", "$1$2$3$4");
		    text = text.replaceAll("(^|[^A-Z]|\\s)(I\\.)\\s(E\\.)($|[^A-Z]|\\s)", "$1$2$3$4");
		    text = text.replaceAll("(^|[^A-Z]|\\s)(E\\.)\\s(G\\.)($|[^A-Z]|\\s)", "$1$2$3$4");
		    text = text.replaceAll("(^|[^A-Z]|\\s)(T\\.)\\s(B\\.)($|[^A-Z]|\\s)", "$1$2$3$4");
		    text = text.replaceAll("(^|[^A-Z]|\\s)(I\\.)\\s(D\\.)($|[^A-Z]|\\s)", "$1$2$3$4");
		    text = text.replaceAll("(^|[^A-Z]|\\s)(D\\.)\\s(C\\.)($|[^A-Z]|\\s)", "$1$2$3$4");
		    text = text.replaceAll("(^|[^A-Z]|\\s)([A-Z])\\s\\&\\s([A-Z])($|[^A-Z]|\\s)", "$1$2&$3$4");
		    text = text.replace("U ' RE N", "U'REN");
	    } else {
		    text = text.replaceAll("(^|[^a-zA-Z]|\\s)([a-zA-Z])\\s\\.", "$1$2.");
		    text = text.replaceAll("(^|[^a-zA-Z]|\\s)(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)\\s\\.", "$1$2.");
		    text = text.replaceAll("(^|[^a-zA-Z]|\\s)(MT|GA|KG|REF|[eE]quipment|USAG)\\s\\.", "$1$2.");
		    text = text.replaceAll("(^|[^a-zA-Z]|\\s)(\\d+)\\s(\\.|\\,\\:)\\s(\\d+)($|[^a-zA-Z]|\\s)", "$1$2$3$4$5");
		    text = text.replaceAll("(^|[^a-zA-Z]|\\s)(\\d+)\\s(\\.|\\,\\:)\\s(\\d+)($|[^a-zA-Z]|\\s)", "$1$2$3$4$5");
		    text = text.replaceAll("(^|[^a-zA-Z]|\\s)(F)\\s(5E)($|[^a-zA-Z]|\\s)", "$1$2$3$4");
		    text = text.replaceAll("(^|[^a-zA-Z]|\\s)(R\\.|[pP]lus|M|FY)\\s(\\d+)($|[^a-zA-Z]|\\s)", "$1$2$3$4");
		    text = text.replaceAll("(^|[^a-zA-Z]|\\s)(I\\.)\\s(E\\.)($|[^a-zA-Z]|\\s)", "$1$2$3$4");
		    text = text.replaceAll("(^|[^a-zA-Z]|\\s)(E\\.)\\s(G\\.)($|[^a-zA-Z]|\\s)", "$1$2$3$4");
		    text = text.replaceAll("(^|[^a-zA-Z]|\\s)(T\\.)\\s(B\\.)($|[^a-zA-Z]|\\s)", "$1$2$3$4");
		    text = text.replaceAll("(^|[^a-zA-Z]|\\s)(I\\.)\\s(D\\.)($|[^a-zA-Z]|\\s)", "$1$2$3$4");
		    text = text.replaceAll("(^|[^a-zA-Z]|\\s)(D\\.)\\s(C\\.)($|[^a-zA-Z]|\\s)", "$1$2$3$4");
		    text = text.replaceAll("(^|[^a-zA-Z]|\\s)([a-zA-Z])\\s\\&\\s([a-zA-Z])($|[^a-zA-Z]|\\s)", "$1$2&$3$4");
		    text = text.replace("U ' re n", "U'ren");
	    }
	    text = text.replace("U. S.", "USA");
	    text = text.replace("U.S.", "USA");
	    text = text.replace("U. N.", "UN");
	    text = text.replace("U. K.", "UK");
	    text = text.replaceAll("\\-\\s?LRB\\s?\\-", "(");
	    text = text.replaceAll("\\-\\s?RRB\\s?\\-", ")");
	    text = text.replaceAll("\\-\\s?LSB\\s?\\-", "[");
	    text = text.replaceAll("\\-\\s?RSB\\s?\\-", "]");
	    text = text.replace("......", ". . . . . .");
	    text = text.replace(".....", ". . . . .");
	    text = text.replace("....", ". . . .");
	    text = text.replace("...", ". . .");
	    text = text.replace("..", ". .");
	    text = text.replaceAll("^\\s+|\\s+$", "");
	    text = text.replaceAll("\\s+", " ");
	    return text;
	}
}
