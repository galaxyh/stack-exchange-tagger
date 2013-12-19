package org.h2t2.setagger.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.h2t2.setagger.util.DocumentVectorProcessor;
import org.h2t2.setagger.util.RankPriorityQueue;
import org.h2t2.setagger.util.TagRank;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;


public class CognitiveBayesian implements Model {
	private ArrayList <HashMap <String, Association>> termMapping;
	private HashMap <String, Integer> tagToDocumentFrequency;
	private int numberOfDocuments = 0;
	private HashSet<String> allTagsSet;
	private final Charset UTF8 = Charset.forName("UTF-8");
	
	private Executor pool;
	private CsvReader testReader;
	private BufferedWriter testWriter;

	private class Association {
		public int tfInDoc = 0;
		public HashMap <String, Integer> tagToCooccurrence;
		public Double attentionWeight;
		public Association() {
			tagToCooccurrence = new HashMap <String, Integer>();
		}

		public double getProbabilityOfTagOverTerm(String tag) {
			if (tagToCooccurrence.get(tag) == null) {
				return 0.0;
			}
			return (double) tagToCooccurrence.get(tag) / tfInDoc;
		}
		
		// attentionWeight will represent entropy at an early stage
		public double getEntropy() {
			if (this.attentionWeight == null) {
				this.attentionWeight = 0.0;
				for (String tag : tagToCooccurrence.keySet()) {
					double probabilityOfTagOverTerm = getProbabilityOfTagOverTerm(tag);
					this.attentionWeight -= probabilityOfTagOverTerm * Math.log(probabilityOfTagOverTerm);
				}
			}
			return this.attentionWeight;
		}
		// attentionWeight will represent scaledEntropy at an early stage
		public void setScaledEntropy (double se) {
			this.attentionWeight = se;
		}
		
		public double getScaledEntropy () {
			return this.attentionWeight;
		}

		public void setAttentionWeight (double aw) {
			attentionWeight = aw;
		}

		public double getAttentionWeight () {
			return attentionWeight;
		}
	}

	private double getBaseLevel (String tag) {
		Integer tagFrequency = this.tagToDocumentFrequency.get(tag);
		if (tagFrequency != null && tagFrequency != 0) {
			double probabilityOfTag = tagFrequency / (double) numberOfDocuments;
			return Math.log(probabilityOfTag / (1 - probabilityOfTag));
		} else {
			return 0.0;
		}
	}
	
	public double getTagIdf(String tag){
		Integer frequency;
		if((frequency = tagToDocumentFrequency.get(tag)) != null){
			return (double)frequency/numberOfDocuments;
		}
		return 0.0;
	}

	@Override
	// args[1] : titleIdf, args[2] : bodyIdf , args[3] : codeIdf
	public void train (String trainFileName, String[] args) {
		try {
			termMapping = new ArrayList <HashMap <String, Association>>(3);
			for (int i = 1; i <= 3; i ++) {
				termMapping.add(getAssociationMap(args[i]));
			}

			tagToDocumentFrequency = new HashMap <String, Integer>();
			allTagsSet = new HashSet<String>();
			

			// Now all disqualified terms are eliminated
			CsvReader reader = new CsvReader(new FileInputStream(new File(trainFileName)), UTF8);
			Association termToAssociation;
			String [] tags;
			Integer termAndTagCooccurrence;
			while (reader.readRecord()) {
				if (reader.getColumnCount() != 5)
					continue;
				numberOfDocuments ++;
				tags = reader.get(4).split("\\s+");

				for (String tag : tags) {
					Integer frequency = null;
					if ((frequency = tagToDocumentFrequency.get(tag)) == null) {
						tagToDocumentFrequency.put(tag, 1);
					} else {
						tagToDocumentFrequency.put(tag, frequency + 1);
					}
					allTagsSet.add(tag);
				}

				for (int i = 1; i <= 3; i ++) {
					for (String uniqueTerm : getUniqueTermSet(reader.get(i))) {
						if ((termToAssociation = termMapping.get(i - 1).get(uniqueTerm)) != null) {
							// the term is qualified because its idf < idfBound
							termToAssociation.tfInDoc ++;
							for (String tag : tags) {
								if ((termAndTagCooccurrence = termToAssociation.tagToCooccurrence.get(tag)) != null) {
									termToAssociation.tagToCooccurrence.put(tag, termAndTagCooccurrence + 1);								
								} else {
									termToAssociation.tagToCooccurrence.put(tag, 1);									
								}
							}
						}
					}
				}
			}
			reader.close();

			// Now start to calculate Entropy, Scaled Entropy and Attention Weight
			for (int i = 0; i < termMapping.size(); i ++) {
				HashMap<String, Association> singleTermMap = termMapping.get(i);

				double entropyMax = 0.0;
				double entropyTemp = 0.0;
				for (String term : singleTermMap.keySet()) {
					Association a = singleTermMap.get(term);
					if ((entropyTemp = a.getEntropy()) > entropyMax) {
						entropyMax = entropyTemp;
					}
				}

				// calculate scaled entropy
				double totalScaledEntropy = 0.0;
				for (String term : singleTermMap.keySet()) {
					Association a = singleTermMap.get(term);
					a.setScaledEntropy(1.0 - a.getEntropy() / entropyMax);
					totalScaledEntropy += a.getScaledEntropy();
				}

				// calculate attention weight
				double attentionWeightBound = 1.0;
				for (String term : singleTermMap.keySet()) {
					Association a = singleTermMap.get(term);
					a.setAttentionWeight(attentionWeightBound * a.getScaledEntropy() / totalScaledEntropy);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private HashSet <String> getUniqueTermSet (String content) {
		HashSet <String> set = new HashSet <String>();
		for (String term : content.split("\\s+")) {
			set.add(term);
		}
		return set;
	}

	private HashMap <String, Association> getAssociationMap (String idfFileName) throws Exception {
		HashMap <String, Association> termToAssociation = new HashMap <String, Association>(); 
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(idfFileName)), "UTF8"));
		reader.readLine();// read header
		String line = null;
		while ((line = reader.readLine()) != null ) {
			String [] term_idf = line.trim().split("\\s+");
			if (Double.parseDouble(term_idf[1]) < DocumentVectorProcessor.idfUpperBound) {
				termToAssociation.put(term_idf[0], new Association());
			}
		}
		reader.close();
		return termToAssociation;
	}
	
	public Association getAssociation(int i, String term){
		return termMapping.get(i).get(term);
	}
	
	 
	// THREAD 
	private class Worker implements Runnable{		
		CognitiveBayesian cb;
		public Worker(CognitiveBayesian cb){
			
			this.cb = cb;
		}
		private HashSet <String> getUniqueTermSet (String content) {
			HashSet <String> set = new HashSet <String>();
			for (String term : content.split("\\s+")) {
				set.add(term);
			}
			return set;
		}
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				String [] record = null;
				int topNumber = 5;
				RankPriorityQueue priQueue = null;
				Double [] weights = {1.0, 1.0, 1.0};
				while((record = cb.getRecord()) != null){
					if(record.length != 4){
						System.out.println(record[0]);
						continue;
					}
					priQueue = new RankPriorityQueue(topNumber);
					Object [] termSets = {getUniqueTermSet(record[1]), getUniqueTermSet(record[2]), getUniqueTermSet(record[3])};
					for (String tag : allTagsSet) {
						double rank = 0.0;
						double tagTf = 0.0;
						// i iterates through title, body, code
						for (int i = 0; i < 3; i ++) {
							for (String term : (HashSet <String>) termSets[i]) {
								if(term.equals(tag))tagTf++;
								Association association = cb.getAssociation(i, term);
								if (association != null) {
									rank += weights[i] * cb.getStrengthAssociation(i, term, tag) * association.getAttentionWeight();
								}
							}
						}
						priQueue.add(tag, rank+Math.log(tagTf+1)*cb.getTagIdf(tag));
					}

					// get top 5 tags
					String [] topTags = priQueue.getHighest(topNumber);
					String tags = record[0] + ",\"";
					//6034196,"javascript c# python php java"
					for (int i = 0; i < topNumber; i ++) {
						if (i != topNumber - 1)tags = tags + topTags[i] + " ";
						else tags = tags + topTags[i] + "\"\n";					
					}
					cb.writeRecord(tags);
				}
				cb.finalFlush();
								
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
					
		}
	public synchronized void finalFlush() throws IOException{
		testWriter.flush();
		
	}
	// THREAD
	public synchronized String [] getRecord() throws IOException{
		
		if(!testReader.readRecord()){
			return null;
		}
		return testReader.getValues();
			
	}
	
	// THREAD
	public synchronized void writeRecord(String record) throws IOException{
		// \n should not not be included for efficiency concern
		testWriter.write(record);
	}
	
	@Override
	public void predict (String predictFileName, String outputFileName, String[] args) {
		try {
			// main THREAD must have only one reader  
			testReader = new CsvReader(new FileInputStream(new File(predictFileName)), UTF8);
			testWriter = new BufferedWriter(new FileWriter(outputFileName, true));
			int threadNumber = 75;
			// header
			testWriter.write("\"Id\",\"Tags\"\n");
			
			pool = Executors.newFixedThreadPool(threadNumber);
			for(int i = 0;i < threadNumber;i++){
				pool.execute(new Worker(this));
			}
			
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void patch (String predictFileName, String outputFileName, String[] args) {
		try {
			// main THREAD must have only one reader  
			testReader = new CsvReader(new FileInputStream(new File(predictFileName)), UTF8);
			testWriter = new BufferedWriter(new FileWriter(outputFileName, true));
			
			BufferedReader bf = new BufferedReader(new FileReader(outputFileName));
			HashSet <Integer> processedRecord = new HashSet <Integer>();
			String line = null;
			line = bf.readLine();
			while((line = bf.readLine()) != null){
				processedRecord.add(Integer.parseInt(line.split(",")[0]));
			}
			int topNumber = 5;
			
			while (testReader.readRecord()) {
				if (testReader.getColumnCount() != 4) {
					System.out.println(testReader.get(0));
					continue;
				}else if( processedRecord.contains(Integer.parseInt(testReader.get(0))) ){
					continue;
				}
				RankPriorityQueue priQueue = new RankPriorityQueue(topNumber);
				Object [] termSets = {getUniqueTermSet(testReader.get(1)), getUniqueTermSet(testReader.get(2)), getUniqueTermSet(testReader.get(3))};
				Double [] weights = {1.0, 1.0, 1.0};

				int index = 0;

				for (String tag : allTagsSet) {
					double rank = 0.0;
					double tagTf = 0.0;
					// i iterates through title, body, code
					for (int i = 0; i < 3; i ++) {
						for (String term : (HashSet <String>) termSets[i]) {
							if(term.equals(tag))tagTf++;
							Association association = getAssociation(i, term);
							if (association != null) {
								rank += weights[i] * getStrengthAssociation(i, term, tag) * association.getAttentionWeight();
							}
						}
					}
					priQueue.add(tag, rank+Math.log(tagTf+1)*getTagIdf(tag));
				}

				
				String [] topTags = priQueue.getHighest(topNumber);
				String tags = testReader.get(0) + ",\"";
				//6034196,"javascript c# python php java"
				for (int i = 0; i < topNumber; i ++) {
					if (i != topNumber - 1)tags = tags + topTags[i] + " ";
					else tags = tags + topTags[i] + "\"\n";					
				}
				testWriter.write(tags);			
			}
			testWriter.flush();
			testReader.close();
			testWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public double getStrengthAssociation (int index, String term, String tag) {
		// index 0: title, index 1: body, index 2 : code
		double probabilityOfTagOverTerm = termMapping.get(index).get(term).getProbabilityOfTagOverTerm(tag);
		if (probabilityOfTagOverTerm != 0.0)
			return Math.log(probabilityOfTagOverTerm / ((double) tagToDocumentFrequency.get(tag) / numberOfDocuments));		
		return 0.0;
	}

	@Override
	public void saveModel (String modelFile) {
		// TODO Auto-generated method stub
	}

	@Override
	public void loadModel (String modelFile) {
		// TODO Auto-generated method stub
	}
}
