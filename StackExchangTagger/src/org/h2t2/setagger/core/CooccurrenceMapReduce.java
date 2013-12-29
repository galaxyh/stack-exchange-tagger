package org.h2t2.setagger.core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.h2t2.setagger.util.RankPriorityQueue;
import org.h2t2.setagger.util.TagRank;
import org.h2t2.setagger.util.TagRankWritable;

import com.csvreader.CsvReader;

public class CooccurrenceMapReduce {

	public static class CoocurrenceMapper extends MapReduceBase implements
	        Mapper<LongWritable, Text, Text, TagRankWritable> {
		CooccurrenceModelData modelData = null;
		int invalidCount = 0;

		public void loadModel(String modelFile) {
			try {
				FileInputStream fis = new FileInputStream(modelFile);
				ObjectInputStream ois = new ObjectInputStream(fis);
				modelData = (CooccurrenceModelData) ois.readObject();
				ois.close();
			} catch (Exception e) {
				System.out.println("Warning! Failed to load model!");
				e.printStackTrace();
			}
		}

		@Override
		public void configure(JobConf job) {
			try {
				String modelFile = null;

				// Read distributed cache file path
				Path[] localFiles = DistributedCache.getLocalCacheFiles(job);
				if (localFiles != null && localFiles.length > 0) {
					modelFile = localFiles[0].toString();
				}

				// Load model from distributed cache file
				loadModel(modelFile);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void map(LongWritable key, Text value, OutputCollector<Text, TagRankWritable> output, Reporter reporter)
		        throws IOException {
			if (modelData == null) {
				System.out.println("Warning! Model has not been trained or loaded yet. Abort prediction.");
				return;
			}

			InputStream is = new ByteArrayInputStream(value.getBytes());
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			CsvReader reader = new CsvReader(br);

			reader.readRecord();
			if (reader.getColumnCount() < 4) {
				invalidCount++;
				reporter.setStatus("Invalid record found. Total invalid record count = " + invalidCount);
				return; // Invalid record, just ignore it.
			}

			Map<String, Double> termFrequency = new HashMap<String, Double>();
			String content = (reader.get(1) + " " + reader.get(2)).trim();
			String[] terms = content.split("\\s+");

			// Compute term counts.
			for (String term : terms) {
				Double frequency = termFrequency.get(term);
				if (frequency != null) {
					termFrequency.put(term, frequency + 1.0);
				} else {
					termFrequency.put(term, 1.0);
				}
			}

			// Compute term frequency.
			for (Map.Entry<String, Double> entry : termFrequency.entrySet()) {
				entry.setValue(entry.getValue() / terms.length);
			}

			Set<String> tagSet = modelData.getTagIdfMap().keySet();
			for (String tag : tagSet) {
				double rank = 0;
				for (Map.Entry<String, Double> entry : termFrequency.entrySet()) {
					Map<String, Double> tagProbability = modelData.getTagProbability().get(entry.getKey());
					if (tagProbability != null) {
						Double probability = tagProbability.get(tag);
						if (probability != null) {
							rank += probability * entry.getValue();
						}
					}
				}
				rank = rank * modelData.getTagIdfMap().get(tag);
				TagRank tagRank = new TagRank(tag, rank);
				output.collect(new Text(reader.get(0)), new TagRankWritable(tagRank));
			}
		}
	}

	public static class CoocurrenceReducer extends MapReduceBase implements Reducer<Text, TagRankWritable, Text, Text> {
		private static final int TOPNUMBER = 3;

		@Override
		public void reduce(Text key, Iterator<TagRankWritable> values, OutputCollector<Text, Text> output,
		        Reporter reporter) throws IOException {

			RankPriorityQueue priorityQueue = new RankPriorityQueue(TOPNUMBER);
			TagRank value;
			while (values.hasNext()) {
				value = values.next().getTagRank();
				priorityQueue.add(value.getTag(), value.getRank());
			}

			String[] topTags = priorityQueue.getHighest(TOPNUMBER);
			String tags = "";
			for (int i = 0; i < TOPNUMBER; i++) {
				tags += topTags[i] + " ";
			}

			output.collect(key, new Text("\"" + tags.trim() + "\""));
		}

	}

	public static void run(String inputPaths, String outputPath, String modelPath) throws IOException,
	        URISyntaxException {
		JobConf conf = new JobConf(CooccurrenceMapReduce.class);
		conf.setJobName("Coocurrence for StackExchange Tagger");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapOutputKeyClass(Text.class);
		conf.setMapOutputValueClass(TagRankWritable.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		// Specify mapper and reducer
		conf.setMapperClass(CoocurrenceMapper.class);
		conf.setReducerClass(CoocurrenceReducer.class);

		// Set separator as comma (default setting is tab)
		conf.set("mapred.textoutputformat.separator", ",");

		FileInputFormat.setInputPaths(conf, new Path(inputPaths));
		FileOutputFormat.setOutputPath(conf, new Path(outputPath));

		DistributedCache.addCacheFile(new URI(modelPath), conf);

		JobClient.runJob(conf);
	}

	public static void main(String[] args) {
		System.out.println(args[2] + " " + args[3] + "" + args[4]);
		try {
			run(args[2], args[3], args[4]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}