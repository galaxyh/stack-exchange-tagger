/**
 * 
 */
package org.h2t2.setagger.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	public Cooccurrence(){
		
	}
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

		Map<String, Double> idfMap = modelData.getIdfMap();
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

		// Divide total number of documents that a term and a tag both appear in by termDocCount
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
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.h2t2.setagger.core.Model#saveModel(java.lang.String)
	 */
	@Override
	public void saveModel(String modelFile) {
		if (modelData == null) {
			System.out.print("Warning! Model has not been trained yet. Model not Saved!");
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
			System.out.print("Warning! Failed to load model!");
			e.printStackTrace();
		}
	}

}
