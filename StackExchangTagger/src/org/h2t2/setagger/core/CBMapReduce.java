package org.h2t2.setagger.core;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

import org.h2t2.setagger.util.TagRank;
import org.h2t2.setagger.util.TagRankWritable;
import org.h2t2.setagger.util.RankPriorityQueue;

public class CBMapReduce {

    public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, TagRankWritable> {

        private HashSet<String> getUniqueTermSet (String content) {
            HashSet <String> set = new HashSet<String>();
            for (String term : content.split("\\s+")) {
                set.add(term);
            }
            return set;
        }

        // TODO read model from DistributedCache
        private CBTrainModel readModel () {
            return null;
        }

        public void map (LongWritable key, Text value, OutputCollector<Text, TagRankWritable> output, Reporter reporter) throws IOException {

            Double[] weights = {1.0, 1.0, 1.0};

            String[] records = value.toString().split(","); // read from csv

            Text id = new Text(records[0]);

            Object [] termSets = {getUniqueTermSet(records[1]), getUniqueTermSet(records[2]), getUniqueTermSet(records[3])};

            CBTrainModel model = this.readModel();

            for (String tag : model.getAllTagsSet()) {
                double rank = 0.0;
                double tagTf = 0.0;

                for (int i = 0; i < 3; i ++) {
                    for (String term : (HashSet<String>) termSets[i]) {
                        if (term.equals(tag))
                            tagTf ++;

                        Association association = model.getAssociation(i, term);
                        if (association != null)
                            rank += weights[i] * model.getStrengthAssociation(i, term, tag) * association.getAttentionWeight();
                    }
                }

                // send TagRank to reducer instead of priority queue
                rank += Math.log(tagTf + 1) * model.getTagIdf(tag));
                TagRank tagRank = new TagRank(tag, rank);
                output.collect(id, new TagRankWritable(tagRank));

            }
        }

    }

    public static class Reduce extends MapReduceBase implements Reducer<Text, TagRankWritable, Text, Text> {

        private static final int TOPNUMBER = 5;

        public void reduce (Text key, Iterator<TagRankWritable> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {

            RankPriorityQueue priorityQueue = new RankPriorityQueue(TOPNUMBER);
            TagRank value;
            while (values.hasNext()) {
                value = values.next().getTagRank();
                priorityQueue.add(value.getTag(), value.getRank());
            }

            String[] topTags = priorityQueue.getHighest(TOPNUMBER);
            String tags = "";
            for (int i = 0; i < TOPNUMBER; i ++) {
                tags += toptags[i];
                if (i != TOPNUMBER - 1)
                    tags += " ";
            }

            output.collect(key, new Text(tags));
        }

    }

    public static run (String inputPaths, String outputPath) {
        JobConf conf = new JobConf(CBMapReduce.class);
        conf.setJobName("Cognitive Bayesian for StackExchange Tagger");

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(Text.class);

        conf.setMapOutputKeyClass(Text.class);
        conf.setMapOutputValueClass(TagRankWritable.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf, new Path(inputPaths));
        FileOutputFormat.setOutputPath(conf, new Path(outputPath));

        JobClient.runJob(conf);
    }
}