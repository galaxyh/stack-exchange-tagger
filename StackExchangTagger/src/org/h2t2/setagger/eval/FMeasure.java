/**
 * 
 */
package org.h2t2.setagger.eval;

import java.util.List;

/**
 * This class provides F1, Micro-F1, and Macro-F1 measure calculation capabilities.
 * 
 * @author Yu-chun Huang
 * 
 */
public class FMeasure {

	/**
	 * F1 measure is given by the formula: 2 * precision * recall / (precision + recall).
	 * 
	 * @param truePositive
	 *            True positive count.
	 * @param falsePositive
	 *            False positive count.
	 * @param falseNegtive
	 *            False negative count
	 * @return F1 score (returning NaN is possible)
	 */
	public static double f1(double truePositive, double falsePositive, double falseNegative) {
		// Although the following condition will result in NaN anyway,
		// we still test it and return NaN for efficiency.
		if ((truePositive + falsePositive) == 0 || (truePositive + falseNegative) == 0) {
			return Double.NaN;
		}

		double precision = truePositive / (truePositive + falsePositive);
		double recall = truePositive / (truePositive + falseNegative);

		// If (precision + recall) is 0, NaN will be returned.
		return 2 * precision * recall / (precision + recall);
	}

	/**
	 * Micro-averaged F-measure gives equal weight to each individual and is therefore considered as an average over all
	 * the individual/category pairs. It tends to be dominated by the classifiers performance on common categories.
	 * 
	 * @param countsList
	 *            A List of List of Doubles. Each row contains: index 0: true positive count, index 1: false positive
	 *            count, and index 2: false negative count.
	 * @return Micro-F1 measure (returning NaN is possible)
	 */
	public static double microF1(List<List<Double>> countsList) {
		if (countsList.size() == 0) {
			throw new IllegalArgumentException();
		}

		double totalTp = 0; // Total true positive count.
		double totalFp = 0; // Total false positive count.
		double totalFn = 0; // Total false negative count.

		for (List<Double> counts : countsList) {
			if (counts.size() != 3) {
				throw new IllegalArgumentException();
			}

			totalTp += counts.get(0);
			totalFp += counts.get(1);
			totalFn += counts.get(2);
		}

		return FMeasure.f1(totalTp, totalFp, totalFn);
	}

	/**
	 * Macro-averaged F-measure gives equal weight to each category, regardless of its frequency. It is influenced more
	 * by the classifier's performance on rare categories.
	 * 
	 * @param countsList
	 *            A List of List of Doubles. Each row contains: index 0: true positive count, index 1: false positive
	 *            count, and index 2: false negative count.
	 * @return Macro-F1 measure (returning NaN is possible)
	 */
	public static double macroF1(List<List<Double>> countsList) {
		if (countsList.size() == 0) {
			throw new IllegalArgumentException();
		}

		double sumF1 = 0; // Sum of all F1 scores.

		for (List<Double> counts : countsList) {
			if (counts.size() != 3) {
				throw new IllegalArgumentException();
			}

			sumF1 += f1(counts.get(0), counts.get(1), counts.get(2));
		}

		return sumF1 / countsList.size();
	}
}
