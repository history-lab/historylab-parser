package edu.columbia.historylab.ner.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.columbia.historylab.ner.constants.Constants;
import edu.columbia.historylab.ner.constants.NERType;
import edu.columbia.historylab.ner.handlers.Config;
import edu.columbia.historylab.ner.handlers.DatabaseHandler;
import edu.columbia.historylab.ner.models.Mention;

public class ParseJob {

	public static void run(String configPath) throws Exception {
		
		//Read the document IDs
		System.out.println("Reading the document IDs...");
		List<String> documentIds = loadOneColumnList(Config.getInstance(configPath).getDocumentPath());
		
		//Fetch the documents from DB
		System.out.println("Fetching the documents from DB...");
		int offsetPlusLimit = Config.getInstance().getOffset()+Config.getInstance().getLimit();
		Map<String, String> documents = DatabaseHandler.getDocuments(		
				documentIds.subList(
						Config.getInstance().getOffset(), 
						offsetPlusLimit > documentIds.size() ? documentIds.size(): offsetPlusLimit
				)
		);
		
		//If none of the documents has content, terminate
		boolean hasText = false;
		for(Entry<String, String> entry : documents.entrySet()){
			String text = entry.getValue();
			if(!(text == null || text.trim().equals("") || text.contains(Constants.TEXT_ERROR))){
				hasText = true;
				break;
			}
		}
		if(!hasText){
			System.out.println("There is no text to process. Quitting!");
			return;
		}
		
		//Create directories if not existing
		System.out.println("Checking output directories...");
		File outputDirectory = new File(Config.getInstance().getOutputPath());
		if(!outputDirectory.exists()){
			outputDirectory.mkdir();
		}

		//Run NER and Wikification
		System.out.println("Processing...");
		int docNum=0;

		for(Entry<String, String> entry : documents.entrySet()) {
			String documentId = entry.getKey();
			String outFilePath=Config.getInstance().getOutputPath()+"/NER-"+documentId+"-parsed-"+Config.getInstance().getOffset()+"-"+Config.getInstance().getLimit()+"-"+docNum+".txt";
			PrintWriter pw = new PrintWriter(outFilePath);
			String text = entry.getValue();
			System.out.println("documentId="+documentId+", text="+text);
			//Trivial cases
			if(text == null || text.trim().equals("") || text.contains(Constants.TEXT_ERROR)){
				System.out.println("#####XPROCESSED: "+documentId);
				continue;
			}
			//Process the text
			text = text.replace("\n", " ").trim();
			
			List<Mention> mentions = HLParser.getInstance(configPath).parse(text, documentId);

			//Final output
			for(Mention mention : mentions){
				if(!mention.getNERType().equals(NERType.NONE)){
					String output = mention.getFileName()+"\t"+mention.getRegularText()+"\t"+mention.getNERType().toString()+"\t"+mention.getWikiURL();
					pw.write(output+"\n");
				}
			}
			
			System.out.println("#####PROCESSED: "+documentId+", output in: " + outFilePath);
			pw.close();
			docNum+=1;
		}
		System.out.println("Done!");
	}
	
	/**
	 * Reading one-column tabular files into a list
	 */
	private static List<String> loadOneColumnList(String filePath){
		List<String> out = new ArrayList<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0){
					out.add(line);
				}
			}
			br.close();
		} catch(Exception e) {
			throw new RuntimeException("Problems reading one column list file", e);
		}
		return out;
	}

}
