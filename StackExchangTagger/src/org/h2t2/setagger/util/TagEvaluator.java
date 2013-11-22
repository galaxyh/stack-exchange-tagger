/**
 * 
 */
package org.h2t2.setagger.util;

import java.util.ArrayList;
import java.util.List;

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
	 * Calculate Micro-F1 score of predicted tags of a set of articles. <BR/>
	 * Note that, for this particular implementation, when ground truth and prediction are all empty, the prediction is
	 * correct, so 1.0 will be returned.
	 * 
	 * @param tagsList
	 *            A List of tag string arrays. <BR/>
	 *            For the tag string array, index 0 is ground truth tags, and index 1 is predicted tags. Both tag string
	 *            are space-separated.
	 * @return Micro-F1 score of predicted tags of a set of articles.
	 */
	public static double microF1(List<String[]> tagsList) {
		return FMeasure.microF1(getPredictResultList(tagsList));
	}

	/**
	 * Calculate Macro-F1 score of predicted tags of a set of articles. <BR/>
	 * Note that, for this particular implementation, when ground truth and prediction are all empty, the prediction is
	 * correct, so 1.0 will be returned.
	 * 
	 * @param tagsList
	 *            A List of tag string arrays. <BR/>
	 *            For the tag string array, index 0 is ground truth tags, and index 1 is predicted tags. Both tag string
	 *            are space-separated.
	 * @return Macro-F1 score of predicted tags of a set of articles.
	 */
	public static double macroF1(List<String[]> tagsList) {
		return FMeasure.macroF1(getPredictResultList(tagsList));
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

	/**
	 * Calculate true positive, false positive, and false negative counts of the given predicted tag string list.
	 * 
	 * @param tagsList
	 *            A List of tag string arrays. <BR/>
	 *            For the tag string array, index 0 is ground truth tags, and index 1 is predicted tags. Both tag string
	 *            are space-separated.
	 * @return A list of arrays containing the following information: <BR/>
	 *         index 0: true positive count, <BR/>
	 *         index 1: false positive count, and <BR/>
	 *         index 2: false negative count.
	 */
	private static List<long[]> getPredictResultList(List<String[]> tagsList) {
		if (tagsList.size() == 0) {
			throw new IllegalArgumentException();
		}

		List<long[]> countsList = new ArrayList<long[]>();
		for (String[] tags : tagsList) {
			countsList.add(getPredictResult(tags[0], tags[1]));
		}

		return countsList;
	}
}
