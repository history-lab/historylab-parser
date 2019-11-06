import java.io.File;
import java.util.stream.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.lang.*;

// TODO: cover this case [[[[GENERAL MILL ||2]] SUPERINTENDENT JACK CLARK ([[BRITISH||1]])  || 10]] 

class DataFormatConverter {

	final static String srcDirPath = "C:\\dev\\historylab-parser\\src\\test\\resources\\gt\\train\\";
	final static String destDirPath = "C:\\dev\\historylab-parser\\CoreNLP_playground\\hl_training\\train_data_set\\";
	private static FileWriter fw_FileList = null;

	public static void main(String[] args) {

		try (Stream<Path> walk = Files.walk(Paths.get(srcDirPath))) {

			fw_FileList = new FileWriter("FileList.txt");

			List<String> result = walk.filter(Files::isRegularFile)
					.map(x -> x.toString()).collect(Collectors.toList());

			convert(result.get(0));

			// result.forEach(System.out::println);
			result.forEach(DataFormatConverter::convert);


			fw_FileList.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void convert(String filePath) {

		try {

			File file = new File(filePath);
			Scanner sc = null;
			sc = new Scanner(file);

			FileWriter fw = null;
			System.out.print(filePath.replace(srcDirPath,"").replace(".txt","") + "-reformated.tsv,");
			fw = new FileWriter(filePath.replace(srcDirPath,"").replace(".txt","") + "-reformated.tsv");
			fw_FileList.write(filePath.replace(srcDirPath,"").replace(".txt","") + "-reformated.tsv,");


			HashMap<String,String> entityMap = new HashMap<String,String>();

			//Scan through the classes at the top of this document
			//TODO: missing the first line for some reason
			while (sc.hasNextLine()) {
				String currentLine = sc.nextLine();
				if (currentLine.length() >= 3 && currentLine.substring(0,3).equals("@@@")) break;
				String[] spaceSplits = currentLine.split(" +");
				if (spaceSplits.length == 0) continue;
				String entityLocalId = spaceSplits[0].replace(".","");
				if (!isNumeric(entityLocalId)) continue;

				entityMap.put(entityLocalId, spaceSplits[spaceSplits.length-1]);
			}

			//Concatenate all other lines into 1 String
			StringBuilder sb = new StringBuilder();
			while (sc.hasNextLine()) {

				String currentLine = sc.nextLine();
				String[] spaceSplits = currentLine.substring(0,currentLine.length()).split(" +");
				if (spaceSplits.length == 0) continue;

				int startIndex = 0;
				if (isNumeric(spaceSplits[0].replace(".",""))) startIndex = spaceSplits[0].length();
				StringBuilder currentSB = new StringBuilder(currentLine.substring(startIndex,currentLine.length())
					.replace(".", " .").replace(",", " ,").replace(";", " ;").replace("?", " ?")
					.replace("!", " !").replace("'s", " 's").replace("-", " -").replace("--", " --"));
				currentSB.append(" ");

				ArrayList<Integer> openBracketPairIndex = new ArrayList<Integer>();

				for (int i=0; i<currentSB.length();i++) {

					if (currentSB.substring(i,i+1).equals("[")) {

						openBracketPairIndex.add(i);
						i++;

					} else if (currentSB.substring(i,i+1).equals("]")) {
						Integer latestOpenBracketPairIndex = null;
						if (openBracketPairIndex.size() != 0) {
							latestOpenBracketPairIndex = openBracketPairIndex.get(openBracketPairIndex.size()-1);
							openBracketPairIndex.remove(openBracketPairIndex.size()-1);
						} else break;

						String stringBetweenBrackets = currentSB.substring(latestOpenBracketPairIndex+2,i);

						// System.out.println("stringBetweenBrackets: " + stringBetweenBrackets);

						int indexOfPipe = stringBetweenBrackets.indexOf("||");
						if (indexOfPipe==-1) break;
						String[] doublePipeSplits = new String[2];
						doublePipeSplits[0] = stringBetweenBrackets.substring(0,indexOfPipe);
						doublePipeSplits[1] = stringBetweenBrackets.substring(indexOfPipe+2);
						String[] localSpaceSplits = doublePipeSplits[0].split(" +");
						StringBuilder replaceSB = new StringBuilder();

						if (openBracketPairIndex.size() == 0) {

							String currentClass = entityMap.get(doublePipeSplits[1].trim());
							// System.out.println("doublePipeSplits[0]: " + doublePipeSplits[0]);
							// System.out.println("doublePipeSplits[1]: " + doublePipeSplits[1]);
							// System.out.println("currentClass: " + currentClass);

							for (int j=0; j<localSpaceSplits.length; j++) {

								replaceSB.append(localSpaceSplits[j] + "	" + currentClass + "\n");

							}

							currentSB.replace(latestOpenBracketPairIndex,i+2,replaceSB.toString());

							i=latestOpenBracketPairIndex + replaceSB.length() - 1;

						} else {

							for (int k=0; k<localSpaceSplits.length; k++) {

								replaceSB.append(localSpaceSplits[k]);
								if (k < localSpaceSplits.length - 1) replaceSB.append(" ");

							}

							currentSB.replace(latestOpenBracketPairIndex,i+2,replaceSB.toString());

							i=latestOpenBracketPairIndex-1;

						}


					} else if (currentSB.substring(i,i+1).equals(" ") && i!=0) {

						if (openBracketPairIndex.size() == 0
							&& i>0 && !currentSB.substring(i-1,i).equals("\n")) {
							currentSB.replace(i,i+1,"	O\n");
						}

					}
				}

				// System.out.println(currentLine);
				// System.out.println(currentSB.toString());
				sb.append(currentSB.toString().replace(" ","").replace("  ","").replace("   ","")
												.replace(" \n","").replace("	\n",""));

			}


			// System.out.println(sb.toString().trim());

			fw.write(sb.toString().trim());


			// for (int i=0; i<spaceSplits.length; i++) {
			// 	fw.write(spaceSplits[i] + "	" + currentClass + "\n");
			// }


			System.out.print("EntityMap: " + entityMap + "\n\n");


			fw.close();


		} catch (IOException e) {

			System.out.print("FAILURE: " + filePath.replace(srcDirPath,"").replace(".txt","") + "-reformated.tsv,");

		}


	}

	private static boolean isNumeric(String strNum) {
	    try {
	        double d = Double.parseDouble(strNum);
	    } catch (NumberFormatException | NullPointerException nfe) {
	        return false;
	    }
    	return true;
	}



}