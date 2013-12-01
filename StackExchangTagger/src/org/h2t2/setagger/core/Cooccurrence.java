/**
 * 
 */
package org.h2t2.setagger.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.h2t2.setagger.util.RankPriorityQueue;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.aliasi.spell.TfIdfDistance;
import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;

/**
 * An implementation of co-occurrence model
 * 
 * @author Yu-chun Huang
 * 
 */
public class Cooccurrence implements Model {
	private static final int MAX_QUEUE_SIZE = 5;

	private TfIdfDistance tfIdf = new TfIdfDistance(new PorterStemmerTokenizerFactory(new EnglishStopTokenizerFactory(
	        new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE))));

	// Model data.
	private CooccurrenceModelData modelData;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.h2t2.setagger.core.ModelBase#train(java.lang.String, java.lang.String[])
	 */
	@Override
	public void train(String trainFileName, String[] args) {
		modelData = new CooccurrenceModelData();
		Map<String, Double> idfMap = modelData.getTagIdfMap();
		Map<String, Map<String, Double>> tagProbability = modelData.getTagProbability();
		Map<String, Integer> termDocCount = new HashMap<String, Integer>();

		try {
			CSVReader reader = new CSVReader(new FileReader(trainFileName));

			int invalidCount = 0;
			String[] record;
			while ((record = reader.readNext()) != null) {
				// Must contain 5 columns: ID, title, body without code, code, and tags
				if (record.length != 5) {
					invalidCount++;
					continue; // Invalid record, just ignore it.
				}

				// Put tags into tfIdf for later Inverse Document Frequency (IDF) calculation.
				tfIdf.handle(record[4]);

				// Calculate P(tag|term). Stored as Map<term, Map<tag, probability>>.
				Set<String> termSet = new HashSet<String>();
				String content = (record[1] + " " + record[2]).trim();
				String[] terms = content.split("\\s+");

				// Find all unique terms
				for (String term : terms) {
					termSet.add(term);
				}

				for (String term : termSet) {
					// Calculate total number of documents that a specific term appears in.
					Integer termDocValue = termDocCount.get(term);
					if (termDocValue != null) {
						termDocCount.put(term, termDocValue + 1);
					} else {
						termDocCount.put(term, 1);
					}

					// Calculate total number of documents that a term and a tag both appear in.
					Map<String, Double> innerMap = tagProbability.get(term);
					if (innerMap == null) {
						innerMap = new HashMap<String, Double>();
					}

					String[] tags = record[4].split("\\s+");
					for (String tag : tags) {
						Double value = innerMap.get(tag);
						if (value != null) {
							innerMap.put(tag, value + 1.0);
						} else {
							innerMap.put(tag, 1.0);
						}
					}

					tagProbability.put(term, innerMap);
				}
			}

			reader.close();
			if (invalidCount > 0) {
				System.out.println("Warning! Found " + invalidCount + " invalid records.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Calculate Inverse Document Frequency (IDF).
		for (String term : tfIdf.termSet()) {
			idfMap.put(term, tfIdf.idf(term));
		}

		// Calculate P(tag|term).
		// Divide total number of documents with a term and a tag both appear in by termDocCount
		for (Map.Entry<String, Map<String, Double>> entry : tagProbability.entrySet()) {
			String key = entry.getKey(); // Term
			double count = termDocCount.get(key);

			Map<String, Double> value = entry.getValue();
			for (Map.Entry<String, Double> innerEntry : value.entrySet()) {
				innerEntry.setValue(innerEntry.getValue() / count);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.h2t2.setagger.core.ModelBase#predict(java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
	public void predict(String predictFileName, String outputFileName, String[] args) {
		if (modelData == null) {
			System.out.println("Warning! Model has not been trained or loaded yet. Abort prediction.");
			return;
		}

		try {
			CSVReader reader = new CSVReader(new FileReader(predictFileName));
			CSVWriter writer = new CSVWriter(new FileWriter(outputFileName), ',');

			String[] record;
			int invalidCount = 0;
			while ((record = reader.readNext()) != null) {
				if (record.length < 4) {
					invalidCount++;
					continue; // Invalid record, just ignore it.
				}

				Map<String, Double> termFrequency = new HashMap<String, Double>();
				String content = (record[1] + " " + record[2]).trim();
				String[] terms = content.split("\\s+");

				// Compute term counts.
				for (String term : terms) {
					Double frequency = termFrequency.get(term);
					if (frequency != null) {
						termFrequency.put(term, frequency + 1.0);
					} else {
						termFrequency.put(term, 1.0);
					}
				}

				// Compute term frequency.
				for (Map.Entry<String, Double> entry : termFrequency.entrySet()) {
					entry.setValue(entry.getValue() / terms.length);
				}

				// Calculate rank score for each tag.
				RankPriorityQueue queue = new RankPriorityQueue(MAX_QUEUE_SIZE);

				Set<String> tagSet = modelData.getTagIdfMap().keySet();
				for (String tag : tagSet) {
					double rank = 0;
					for (Map.Entry<String, Double> entry : termFrequency.entrySet()) {
						Map<String, Double> tagProbability = modelData.getTagProbability().get(entry.getKey());
						if (tagProbability != null) {
							Double probability = tagProbability.get(tag);
							if (probability != null) {
								rank += probability * entry.getValue();
							}
						}
					}
					rank = rank * modelData.getTagIdfMap().get(tag);
					queue.add(tag, rank);
				}

				// Put tags.
				String[] tagArray = queue.getHighest(MAX_QUEUE_SIZE);
				String tagString = "";
				for (String tag : tagArray) {
					tagString += tag + " ";
				}
				String[] result = new String[] { record[0], tagString.trim() };
				writer.writeNext(result);
			}

			reader.close();
			writer.close();

			if (invalidCount > 0) {
				System.out.println("Warning! Found " + invalidCount + " invalid records.");
			}
		} catch (IOException e) {
			System.out.println("Fail to load predict data or write predict result!");
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.h2t2.setagger.core.Model#saveModel(java.lang.String)
	 */
	@Override
	public void saveModel(String modelFile) {
		if (modelData == null) {
			System.out.println("Warning! Model has not been trained yet. Model not Saved!");
		}

		try {
			FileOutputStream fos = new FileOutputStream(modelFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(modelData);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.h2t2.setagger.core.Model#loadModel(java.lang.String)
	 */
	@Override
	public void loadModel(String modelFile) {
		try {
			FileInputStream fis = new FileInputStream(modelFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			modelData = (CooccurrenceModelData) ois.readObject();
			ois.close();
		} catch (Exception e) {
			System.out.println("Warning! Failed to load model!");
			e.printStackTrace();
		}
	}

}
