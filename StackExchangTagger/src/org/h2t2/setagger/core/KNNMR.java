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

    public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text> {
        private Path[] localFiles;
        private KnnClassifier knn;

        public void configure(JobConf job) {
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
        }

        public void map(LongWritable key, Text value, OutputCollector<IntWritable, Text> output, Reporter reporter) throws IOException {
            String line = value.toString();
            String[] record;
            record = line.split(",");
            output.collect(new IntWritable(Integer.parseInt(record[0])), new Text(record[1]));
        }
    }

    public static void main(String[] args) throws Exception {
        JobConf conf = new JobConf(KNNMR.class);
        conf.setJobName("KNNMR");

        conf.setMapOutputKeyClass(IntWritable.class);
        conf.setMapOutputValueClass(Text.class);
        conf.setOutputKeyClass(IntWritable.class);
        conf.setOutputValueClass(Text.class);

        conf.setMapperClass(Map.class);
        //conf.setCombinerClass(Reduce.class);
        //conf.setReducerClass(Reduce.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        //conf.setKeyFieldComparatorOptions("-n");
        conf.set("mapred.textoutputformat.separator", ",");

        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        DistributedCache.addCacheFile(new URI("./Train.10w.pre"), conf);

        JobClient.runJob(conf);
    }
}
