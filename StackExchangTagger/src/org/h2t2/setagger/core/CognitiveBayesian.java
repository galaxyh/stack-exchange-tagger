package org.h2t2.setagger.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.h2t2.setagger.util.DocumentVectorProcessor;
import org.h2t2.setagger.util.RankPriorityQueue;
import org.h2t2.setagger.util.TagRank;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;


public class CognitiveBayesian implements Model {
	private ArrayList <HashMap <String, Association>> termMapping;
	private HashMap <String, Integer> tagToDocumentFrequency;
	private int numberOfDocuments = 0;
	private HashSet<String> allTagsSet;
	private final Charset UTF8 = Charset.forName("UTF-8");

	private class Association {
		public int tfInDoc = 0;
		public HashMap <String, Integer> tagToCooccurrence;
		public double attentionWeight = 0.0;
		public Double entropy = null;
		public double scaledEntropy = 0.0;
		public Association() {
			tagToCooccurrence = new HashMap <String, Integer>();
		}

		public double getProbabilityOfTagOverTerm(String tag) {
			if (tagToCooccurrence.get(tag) == null) {
				return 0.0;
			}
			return (double) tagToCooccurrence.get(tag) / tfInDoc;
		}

		public double getEntropy() {
			if (this.entropy == null) {
				this.entropy = 0.0;
				for (String tag : tagToCooccurrence.keySet()) {
					double probabilityOfTagOverTerm = getProbabilityOfTagOverTerm(tag);
					this.entropy -= probabilityOfTagOverTerm * Math.log(probabilityOfTagOverTerm);
				}
			}
			return this.entropy;
		}

		public void setScaledEntropy (double se) {
			scaledEntropy = se;
		}
		
		public double getScaledEntropy () {
			return scaledEntropy;
		}

		public void setAttentionWeight (double aw) {
			attentionWeight = aw;
		}

		public double getAttentionWeight () {
			return attentionWeight;
		}
	}

	private double getBaseLevel (String tag) {
		Integer tagFrequency = this.tagToDocumentFrequency.get(tag);
		if (tagFrequency != null && tagFrequency != 0) {
			double probabilityOfTag = tagFrequency / (double) numberOfDocuments;
			return Math.log(probabilityOfTag / (1 - probabilityOfTag));
		} else {
			return 0.0;
		}
	}

	@Override
	// args[1] : titleIdf, args[2] : bodyIdf , args[3] : codeIdf
	public void train (String trainFileName, String[] args) {
		try {
			termMapping = new ArrayList <HashMap <String, Association>>(3);
			for (int i = 1; i <= 3; i ++) {
				termMapping.add(getAssociationMap(args[i]));
			}

			tagToDocumentFrequency = new HashMap <String, Integer>();
			allTagsSet = new HashSet<String>();

			// Now all disqualified terms are eliminated
			CsvReader reader = new CsvReader(new FileInputStream(new File(trainFileName)), UTF8);
			Association termToAssociation;
			String [] tags;
			Integer termAndTagCooccurrence;
			while (reader.readRecord()) {
				if (reader.getColumnCount() != 5)
					continue;
				numberOfDocuments ++;
				tags = reader.get(4).split("\\s+");

				for (String tag : tags) {
					Integer frequency = null;
					if ((frequency = tagToDocumentFrequency.get(tag)) == null) {
						tagToDocumentFrequency.put(tag, 1);
					} else {
						tagToDocumentFrequency.put(tag, frequency + 1);
					}
					allTagsSet.add(tag);
				}

				for (int i = 1; i <= 3; i ++) {
					for (String uniqueTerm : getUniqueTermSet(reader.get(i))) {
						if ((termToAssociation = termMapping.get(i - 1).get(uniqueTerm)) != null) {
							// the term is qualified because its idf < idfBound
							termToAssociation.tfInDoc ++;
							for (String tag : tags) {
								if ((termAndTagCooccurrence = termToAssociation.tagToCooccurrence.get(tag)) != null) {
									termToAssociation.tagToCooccurrence.put(tag, termAndTagCooccurrence + 1);								
								} else {
									termToAssociation.tagToCooccurrence.put(tag, 1);									
								}
							}
						}
					}
				}
			}
			reader.close();

			// Now start to calculate Entropy, Scaled Entropy and Attention Weight
			for (int i = 0; i < termMapping.size(); i ++) {

				double entropyMax = 0.0;
				double entropyTemp = 0.0;
				for (String term : termMapping.get(i).keySet()) {
					if ((entropyTemp = termMapping.get(i).get(term).getEntropy()) > entropyMax) {
						entropyMax = entropyTemp;
					}
				}

				// calculate scaled entropy
				double totalScaledEntropy = 0.0;
				for (String term : termMapping.get(i).keySet()) {
					Association association = termMapping.get(i).get(term);
					association.setScaledEntropy(1.0 - association.getEntropy() / entropyMax);
					totalScaledEntropy += association.getScaledEntropy();
				}

				double attentionWeightBound = 1.0;

				// calculate attention weight
				for (String term : termMapping.get(i).keySet()) {
					Association association = termMapping.get(i).get(term);
					association.setAttentionWeight(attentionWeightBound * association.getScaledEntropy() / totalScaledEntropy);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private HashSet <String> getUniqueTermSet (String content) {
		HashSet <String> set = new HashSet <String>();
		for (String term : content.split("\\s+")) {
			set.add(term);
		}
		return set;
	}

	private HashMap <String, Association> getAssociationMap (String idfFileName) throws Exception {
		HashMap <String, Association> termToAssociation = new HashMap <String, Association>(); 
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(idfFileName)), "UTF8"));
		reader.readLine();// read header
		String line = null;
		while ((line = reader.readLine()) != null ) {
			String [] term_idf = line.trim().split("\\s+");
			if (Double.parseDouble(term_idf[1]) < DocumentVectorProcessor.idfUpperBound) {
				termToAssociation.put(term_idf[0], new Association());
			}
		}
		reader.close();
		return termToAssociation;
	}

	@Override
	public void predict (String predictFileName, String outputFileName, String[] args) {
		try {
			CsvReader reader = new CsvReader(new FileInputStream(new File(predictFileName)), UTF8);
			CsvWriter writer = new CsvWriter(outputFileName, ',', UTF8);
			
			// header
			writer.write("\"Id\"");
			writer.write("\"Tags\"");
			writer.endRecord();

			while (reader.readRecord()) {
				if (reader.getColumnCount() != 4) {
					System.err.println("error test record should contain 4 columns!");
					System.exit(-1);
				}
				//RankPriorityQueue priQueue = new RankPriorityQueue(10);
				TagRank [] queue = new TagRank[allTagsSet.size()];
				Object [] termSets = {getUniqueTermSet(reader.get(1)), getUniqueTermSet(reader.get(2)), getUniqueTermSet(reader.get(3))};
				Double [] weights = {1.0, 1.0, 3.0};

				int index = 0;

				for (String tag : allTagsSet) {

					double rank = getBaseLevel(tag);

					// i iterates through title, body, code
					for (int i = 0; i < 3; i ++) {
						for (String term : (HashSet <String>) termSets[i]) {
							Association association = termMapping.get(i).get(term);
							if (association != null) {
								rank += weights[i] * getStrengthAssociation(i, term, tag) * association.getAttentionWeight();
							}
						}
					}

					//System.out.println(record[0] + " " + tag + " " + rank);
					//priQueue.add(tag, rank);
					queue[index++] = new TagRank(tag, rank);
				}

				//String [] top5Tags = priQueue.getHighest(5);
				Arrays.sort(queue);

				int topNumber = 3;
				// get top 3 tags
				writer.write(reader.get(0));
				String tags = "\"";
				for (int i = 0; i < topNumber; i ++) {
					tags = tags + queue[queue.length - i - 1].getTag();
					if (i != topNumber - 1)
						tags = tags + (" ");
				}
				tags = tags + "\"";
				writer.write(tags);
				writer.endRecord();
			}
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private double getStrengthAssociation (int index, String term, String tag) {
		// index 0: title, index 1: body, index 2 : code
		double probabilityOfTagOverTerm = termMapping.get(index).get(term).getProbabilityOfTagOverTerm(tag);
		if (probabilityOfTagOverTerm != 0.0)
			return Math.log(probabilityOfTagOverTerm / ((double) tagToDocumentFrequency.get(tag) / numberOfDocuments));		
		return 0.0;
	}

	@Override
	public void saveModel (String modelFile) {
		// TODO Auto-generated method stub
	}

	@Override
	public void loadModel (String modelFile) {
		// TODO Auto-generated method stub
	}
}
