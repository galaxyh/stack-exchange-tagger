package org.h2t2.setagger.core;

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

        public void configure(JobConf job) {
            try {
                localFiles = DistributedCache.getLocalCacheFiles(job);
                BufferedReader br = new BufferedReader(new FileReader(localFiles[0].toString()));
                String line;
                while((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                br.close();
            }
            catch(IOException e) {
                System.out.println("IOException");
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

        DistributedCache.addCacheFile(new URI("./oneLine"), conf);

        JobClient.runJob(conf);
    }
}
