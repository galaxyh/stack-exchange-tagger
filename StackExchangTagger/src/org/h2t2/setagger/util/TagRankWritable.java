package org.h2t2.setagger.util;

import org.apache.hadoop.io.*;

public class TagRankWritable extends MapWritable {

	private static final Text TAG = new Text("TAG");
	private static final Text RANK = new Text("RANK");

	public TagRankWritable(TagRank tagRank) {
		this.put(TAG, new Text(tagRank.getTag()));
		this.put(RANK, new DoubleWritable(tagRank.getRank()));
	}

	public TagRank getTagRank() {
		String tag = this.get(TAG).toString();
		double rank = Double.parseDouble(this.get(RANK).toString());
		return new TagRank(tag, rank);
	}

}