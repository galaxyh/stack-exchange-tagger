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
	private static final long serialVersionUID = -7738062312756792996L;

	// Tag IDF
	public Map<String, Double> idfMap = new HashMap<String, Double>();

	// Probability of a tag given a term. Map<term, Map<tag, probability>>.
	public Map<String, Map<String, Double>> tagProbability = new HashMap<String, Map<String, Double>>();
}
