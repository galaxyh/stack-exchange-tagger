package org.h2t2.setagger.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to store the training result. It is necessary for predicting tags.
 * 
 * @author Yu-chun Huang
 * 
 */
public class CooccurrenceModelData implements Serializable {
	private static final long serialVersionUID = -3111843070109984433L;

	// Tag IDF
	private Map<String, Double> tagIdfMap = new HashMap<String, Double>();

	// Probability of a tag given a term. Map<term, Map<tag, probability>>.
	private Map<String, Map<String, Double>> tagProbability = new HashMap<String, Map<String, Double>>();

	/**
	 * @return the tagIdfMap
	 */
	public Map<String, Double> getTagIdfMap() {
		return tagIdfMap;
	}

	/**
	 * @param tagIdfMap
	 *            the tagIdfMap to set
	 */
	public void setTagIdfMap(Map<String, Double> tagIdfMap) {
		this.tagIdfMap = tagIdfMap;
	}

	/**
	 * @return the tagProbability
	 */
	public Map<String, Map<String, Double>> getTagProbability() {
		return tagProbability;
	}

	/**
	 * @param tagProbability
	 *            the tagProbability to set
	 */
	public void setTagProbability(Map<String, Map<String, Double>> tagProbability) {
		this.tagProbability = tagProbability;
	}
}
