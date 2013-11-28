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
public abstract class Model {
	private List<Data> trainData;
	private List<Data> predictData;

	/**
	 * Load training data.
	 * 
	 * @param fileName
	 */
	public void loadTrainData(String fileName) {
		trainData = new ArrayList<Data>();

		try {
			CSVReader reader = new CSVReader(new FileReader(fileName));

			String[] record;
			while ((record = reader.readNext()) != null) {
				if (record.length != 5) {
					continue; // Invalid record, just ignore it.
				}

				List<String> tagList = new ArrayList<String>();
				String[] tags = record[4].split(" ");
				for (String tag : tags) {
					tagList.add(tag);
				}

				Data data = new Data(record[0], record[1], record[2], record[3], tagList);
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
		predictData = new ArrayList<Data>();

		try {
			CSVReader reader = new CSVReader(new FileReader(fileName));

			String[] record;
			while ((record = reader.readNext()) != null) {
				if (record.length != 4) {
					continue; // Invalid record, just ignore it.
				}

				Data data = new Data(record[0], record[1], record[2], record[3]);
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

			for (Data row : predictData) {
				String[] record = new String[2];
				record[0] = row.getId();
				for (String tag : row.getTags()) {
					record[1] += tag + " ";
				}
				record[1] = record[1].trim();

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
	public List<Data> getTrainData() {
		return trainData;
	}

	/**
	 * @param trainData
	 *            the trainData to set
	 */
	public void setTrainData(List<Data> trainData) {
		this.trainData = trainData;
	}

	/**
	 * @return the predictData
	 */
	public List<Data> getPredictData() {
		return predictData;
	}

	/**
	 * @param predictData
	 *            the predictData to set
	 */
	public void setPredictData(List<Data> predictData) {
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
	 */
	public abstract void predict();

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
