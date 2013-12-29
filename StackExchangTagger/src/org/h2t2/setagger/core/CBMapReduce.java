package org.h2t2.setagger.core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.filecache.DistributedCache;

import org.h2t2.setagger.util.TagRank;
import org.h2t2.setagger.util.TagRankWritable;
import org.h2t2.setagger.util.RankPriorityQueue;

import com.csvreader.CsvReader;

public class CBMapReduce {

    public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, TagRankWritable> {

        private String file;

        private CBTrainModel model;

        private HashSet<String> getUniqueTermSet (String content) {
            HashSet <String> set = new HashSet<String>();
            for (String term : content.split("\\s+")) {
                set.add(term);
            }
            return set;
        }

        private void readModel () {
            try {
                this.model = CBTrainModel.readFromFile(this.file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String[] readRecords (Text recordText) throws IOException {
            CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(recordText.getBytes()))));
            reader.readRecord();
            return reader.getValues();
        }

        @Override
        public void configure (JobConf job) {
            try {
                Path[] localFiles = DistributedCache.getLocalCacheFiles(job);
                if (localFiles != null && localFiles.length > 0) {
                    this.file = localFiles[0].toString();
                    readModel();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("unchecked")
        public void map (LongWritable key, Text value, OutputCollector<Text, TagRankWritable> output, Reporter reporter) throws IOException {

            Double[] weights = {1.0, 1.0, 1.0};

            String[] records = readRecords(value);

            Text id = new Text(records[0]);

            Object [] termSets = {getUniqueTermSet(records[1]), getUniqueTermSet(records[2]), getUniqueTermSet(records[3])};

            CBTrainModel model = this.model;

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
                rank += Math.log(tagTf + 1) * model.getTagIdf(tag);
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
                tags += topTags[i];
                if (i != TOPNUMBER - 1)
                    tags += " ";
            }

            output.collect(key, new Text(tags));
        }

    }

    public static void run (String inputPaths, String outputPath, String modelPath) throws IOException, URISyntaxException {
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

        DistributedCache.addCacheFile(new URI(modelPath), conf);

        JobClient.runJob(conf);
    }

    public static void main (String[] args) {
        CognitiveBayesian cb = new CognitiveBayesian();
        //args[1] : titleIdf, args[2] : bodyIdf, args[3] : codeIdf, 
        cb.train(args[0], args, args[4]);
        try {
			run(args[5], args[6], args[4]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}