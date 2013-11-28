/**
 * 
 */
package org.h2t2.setagger.util;

import java.util.List;

/**
 * This class provides F1, Micro-F1, and Macro-F1 measure calculation capabilities.
 * 
 * @author Yu-chun Huang
 * 
 */
public class FMeasure {

	/**
	 * F1 measure is given by the formula: 2 * TP / ( 2 * TP + FP + FN). <BR/>
	 * Note that, when true positive, false positive, and false negative are all zero, F1 score is undefined, and NaN
	 * will be returned.
	 * 
	 * @param truePositive
	 *            True positive count.
	 * @param falsePositive
	 *            False positive count.
	 * @param falseNegtive
	 *            False negative count
	 * @return F1 score.
	 */
	public static double f1(long truePositive, long falsePositive, long falseNegative) {
		long denominator = 2 * truePositive + falsePositive + falseNegative;
		if (denominator == 0) {
			return Double.NaN; // When TP, FP, FN are all zero. F1 score is undefined.
		} else {
			return 2 * (double) truePositive / denominator;
		}
	}

	/**
	 * Micro-averaged F-measure gives equal weight to each individual and is therefore considered as an average over all
	 * the individual/category pairs. It tends to be dominated by the classifiers performance on common categories. <BR/>
	 * Note that, when true positive, false positive, and false negative are all zero, F1 score is undefined, and NaN
	 * will be returned.
	 * 
	 * @param countsList
	 *            A List of long[]. Each row contains: <BR/>
	 *            index 0: true positive count, <BR/>
	 *            index 1: false positive count, and <BR/>
	 *            index 2: false negative count.
	 * @return Micro-F1 measure.
	 */
	public static double microF1(List<long[]> countsList) {
		if (countsList.size() == 0) {
			throw new IllegalArgumentException();
		}

		long totalTp = 0; // Total true positive count.
		long totalFp = 0; // Total false positive count.
		long totalFn = 0; // Total false negative count.

		for (long[] counts : countsList) {
			if (counts.length != 3) {
				throw new IllegalArgumentException();
			}

			totalTp += counts[0];
			totalFp += counts[1];
			totalFn += counts[2];
		}

		return FMeasure.f1(totalTp, totalFp, totalFn);
	}

	/**
	 * Macro-averaged F-measure gives equal weight to each category, regardless of its frequency. It is influenced more
	 * by the classifier's performance on rare categories. <BR/>
	 * Note that, when true positive, false positive, and false negative are all zero, F1 score is undefined, and NaN
	 * will be returned.
	 * 
	 * @param countsList
	 *            A List of long[]. Each row contains: <BR/>
	 *            index 0: true positive count, <BR/>
	 *            index 1: false positive count, and <BR/>
	 *            index 2: false negative count.
	 * @return Macro-F1 measure.
	 */
	public static double macroF1(List<long[]> countsList) {
		if (countsList.size() == 0) {
			throw new IllegalArgumentException();
		}

		double sumF1 = 0; // Sum of all F1 scores.
		double f1Score = 0;

		for (long[] counts : countsList) {
			if (counts.length != 3) {
				throw new IllegalArgumentException();
			}

			f1Score = f1(counts[0], counts[1], counts[2]);
			if (Double.isNaN(f1Score)) {
				return Double.NaN; // When TP, FP, FN are all zero. F1 score is undefined.
			}

			sumF1 += f1Score;
		}

		return sumF1 / countsList.size();
	}
}
