package vocab;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;


// The program takes all words from file "inputwords" and creates a file "outputdictionary" with synonyms. The order is not preserved between the files because a HashMap and Set datastructures
// are used to do the computations... Input file has format: one word per line. Output file has format [list of synonyms] per line for a given word. 
// An extra method can be added to take the output file and convert it to a desired format...
public class AugmentVocabulary {

	public static void main(String args[]) {

		String line = null;

		try {
			FileReader fr = new FileReader("inputwords"); // place the words in this file (one word per line) for which you want to find synonyms (or other related words)
			BufferedReader bufferedReader = new BufferedReader(fr);

			while ((line = bufferedReader.readLine()) != null) {
				MyWordNet w = new MyWordNet();
				w.searchWord(line);
			}

			bufferedReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class MyWordNet {
	public IDictionary dictionary = null;
	public static WordnetStemmer stemmer = null;
	MyWordNet() {
		try {
			String path = "dict";
			URL url = new URL("file", null, path);

			dictionary = new Dictionary(url);
			dictionary.open();
			stemmer = new WordnetStemmer(dictionary);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void searchWord(String inkey) {
		
		HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>(); // create a "key->value" map where the "value" is a growing list of synonyms of the "key" word
		for (POS p : POS.values()) { // for all possible parts of speech
			List<String>keyStems = stemmer.findStems(inkey, p); // for all possible stems of the word given the part of speech / POS
				for(String key : keyStems) {
					IIndexWord idxWord = dictionary.getIndexWord(key, p); // find that particular stem of "key" in the dictionary with the POS = p
					if (idxWord != null) { // this avoids the exception if word is not found
						for (IWordID wordID : idxWord.getWordIDs()) {
		
							IWord word = dictionary.getWord(wordID);
							ISynset wordSynset = word.getSynset(); // get synonyms
							for (IWord synonym : wordSynset.getWords()) {
								if (hashMap.containsKey(key)) { // if "key" is already in the map we want to grow the value list and not overwrite it...
									hashMap.get(key).add(synonym.getLemma());
								} else { // if "key" is not in the map than create the "value" list and make the "key" point to it
									List<String> list = new ArrayList<String>();
									hashMap.put(key, list);
								}
		
							}
						}
				}
			}
		}

		try {
			// delete the output file contents each time you run the application
			FileWriter fw = new FileWriter("outputdictionary", true); // "true" means append mode is enabled
			BufferedWriter bw = new BufferedWriter(fw);

			for (String mapKey : hashMap.keySet()) {
				HashSet<String> set = new HashSet<String>(hashMap.get(mapKey)); // use Set data structure to get rid of duplicates
				String line = set.toString();
				bw.write(line);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
