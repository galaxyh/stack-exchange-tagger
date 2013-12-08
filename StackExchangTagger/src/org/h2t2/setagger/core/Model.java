/**
 * 
 */
package org.h2t2.setagger.core;

/**
 * An interface for models.
 * 
 * @author Yu-chun Huang
 * 
 */
public interface Model {

	/**
	 * Use training data to train the model.
	 * 
	 * @param trainFileName
	 * @param args
	 *            Arguments to be used when training the model.
	 */
	public void train(String trainFileName, String[] args);

	/**
	 * Use the model to predict tags.
	 * 
	 * @param predictFileName
	 * @param outputFileName
	 * @param args
	 *            Arguments to be used when predicting the model.
	 */
	public void predict(String predictFileName, String outputFileName, String[] args);

	/**
	 * Save the trained model into the specified file for later use.
	 * 
	 * @param modelFile
	 */
	public void saveModel(String modelFile);

	/**
	 * Load the saved model from the specified file.
	 * 
	 * @param modelFile
	 */
	public void loadModel(String modelFile);

}
