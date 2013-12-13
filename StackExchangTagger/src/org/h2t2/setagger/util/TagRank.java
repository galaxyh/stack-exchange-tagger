/**
 * 
 */
package org.h2t2.setagger.util;

/**
 * @author Yu-chun Huang
 * 
 */
public class TagRank implements Comparable<TagRank> {
	private String tag;
	private double rank;
	public static double eps = Math.pow(10, -6);

	/**
	 * @param tag
	 * @param rank
	 */
	public TagRank() {
		super();
	}

	/**
	 * @param tag
	 * @param rank
	 */
	public TagRank(String tag, double rank) {
		super();

		if (tag == null) {
			throw new IllegalArgumentException("Tag cannot be null.");
		}

		this.tag = tag;
		this.rank = rank;
	}

	/**
	 * @param tag
	 * @param rank
	 */
	public void setTagRank(String tag, double rank) {
		if (tag == null) {
			throw new IllegalArgumentException("Tag can not be null.");
		}

		this.tag = tag;
		this.rank = rank;
	}

	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param tag
	 *            the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * @return the rank
	 */
	public double getRank() {
		return rank;
	}

	/**
	 * @param rank
	 *            the rank to set
	 */
	public void setRank(double rank) {
		this.rank = rank;
	}

	@Override
	public int compareTo(TagRank other) {
	//	if (Math.abs(this.rank-other.getRank()) < eps){
	//		return 0;
	//	}
		if (this.rank > other.getRank()) {
			return 1;
		} else {
			return -1;
		}
	}

}
