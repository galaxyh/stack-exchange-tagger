/**
 * 
 */
package org.h2t2.setagger.core;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author Yu-chun Huang
 * 
 */
public abstract class ModelBase {
	private List<StackExchangeData> trainData;
	private List<StackExchangeData> predictData;

	/**
	 * Load training data.
	 * 
	 * @param fileName
	 */
	public void loadTrainData(String fileName) {
		trainData = new ArrayList<StackExchangeData>();

		try {
			CSVReader reader = new CSVReader(new FileReader(fileName));

			String[] record;
			while ((record = reader.readNext()) != null) {
				if (record.length != 5) {
					continue; // Invalid record, just ignore it.
				}

				StackExchangeData data = new StackExchangeData(record[0], record[1], record[2], record[3], record[4]);
				trainData.add(data);
			}

			reader.close();

		} catch (IOException e) {
			System.out.println("Fail to load training data!");
			e.printStackTrace();
		}
	}

	/**
	 * Load data to be predicted. <BR/>
	 * Note that, the loaded dataset doesn't include tags.
	 * 
	 * @param fileName
	 */
	public void loadPredictData(String fileName) {
		predictData = new ArrayList<StackExchangeData>();

		try {
			CSVReader reader = new CSVReader(new FileReader(fileName));

			String[] record;
			while ((record = reader.readNext()) != null) {
				if (record.length != 4) {
					continue; // Invalid record, just ignore it.
				}

				StackExchangeData data = new StackExchangeData(record[0], record[1], record[2], record[3]);
				predictData.add(data);
			}

			reader.close();

		} catch (IOException e) {
			System.out.println("Fail to load training data!");
			e.printStackTrace();
		}
	}

	/**
	 * Save predicted tags into the specified file.
	 * 
	 * @param fileName
	 */
	public void savePrediction(String fileName) {
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(fileName), ',');

			for (StackExchangeData row : predictData) {
				String[] record = new String[2];
				record[0] = row.getId();
				record[1] = row.getTagString();
				writer.writeNext(record);
			}

			writer.close();
		} catch (IOException e) {
			System.out.println("Fail to load training data!");
			e.printStackTrace();
		}
	}

	/**
	 * @return the trainData
	 */
	public List<StackExchangeData> getTrainData() {
		return trainData;
	}

	/**
	 * @param trainData
	 *            the trainData to set
	 */
	public void setTrainData(List<StackExchangeData> trainData) {
		this.trainData = trainData;
	}

	/**
	 * @return the predictData
	 */
	public List<StackExchangeData> getPredictData() {
		return predictData;
	}

	/**
	 * @param predictData
	 *            the predictData to set
	 */
	public void setPredictData(List<StackExchangeData> predictData) {
		this.predictData = predictData;
	}

	/**
	 * Use training data to train the model.
	 * 
	 * @param args
	 *            Arguments to be used when training the model.
	 */
	public abstract void train(String[] args);

	/**
	 * Use the model to predict tags.
	 * 
	 * @param args
	 *            Arguments to be used when predicting the model.
	 */
	public abstract void predict(String[] args);

	/**
	 * Save the trained model into the specified file for later use.
	 * 
	 * @param modelFile
	 */
	public abstract void saveModel(String modelFile);

	/**
	 * Load the saved model from the specified file.
	 * 
	 * @param modelFile
	 */
	public abstract void loadModel(String modelFile);

}
