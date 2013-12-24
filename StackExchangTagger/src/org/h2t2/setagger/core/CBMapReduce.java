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

        public void map (LongWritable key, Text value, OutputCollector<Text, TagRankWritable> output, Reporter reporter) throws IOException {
            ;
        }

    }

    public static class Reduce extends MapReduceBase implements Reducer<Text, TagRankWritable, Text, Text> {

        public void reduce (Text key, TagRankWritable value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
            ;
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