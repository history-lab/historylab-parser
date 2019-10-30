import java.io.File;
import java.util.Scanner;
import java.io.*;

class DataFormatConverter {

	public static void main(String[] args) {

		try {

			File file = new File("C:\\dev\\historylab-parser\\CoreNLP_playground\\hl_training\\baseline.txt");
			Scanner sc = null;
			sc = new Scanner(file);

			FileWriter fw = null;
			fw = new FileWriter("ReformatedData.tsv");

			while (sc.hasNextLine()) {

				String currentLine = sc.nextLine();
				String[] spaceSplits = currentLine.split(" ");
				String[] tabSplits = spaceSplits[spaceSplits.length-1].split("	");
				spaceSplits[spaceSplits.length-1] = tabSplits[0];
				String currentClass = tabSplits[1];

				for (int i=0; i<spaceSplits.length; i++) {
					fw.write(spaceSplits[i] + "	" + currentClass + "\n");
				}


			}
			fw.close();


		} catch (IOException e) {

		}




  }


}