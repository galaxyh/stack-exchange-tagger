package org.h2t2.setagger.core;

import java.io.IOException;

import org.h2t2.setagger.util.Preprocessor;

public class Tagger {

	/**
	 * <P>
	 * Dataset pre-processing: <BR/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Usage: -pre &lt;input&gt; &lt;output&gt;
	 * </P>
	 * 
	 * <P>
	 * Train and predict: <BR/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Usage: -tp &lt;model name&gt; &lt;train data&gt; &lt;predict data&gt; &lt;predict
	 * output&gt; [additional training arguments]
	 * </P>
	 * 
	 * <P>
	 * Train: <BR/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Usage: -t &lt;model name&gt; &lt;train data&gt; &lt;model file&gt; [additional training
	 * arguments]
	 * </P>
	 * 
	 * <P>
	 * Predict: <BR/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Usage: -p &lt;model name&gt; &lt;model file&gt; &lt;predict data&gt; &lt;predict
	 * output&gt;
	 * </P>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			printUsage();
			return;
		}

		if ("-pre".equals(args[0])) { // Do dataset pre-processing.
			if (args.length < 3) {
				printUsage();
				return;
			}

			try {
				new Preprocessor().process(args[1], args[2]);
			} catch (IOException e) {
				System.out.println("Fail to pre-process the dataset!");
				e.printStackTrace();
			}
		} else if ("-tp".equals(args[0])) { // Do training and prediction.
			if (args.length < 5) {
				printUsage();
				return;
			}

			// Get additional training arguments
			String[] trainArgs = null;
			if (args.length > 5) {
				trainArgs = new String[args.length - 5];
				for (int i = 5; i < args.length; i++) {
					trainArgs[i - 5] = args[i];
				}
			}

			// Do training and prediction
			Model model = getModelObject(args[1]);
			model.loadTrainData(args[2]);
			model.train(trainArgs);
			model.loadPredictData(args[3]);
			model.predict();
			model.savePrediction(args[4]);
		} else if ("-t".equals(args[0])) { // Do training
			if (args.length < 4) {
				printUsage();
				return;
			}

			// Get additional training arguments
			String[] trainArgs = null;
			if (args.length > 4) {
				trainArgs = new String[args.length - 4];
				for (int i = 4; i < args.length; i++) {
					trainArgs[i - 4] = args[i];
				}
			}

			// Do training
			Model model = getModelObject(args[1]);
			model.loadTrainData(args[2]);
			model.train(trainArgs);
			model.saveModel(args[3]);
		} else if ("-p".equals(args[0])) { // Do prediction
			if (args.length < 5) {
				printUsage();
				return;
			}

			// Do prediction
			Model model = getModelObject(args[1]);
			model.loadModel(args[2]);
			model.loadPredictData(args[3]);
			model.predict();
			model.savePrediction(args[4]);
		}
	}

	private static Model getModelObject(String modelName) {
		if ("Dummy".equals(modelName)) {
			return new DummyModel();
		} else {
			return null;
		}
	}

	private static void printUsage() {
		System.out.println("Dataset preprocessing:\n    Usage: -pre <input> <output>");
		System.out
		        .println("Train and predict:\n    Usage: -tp <model name> <train data> <predict data> <predict output> [additional training arguments]");
		System.out
		        .println("Train:\n    Usage: -t <model name> <train data> <model file> [additional training arguments]");
		System.out.println("Train:\n    Usage: -p <model name> <model file> <predict data> <predict output>");
	}
}
