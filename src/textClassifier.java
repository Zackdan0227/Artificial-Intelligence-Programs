import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

public class textClassifier {

	private static HashSet<String> stopWords;
	private static ArrayList<Category> catArray = new ArrayList<Category>();
	private static ArrayList<Word> wordArray = new ArrayList<Word>();
	private static int totalBio = 0;
	private static double epsilon = 0.1;
	private static int numCat = 0;

	// prediction 1 for right, 0 for wrong, -1 for unassigned
	private static class People {
		String name;
		String actualCat;
		String predictedCat;
		int prediction;
		HashMap<String, Double> catTotal;
		HashMap<String, Double> catProb;

		People(String n) {
			this.name = n;
			this.actualCat = "";
			this.catTotal = new HashMap<String, Double>();
			this.catProb = new HashMap<String, Double>();
			this.prediction = -1;
		}

		People() {
			this.name = "";
			this.actualCat = "";
			this.catTotal = new HashMap<String, Double>();
			this.catProb = new HashMap<String, Double>();
			this.prediction = -1;

		}

		boolean result(String actualCat, String predictedCat) {
			if (actualCat.equals(predictedCat)) {
				return true;
			} else {
				return false;
			}
		}

	}

	private static class Category {
		String categoryName;
		int numBio;
		boolean inBio;

		Category() {
			this.numBio = 0;
			this.categoryName = "";
		}

		Category(String name) {
			this.numBio = 0;
			this.categoryName = name;
		}
	}

	private static class Word {
		String word;
		ArrayList<Category> wordCatArray;
		HashMap<String, Integer> catBioCount;

		Word(String w) {
			word = w;
			wordCatArray = new ArrayList<Category>();
			catBioCount = new HashMap<String, Integer>();
		}

	}

	static boolean containWord(String wd) {
		if (wordArray.isEmpty()) {
			return false;
		}
		for (int i = 0; i < wordArray.size(); i++) {
			if (wordArray.get(i).word.equals(wd)) {
				return true;
			}
		}
		return false;

	}

	static Word retrieveWord(String wd) {
		for (int i = 0; i < wordArray.size(); i++) {
			if (wordArray.get(i).word.equals(wd)) {
				return wordArray.get(i);
			}
		}
		return null;
	}

	static double FreqCatBio(Category C) {

		return ((double) C.numBio) / ((double) totalBio);
	}

	static double FreqWordCat(Category C, Word W) {
		String catName = C.categoryName;
		double wordCatCount = 0;
		if (W.catBioCount.containsKey(catName)) {
			wordCatCount = (double) W.catBioCount.get(catName);
		}

		return wordCatCount / (double) C.numBio;
	}

	static double PCategory(Category C) {
		return (FreqCatBio(C) + epsilon) / (1 + numCat * epsilon);
	}

	static double PWordGivenCat(Category C, Word W) {
		return (FreqWordCat(C, W) + epsilon) / (1 + 2 * epsilon);
	}

	static double L(double x) {
		return -(Math.log(x) / Math.log(2));
	}
	// for each category, number of biographies of catoegory C (OccT(C))

	// for each category and word W, the number of biographies of category C that
	// contain W, (OccT(W|C)
	public static void main(String[] args) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		String corpusPath = args[0];
		int N = Integer.valueOf(args[1]);
		totalBio = N;
		// parse in stop words
		String stopWordsPath = args[2];
		Scanner readerStopWords = new Scanner(new FileInputStream(stopWordsPath));
		stopWords = new HashSet<String>();
		while (readerStopWords.hasNext()) {
			String word = readerStopWords.next();
			stopWords.add(word);
		}
		readerStopWords.close();
//		System.out.println(stopWords.toString());
		// check if the new cat is already in array, we dont want duplicate
		ArrayList<String> tempCatRec = new ArrayList<String>();
		ArrayList<People> pplArray = new ArrayList<People>();
		Scanner corpusReader = new Scanner(new FileInputStream(corpusPath));
		int ct = 0;
		int count = 0;
		// reading trainning data
		while (corpusReader.hasNext()) {
			if (count > N) {
				break;
			}
			String catName = "";
			// people and category
			switch (ct) {
			case 0:
				if (count == N) {
					count++;
					break;
				}
				String peopleName = corpusReader.nextLine();
				if (peopleName.length() == 0) {
					continue;
				}
				count++;
				break;
			case 1:
				catName = corpusReader.next();
				Category cat = new Category(catName);
				String line = null;
				corpusReader.nextLine();
				while (!(line = corpusReader.nextLine()).isEmpty()) {
					while (line.length() > 0) {
						int blank = line.indexOf(" ");
						String wdCap = null;
						if (blank < 0) {
							wdCap = line;
							line = "";
						} else {
							wdCap = line.substring(0, blank);
						}

						String wd = wdCap.toLowerCase();
						if (wd.indexOf("\\p{Punct}") != 0 || wd.indexOf("\\p{Punct}") != wd.length()) {
							wd = wd.replaceAll("\\p{Punct}", "");
						}
						// skip blank and 2 word
						if (wd == null || wd.trim().length() == 0) {
							if (!line.isEmpty()) {
								line = line.substring(blank + 1);
							}
							continue;
						}
						if (wd.length() <= 2) {
							if (!line.isEmpty()) {
								line = line.substring(blank + 1);
							}
							continue;
						}
						if (!stopWords.contains(wd)) {
							Word W = new Word("");
							if (containWord(wd)) {
								W = retrieveWord(wd);
							} else {
								W = new Word(wd);
							}

							// add category to the Word cat array if empty
							if (W.wordCatArray.isEmpty()) {
								W.wordCatArray.add(cat);

							}
							if ((W.catBioCount.putIfAbsent(cat.categoryName, 1)) != null) {
								W.catBioCount.put(cat.categoryName, W.catBioCount.get(cat.categoryName) + 1);
							}
							if (cat.categoryName.equals(wd)) {
								cat.inBio = true;
							}
							if (wordArray.isEmpty()) {
								wordArray.add(W);
							}
							boolean contain = containWord(W.word);
							if (!contain) {
								wordArray.add(W);
							}
						}
						line = line.substring(blank + 1);

					}
				}

				if (tempCatRec.isEmpty() || !tempCatRec.contains(catName)) {
					tempCatRec.add(catName);
					catArray.add(cat);
				}
				if (tempCatRec.contains(catName)) {
					for (int i = 0; i < catArray.size(); i++) {
						if (catArray.get(i).categoryName.equals(catName)) {
							catArray.get(i).numBio++;
						}

					}
				}
				break;

			}
			if (ct <= 1) {
				ct++;
			} else if (ct == 2) {
				ct = 0;
			}
		}
		numCat = tempCatRec.size();

		// probability
		HashMap<String, Double> LFreqC = new HashMap<String, Double>();
		for (int i = 0; i < catArray.size(); i++) {
			String name = catArray.get(i).categoryName;
			double PC = PCategory(catArray.get(i));
			PC = L(PC);
			LFreqC.put(name, PC);
		}

		HashMap<String, HashMap<String, Double>> LFreqWordC = new HashMap<String, HashMap<String, Double>>();
		for (int i = 0; i < wordArray.size(); i++) {
			String word = wordArray.get(i).word;
			Word W = wordArray.get(i);
			HashMap<String, Double> temp = new HashMap<String, Double>();
			for (int j = 0; j < catArray.size(); j++) {
				Category cat = catArray.get(j);
				double value = PWordGivenCat(cat, W);
				value = L(value);
				temp.put(cat.categoryName, value);
				LFreqWordC.put(word, temp);
			}
		}

		ct = 0;
		// applying the classifier to the data
		while (corpusReader.hasNext()) {
			ArrayList<Word> wordNeedProcess = new ArrayList<Word>();

			double total = 0;
			People ppl = new People();
			String catName = "";
			// people and category

			String peopleName = corpusReader.nextLine();
			if (peopleName.length() == 0) {
				continue;
			}
			catName = corpusReader.next();
			String line = null;
			ppl.name = peopleName;
			ppl.actualCat = catName;

			corpusReader.nextLine();
			while (corpusReader.hasNext() && !(line = corpusReader.nextLine()).isEmpty()) {
				while (line.length() > 0) {
					int blank = line.indexOf(" ");
					String wdCap = null;
					if (blank < 0) {
						wdCap = line;
						line = "";
					} else {
						wdCap = line.substring(0, blank);
					}

					String wd = wdCap.toLowerCase();
					if (wd.indexOf("\\p{Punct}") != 0 || wd.indexOf("\\p{Punct}") != wd.length()) {
						wd = wd.replaceAll("\\p{Punct}", "");
					}
					// skip blank and 2 word
					if (wd == null || wd.trim().length() == 0) {
						if (!line.isEmpty()) {
							line = line.substring(blank + 1);
						}
						continue;
					}
					if (wd.length() <= 2) {
						if (!line.isEmpty()) {
							line = line.substring(blank + 1);
						}
						continue;
					}
					if (!stopWords.contains(wd)) {
						Word W = new Word("");
						if (containWord(wd)) {
							W = retrieveWord(wd);
						} else {
							line = line.substring(blank + 1);
							continue;
						}
						wordNeedProcess.add(W);
					}
					line = line.substring(blank + 1);

				}
			}
			double min = 10000;
			for (int j = 0; j < catArray.size(); j++) {
				String cat = catArray.get(j).categoryName;
				double totalCat = 0;
				double curCatL = LFreqC.get(cat);
				totalCat += curCatL;
				for (int i = 0; i < wordNeedProcess.size(); i++) {
					String currentW = wordNeedProcess.get(i).word;
					HashMap<String, Double> curWordLProb = LFreqWordC.get(currentW);
					double curWordProbByCat = curWordLProb.get(cat);
					totalCat += curWordProbByCat;
				}
				if (min > totalCat) {
					min = totalCat;
				}
				ppl.catTotal.put(cat, totalCat);
			}
			// check if c-m<7
			HashMap<String, Double> pplProbTemp = new HashMap<String, Double>();
			double sumOfProb = 0;
			for (Map.Entry<String, Double> set : ppl.catTotal.entrySet()) {
				String key = set.getKey();
				double value = set.getValue();
				double x = 0;

				if ((value - min) < 7) {
					x = Math.pow(2, (min - value));
				}
				sumOfProb += x;
				pplProbTemp.put(key, x);
			}
			double biggest = 0;
			String prediction = "";
			for (Map.Entry<String, Double> set : pplProbTemp.entrySet()) {
				String key = set.getKey();
				double value = set.getValue();
				double prob = value / sumOfProb;
				if (prob > biggest) {
					prediction = key;
					biggest = prob;
				}
				ppl.catProb.put(key, prob);
			}
			ppl.predictedCat = prediction;
			if (ppl.predictedCat.equals(ppl.actualCat)) {
				ppl.prediction = 1;
			} else {
				ppl.prediction = 0;
			}
			pplArray.add(ppl);
		}
		corpusReader.close();
		int right = 0;
		BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));

		for (int i = 0; i < pplArray.size(); i++) {
			People p = pplArray.get(i);
			String content = p.name + "." + " " + "Prediction: " + p.predictedCat + ". ";
			writer.write(content);
			if (p.prediction == 1) {
				right++;
				writer.write("Right.");
				writer.newLine();
			} else if (p.prediction == 0) {
				writer.write("Wrong.");
				writer.newLine();
			}
			for (Map.Entry<String, Double> set : p.catProb.entrySet()) {
				String cat = set.getKey();
				double prob = set.getValue();
				double roundOff = (double) Math.round(prob * 100) / 100;
				content = cat + ":" + String.valueOf(roundOff) + "   ";
//				System.out.printf("%s:%.2f    ", cat, prob);
				writer.write(content);
			}
			writer.newLine();
			writer.newLine();
		}
		double y = (double) right / (double) pplArray.size();
		String yS = String.valueOf(y);

		String acc = "Overall accuracy: " + String.valueOf(right) + " out of " + String.valueOf(pplArray.size()) + "= "
				+ yS + ".";
		writer.write(acc);
		writer.close();
	}

}
