/**
 * 
 */
package org.h2t2.setagger.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

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
	private static TfIdfDistance tfIdf = new TfIdfDistance(new PorterStemmerTokenizerFactory(
	        new EnglishStopTokenizerFactory(new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE))));

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

		// Calculate Inverse Document Frequency (IDF)
		List<StackExchangeData> trainData = this.getTrainData();
		for (StackExchangeData data : trainData) {
			tfIdf.handle(data.getTagString());
		}

		for (String term : tfIdf.termSet()) {
			modelData.idfMap.put(term, tfIdf.idf(term));
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
