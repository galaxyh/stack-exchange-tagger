/**
 * 
 */
package org.h2t2.setagger.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.h2t2.setagger.util.RankPriorityQueue;
import org.h2t2.setagger.util.TagRank;

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
public class Cooccurrence extends ModelBase {
	private static final int MAX_QUEUE_SIZE = 10;

	private TfIdfDistance tfIdf = new TfIdfDistance(new PorterStemmerTokenizerFactory(new EnglishStopTokenizerFactory(
	        new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE))));

	// Model data.
	private CooccurrenceModelData modelData;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.h2t2.setagger.core.Model#train(java.lang.String[])
	 */
	@Override
	public void train(String[] args) {
		modelData = new CooccurrenceModelData();

		// Calculate Inverse Document Frequency (IDF).
		List<StackExchangeData> trainData = this.getTrainData();
		for (StackExchangeData data : trainData) {
			tfIdf.handle(data.getTagString());
		}

		Map<String, Double> idfMap = modelData.getTagIdfMap();
		for (String term : tfIdf.termSet()) {
			idfMap.put(term, tfIdf.idf(term));
		}

		// Calculate P(tag|term). Stored as Map<term, Map<tag, probability>>.
		Map<String, Integer> termDocCount = new HashMap<String, Integer>();
		Map<String, Map<String, Double>> tagProbability = modelData.getTagProbability();

		for (StackExchangeData data : trainData) {
			Set<String> termSet = new HashSet<String>();
			String content = (data.getTitle() + " " + data.getBody()).trim();
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

				for (String tag : data.getTagList()) {
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
	 * @see org.h2t2.setagger.core.Model#predict(java.lang.String[])
	 */
	@Override
	public void predict(String[] args) {
		if (modelData == null) {
			System.out.println("Warning! Model has not been trained or loaded yet. Abort prediction.");
			return;
		}

		List<StackExchangeData> predictData = getPredictData();
		if (predictData == null) {
			System.out.println("Warning! Predict data has not been loaded yet. Abort prediction.");
			return;
		}

		for (StackExchangeData data : predictData) {
			Map<String, Double> termFrequency = new HashMap<String, Double>();
			String content = (data.getTitle() + " " + data.getBody()).trim();
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
			List<String> tagList = new ArrayList<String>();
			TagRank[] tagArray = queue.getHighest(MAX_QUEUE_SIZE);
			for (TagRank tagRank : tagArray) {
				tagList.add(tagRank.getTag());
			}
			data.setTagList(tagList);
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
