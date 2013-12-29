package org.h2t2.setagger.core;

import org.h2t2.setagger.util.KnnClassifier;

import java.io.*;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.filecache.DistributedCache;
import java.net.URI;

public class KNNMR {

    public static class MyMap extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text> {
        private Path[] localFiles;
        private KnnClassifier knn;

        public void configure(JobConf job) {
            long startTime = System.currentTimeMillis();
            try {
                localFiles = DistributedCache.getLocalCacheFiles(job);
                BufferedReader reader = new BufferedReader(new FileReader(localFiles[0].toString()));
                knn = new KnnClassifier();

                String s;
                String[] record;
                while ((s = reader.readLine()) != null) {
                    record = s.split(",");
                    if(record.length != 5) {
                        System.err.println("BufferedReader error");
                        System.exit(1);
                    }

                    knn.trainTfIdf(record);
                }
                knn.endTrainTfIdf();

                reader = new BufferedReader(new FileReader(localFiles[0].toString()));
                while ((s = reader.readLine()) != null) {
                    record = s.split(",");
                    if(record.length != 5) {
                        System.err.println("BufferedReader error");
                        System.exit(1);
                    }

                    knn.trainFeatureVector(record);
                }

                reader.close();
            }
            catch(IOException e) {
                System.out.println("IOException");
                e.printStackTrace();
            }

            System.out.println("configure time: " + (System.currentTimeMillis() - startTime));
        }

        public void map(LongWritable key, Text text, OutputCollector<IntWritable, Text> output, Reporter reporter) throws IOException {
            long startTime = System.currentTimeMillis();
            String line = text.toString();
            String[] record;
            record = line.split(",");
            if(record.length != 5){ // 5 for test, 4 for real
                System.err.println("wrong record length");
                System.exit(1);
            }

            TreeMap<Double, ArrayList<String>> nearestNeighbor = knn.classify(record);

            TreeMap<String, Double> tagRank = new TreeMap<String, Double>();
            TreeMap<Double, ArrayList<String>> maxTags = new TreeMap<Double, ArrayList<String>>();
            for(Map.Entry<Double, ArrayList<String>> entry : nearestNeighbor.entrySet()) {
                Double proximity = entry.getKey();
                Double value;
                for(String str : entry.getValue()) {
                    if((value = tagRank.get(str)) != null)
                        tagRank.put(str, value + proximity);
                    else
                        tagRank.put(str, proximity);
                }
            }
            ArrayList<String> tmpArrayList;
            for(Map.Entry<String, Double> entry : tagRank.entrySet()) {
                if((tmpArrayList = maxTags.get(entry.getValue())) != null) {
                    tmpArrayList.add(entry.getKey());
                }
                else {
                    tmpArrayList = new ArrayList<String>();
                    tmpArrayList.add(entry.getKey());
                    maxTags.put(entry.getValue(), tmpArrayList);
                }
            }

            Map.Entry<Double, ArrayList<String>> tmpEntry;
            StringBuilder sb = new StringBuilder();
            int cntTags = 0;
            while((tmpEntry = maxTags.pollLastEntry()) != null) {
                for(String str : tmpEntry.getValue()) {
                    sb.append(str).append(" ");
                    cntTags++;
                    if(cntTags >= 3) {
                        break;
                    }
                }
                if(cntTags >= 3) {
                    break;
                }
            }

            output.collect(new IntWritable(Integer.parseInt(record[0])), new Text(sb.toString()));
            System.out.println("map " + record[0] + " time: " + (System.currentTimeMillis() - startTime));
        }
    }

    public static void main(String[] args) throws Exception {
        JobConf conf = new JobConf(KNNMR.class);
        conf.setJobName("KNNMR");

        conf.setMapOutputKeyClass(IntWritable.class);
        conf.setMapOutputValueClass(Text.class);
        conf.setOutputKeyClass(IntWritable.class);
        conf.setOutputValueClass(Text.class);

        conf.setMapperClass(MyMap.class);
        //conf.setCombinerClass(Reduce.class);
        //conf.setReducerClass(Reduce.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        conf.setNumReduceTasks(0);

        //conf.setKeyFieldComparatorOptions("-n");
        conf.set("mapred.textoutputformat.separator", ",");

        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[2]));

        DistributedCache.addCacheFile(new URI(args[1]), conf);

        JobClient.runJob(conf);
    }
}
