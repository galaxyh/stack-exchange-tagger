/**
 * 
 */
package org.h2t2.setagger.util;

import java.util.PriorityQueue;

/**
 * @author Yu-chun Huang
 * 
 */
public class RankPriorityQueue {
	private PriorityQueue<TagRank> queue = new PriorityQueue<TagRank>();
	private int maxSize = 0;

	public RankPriorityQueue(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("Max size must be larger than zero.");
		}

		this.maxSize = maxSize;
	}

	public boolean add(String tag, double rank) {
		boolean result = queue.add(new TagRank(tag, rank));

		if (result && maxSize > 0 && queue.size() > maxSize) {
			queue.poll();
		}

		return result;
	}

	public TagRank poll() {
		return queue.poll();
	}

	public int size() {
		return queue.size();
	}

	public TagRank[] getHighest(int numberOfTags) {
		if (numberOfTags > maxSize) {
			numberOfTags = maxSize;
		}

		if (numberOfTags > queue.size()) {
			numberOfTags = queue.size();
		}

		TagRank[] array = new TagRank[numberOfTags];

		int discard = queue.size() - numberOfTags;
		for (int i = 0; i < discard; i++) {
			queue.poll();
		}

		for (int i = numberOfTags - 1; i >= 0; i--) {
			array[i] = queue.poll();
		}

		return array;
	}
}
