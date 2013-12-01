/**
 * 
 */
package org.h2t2.setagger.util;

import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Yu-chun Huang
 * 
 */
public class TagEvaluator {

	/**
	 * Calculate F1 score of the predicted tags. <BR/>
	 * Note that, for this particular implementation, when ground truth and prediction are all empty, the prediction is
	 * correct, so 1.0 will be returned.
	 * 
	 * @param trueTags
	 *            Ground truth tags (separated by space)
	 * @param predictTags
	 *            Predicted tags (separated by space)
	 * @return F1 score of the predicted tags.
	 */
	public static double f1(String trueTags, String predictTags) {
		long[] result = getPredictResult(trueTags, predictTags);
		return FMeasure.f1(result[0], result[1], result[2]);
	}

	/**
	 * Calculate true positive, false positive, and false negative counts of the given predicted tag string. <BR/>
	 * When ground truth and prediction are all empty, the prediction is correct. Set true positive as 1.
	 * 
	 * @param trueTags
	 *            Ground truth tags (separated by space)
	 * @param predictTags
	 *            Predicted tags (separated by space)
	 * @return An array containing the following information: <BR/>
	 *         index 0: true positive count, <BR/>
	 *         index 1: false positive count, and <BR/>
	 *         index 2: false negative count.
	 */
	public static long[] getPredictResult(String trueTags, String predictTags) {
		if (trueTags == null || predictTags == null) {
			throw new IllegalArgumentException();
		}

		String[] trueTokens = trueTags.split(" ");
		String[] predictTokens = predictTags.split(" ");

		// Index 0: true positive; 1: false positive; 2: false negative
		long[] result = new long[3];

		// When ground truth and prediction are all empty, the prediction is correct. Set true positive as 1 and
		// return immediately.
		if (trueTokens.length == 0 && predictTokens.length == 0) {
			result[0] = 1;
			return result;
		}

		// True positive
		for (String predictToken : predictTokens) {
			for (String trueToken : trueTokens) {
				if (predictToken.equals(trueToken)) {
					result[0]++;
					break;
				}
			}
		}

		// False positive
		result[1] = predictTokens.length - result[0];

		// False negative
		if (trueTokens.length > result[0]) {
			result[2] = trueTokens.length - result[0];
		}

		return result;
	}

	public static double evaluateMacroF1(String truthFileName, String predictFileName) {
		double sumF1 = 0;
		int recordCount = 0;
		int invalidCount = 0;

		try {
			CSVReader truthReader = new CSVReader(new FileReader(truthFileName));
			CSVReader predictReader = new CSVReader(new FileReader(predictFileName));

			String[] truthRecord;
			String[] predictRecord;

			predictReader.readNext(); // Skip header.

			while ((truthRecord = truthReader.readNext()) != null) {
				recordCount++;

				// Must contain 5 columns: ID, title, body without code, code, and tags
				if (truthRecord.length != 5) {
					invalidCount++;
					continue; // Invalid record, just ignore it.
				}

				if ((predictRecord = predictReader.readNext()) == null) {
					break;
				}

				while (!truthRecord[0].equals(predictRecord[0])) {
					System.out.println("Can not find matching predict ID (" + truthRecord[0]
					        + "), try next predict record.");
					if ((predictRecord = predictReader.readNext()) == null) {
						break;
					}
				}

				sumF1 += TagEvaluator.f1(truthRecord[4], predictRecord[1]);
			}

			truthReader.close();
			predictReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (invalidCount > 0) {
			System.out.println("Warning! Found " + invalidCount + " invalid records.");
		}

		System.out.println("Total " + recordCount + " records are processed.");

		if ((recordCount - invalidCount) <= 0) {
			System.out.println("Warning! No record is processed.");
			return 0;
		} else {
			return sumF1 / (recordCount - invalidCount);
		}
	}
}
