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

public class CBMapReduce {

    public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, TagRankWritable> {

        private HashSet<String> getUniqueTermSet (String content) {
            HashSet <String> set = new HashSet<String>();
            for (String term : content.split("\\s+")) {
                set.add(term);
            }
            return set;
        }

        public void map (LongWritable key, Text value, OutputCollector<Text, TagRankWritable> output, Reporter reporter) throws IOException {

            Double[] weights = {1.0, 1.0, 1.0};

            String[] records = value.toString().split(","); // read from csv

            Text id = new Text(records[0]);

            Object [] termSets = {getUniqueTermSet(records[1]), getUniqueTermSet(records[2]), getUniqueTermSet(records[3])};

            // TODO read allTagsSet from DistributedCache
            String[] allTagsSet = {};

            for (String tag : allTagsSet) {
                double rank = 0.0;
                double tagTf = 0.0;

                for (int i = 0; i < 3; i ++) {
                    for (String term : (HashSet<String>) termSets[i]) {
                        if (term.equals(tag))
                            tagTf ++;

                        // TODO add Association, getAssociation, getStrengthAssociation from CognitiveBayesian
                        Association association = cb.getAssociation(i, term);
                        if (association != null)
                            rank += weights[i] * cb.getStrengthAssociation(i, term, tag) * association.getAttentionWeight();
                    }
                }

                // send TagRank to reducer instead of priority queue
                TagRank tagRank = new TagRank(tag, rank);
                output.collect(id, new TagRankWritable(tagRank));

            }
        }

    }

    public static class Reduce extends MapReduceBase implements Reducer<Text, TagRankWritable, Text, Text> {

        public void reduce (Text key, Iterator<TagRankWritable> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {

            // TODO find top 5 TagRank from values

            // TODO join 5 tags into 1 string
            String top5Tags = "";

            output.collect(key, new Text(top5Tags));
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