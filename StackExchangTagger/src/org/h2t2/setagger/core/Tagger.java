package org.h2t2.setagger.core;

import java.io.IOException;

import org.apache.commons.lang3.time.StopWatch;
import org.h2t2.setagger.util.Preprocessor;
import org.h2t2.setagger.util.Scanner;
import org.h2t2.setagger.util.TagEvaluator;
import org.h2t2.setagger.util.TagIndexProcessor;

public class Tagger {

	/**
	 * <P>
	 * Dataset pre-processing: <BR/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Usage: -pre &lt;input&gt; &lt;output&gt;
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
	 * output&gt; [additional predicting arguments]
	 * </P>
	 * 
	 * <P>
	 * Evaluate: <BR/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Usage: -eval &lt;truth data&gt; &lt;predict data&gt;
	 * </P>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			printUsage();
			return;
		}

		StopWatch stopWatch = new StopWatch();

		if ("-pre".equals(args[0])) { // Do dataset pre-processing.
			if (args.length < 3) {
				printUsage();
				return;
			}

			try {
				System.out.println("Preprocessing...");
				stopWatch.start();
				new Preprocessor().process(args[1], args[2]);
				stopWatch.stop();
				System.out.println("Done. (" + stopWatch.toString() + ")\n");
			} catch (IOException e) {
				System.out.println("Fail to pre-process the dataset!");
				e.printStackTrace();
			}
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

			System.out.println("Training...");
			stopWatch.start();
			model.train(args[2], trainArgs);
			stopWatch.stop();
			System.out.println("Done. (" + stopWatch.toString() + ")\n");

			System.out.println("Saving model...");
			model.saveModel(args[3]);
			System.out.println("Done.\n");
		} else if ("-p".equals(args[0])) { // Do prediction
			if (args.length < 5) {
				printUsage();
				return;
			}

			// Get additional predicting arguments
			String[] predictArgs = null;
			if (args.length > 5) {
				predictArgs = new String[args.length - 5];
				for (int i = 5; i < args.length; i++) {
					predictArgs[i - 5] = args[i];
				}
			}

			// Do prediction
			Model model = getModelObject(args[1]);

			System.out.println("Load model...");
			model.loadModel(args[2]);
			System.out.println("Done.\n");

			System.out.println("Predicting...");
			stopWatch.start();
			model.predict(args[3], args[4], predictArgs);
			stopWatch.stop();
			System.out.println("Done. (" + stopWatch.toString() + ")\n");
		} else if ("-eval".equals(args[0])) {
			System.out.println("Evaluating using Macro-F1...");
			stopWatch.start();
			double evalResult = TagEvaluator.evaluateMacroF1(args[1], args[2]);
			stopWatch.stop();
			System.out.println("Done. (" + stopWatch.toString() + ")");
			System.out.println("Macro-F1 score = " + evalResult + "\n");
		} else if ("-tag".equals(args[0])) {
			try {
				TagIndexProcessor tagIndexProcessor = new TagIndexProcessor();
				tagIndexProcessor.process(args[1], args[2]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if ("-scan".equals(args[0])) {
			new Scanner().scan(args[1], args[2]);
		}
	}

	private static Model getModelObject(String modelName) {
		if ("cooccurrence".equals(modelName)) {
			return new Cooccurrence();
		} else {
			return null;
		}
	}

	private static void printUsage() {
		System.out.println("Dataset preprocessing:\n    Usage: -pre <input> <output>");
		System.out
		        .println("Train:\n    Usage: -t <model name> <train data> <model file> [additional training arguments]");
		System.out
		        .println("Predict:\n    Usage: -p <model name> <model file> <predict data> <predict output> [additional predicting arguments]");
		System.out.println("Evaluate:\n    Usage: -eval <truth data> <predict data>");
	}
}
