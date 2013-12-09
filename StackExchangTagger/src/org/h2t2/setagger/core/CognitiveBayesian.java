package org.h2t2.setagger.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.h2t2.setagger.util.DocumentVectorProcessor;

import au.com.bytecode.opencsv.CSVReader;


public class CognitiveBayesian implements Model{
	private ArrayList <HashMap <String, Association>> termMapping;
	private HashMap <String, Integer> tagToDocumentFrequency;
	private int numberOfDocuments = 0;
	private HashSet<String> allTagsSet;
	
	
	private class Association{
		public int tfInDoc = 0;
		public HashMap <String, Integer> tagToCooccurrence;
		public double attentionWeight = 0.0;
		public double entropy = 0.0;
		public double scaledEntropy = 0.0;
		public Association(){
			tagToCooccurrence = new HashMap <String, Integer>();
		
		}
		
		public double getProbabilityOfTagOverTerm(String tag){
			return tagToCooccurrence.get(tag)/tfInDoc;
		}
		
		public double getEntropy(){
			if(this.entropy == 0.0){
				double entropy = 0.0;
				for(String tag : tagToCooccurrence.keySet()){
					double probabilityOfTagOverTerm = getProbabilityOfTagOverTerm(tag);
					entropy += probabilityOfTagOverTerm/Math.log(probabilityOfTagOverTerm);
				}
				this.entropy = -entropy;
			}
			return this.entropy;
			
		}
		
		public void setScaledEntropy(double se){
			scaledEntropy = se;			
		}
		
		public double getScaledEntropy(){
			return scaledEntropy;
		}
		
		public void setAttentionWeight(double aw){
			attentionWeight = aw;			
		}
		
		public double getAttentionWeight(){
			return attentionWeight;
		}
		
		
	}
	
	private double getBaseLevel(String tag){
		double probabilityOfTag = tagToDocumentFrequency.get(tag)/(double)numberOfDocuments;
		return Math.log(probabilityOfTag/(1-probabilityOfTag));
	}
	@Override
	// args[1] : titleIdf, args[2] : bodyIdf , args[3] : codeIdf
	public void train(String trainFileName, String[] args){
		try {
			termMapping = new ArrayList <HashMap <String, Association>>(3);
			for(int i = 1;i <= 3;i++){
				termMapping.add(getAssociationMap(args[i]));
			}
			
			tagToDocumentFrequency = new HashMap <String, Integer>();
			allTagsSet = new HashSet<String>();
			
			// Now all disqualified terms are eliminated
			CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(new File(trainFileName)), "UTF8"));
			String [] record;
			Association termToAssociation;
			String [] tags;
			Integer TermAndTagCooccurrence;
			while((record = reader.readNext()) != null){
				if(record.length != 5)continue;
				numberOfDocuments++;
				tags = record[4].split("\\s+");
				
				
				for(String tag: tags){
					Integer frequency = null;
					if((frequency = tagToDocumentFrequency.get(tag)) == null){
						tagToDocumentFrequency.put(tag, 1);
					}else {
						tagToDocumentFrequency.put(tag, frequency+1);
					}
					allTagsSet.add(tag);
				}
				
				for(int i = 1;i <= 3;i++){
					for(String uniqueTerm : getUniqueTermSet(record[i])){
						if( (termToAssociation = termMapping.get(i-1).get(uniqueTerm)) != null){// the term is qualified because its idf < idfBound
							termToAssociation.tfInDoc++;
							for(String tag : tags){
								if((TermAndTagCooccurrence = termToAssociation.tagToCooccurrence.get(tag)) != null){
									termToAssociation.tagToCooccurrence.put(tag, TermAndTagCooccurrence+1);								
								}else {
									termToAssociation.tagToCooccurrence.put(tag, 1);									
								}
								
							}
						}
						
					}
					
				}
				
			}
			reader.close();
			// Now start to calculate Entropy, Scaled Entropy and Attention Weight
			
			for(int i = 0;i < termMapping.size();i++){
				double entropyMax = 0.0;
				double entropyTemp = 0.0;
				for(String term : termMapping.get(i).keySet()){
					if((entropyTemp = termMapping.get(i).get(term).getEntropy()) > entropyMax){
						entropyMax = entropyTemp;
					}
				}
				
				// calculate scaled entropy				
				double totalScaledEntropy = 0.0;
				for(String term : termMapping.get(i).keySet()){
					termMapping.get(i).get(term).setScaledEntropy
					(1.0-termMapping.get(i).get(term).getEntropy()/entropyMax);
					totalScaledEntropy += termMapping.get(i).get(term).getScaledEntropy();
				}
				// calculate attention weight
				for(String term : termMapping.get(i).keySet()){
					termMapping.get(i).get(term).setAttentionWeight
					(termMapping.get(i).get(term).getScaledEntropy()/totalScaledEntropy);
				}
				
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	private HashSet <String> getUniqueTermSet(String content){
		HashSet <String> set = new HashSet <String>();
		for(String term : content.split("\\s+")){
			set.add(term);
		}
		return set;
	} 
	
	private HashMap <String, Association> getAssociationMap(String idfFileName) throws Exception{
		HashMap <String,   Association> termToAssociation = new HashMap <String,   Association>(); 
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(idfFileName)), "UTF8"));
		reader.readLine();// read header
		String line = null;
		while((line = reader.readLine()) != null ){
			String [] term_idf = line.trim().split("\\s+");
			if(Double.parseDouble(term_idf[1]) < DocumentVectorProcessor.idfUpperBound){
				termToAssociation.put(term_idf[0], new Association() );
			}
			
		}
		reader.close();
		return termToAssociation;
		
	}
	

	@Override
	public void predict(String predictFileName, String outputFileName,
			String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveModel(String modelFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadModel(String modelFile) {
		// TODO Auto-generated method stub
		
	}
	

}
