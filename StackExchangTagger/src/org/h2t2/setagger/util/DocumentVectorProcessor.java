package org.h2t2.setagger.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;



public class DocumentVectorProcessor {
	private  ArrayList <HashMap <String, Term>> termMapping;
	private HashMap <String, Integer> tagToIndex;
	private int globalIndex = 0;
	public static double idfUpperBound = 10.0;
	
	private class Term{
		public int index;
		public double idf;
		public Term( int i, double d){
			index = i;
			idf = d;
		}
	}
	
	public DocumentVectorProcessor(String titleIdfFile, String bodyIdfFile, String codeIdfFile, String tagIndexFile) throws IOException{
		termMapping = new ArrayList<HashMap <String, Term>>(3);
		
		termMapping.add(readIdfFile(titleIdfFile));
		termMapping.add(readIdfFile(bodyIdfFile));
		termMapping.add(readIdfFile(codeIdfFile));
		
		tagToIndex = readTagIndexFile(tagIndexFile);
		
	}
	
	public void makeVector(String trainFile, String output) throws IOException{
		CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(new File(trainFile)), "UTF8"));
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		String [] record = null;
		String [] terms;
		String [] tags;
		while((record = reader.readNext()) != null){
			if(record.length != 5)continue;
			StringBuilder data = new StringBuilder();
			for(int i = 0;i < termMapping.size();i++){
				terms = record[i+1].split("\\s+");
				HashMap <String, Double> frequencyMap = calculateFrequency(terms);
				for(String term : frequencyMap.keySet()){
					Term t = termMapping.get(i).get(term);
					Double tf = frequencyMap.get(term);
					if(t != null  && tf != null ){
						data.append(t.index+":"+(t.idf*tf)+" ");
					}
				}
			}
			
			
			tags = record[4].split("\\s+");
			String labels = "";
			for(int i = 0;i < tags.length-1;i++){
				if(tagToIndex.get(tags[i]) != null)labels += (tagToIndex.get(tags[i])+",");  				
			}
			if(tagToIndex.get(tags[tags.length-1]) != null) labels += tagToIndex.get(tags[tags.length-1]);
			writer.write(labels + " " + data.toString().trim() + "\n");
			
		}
		reader.close();
		writer.close();
	}
	
	private HashMap <String, Double> calculateFrequency(String [] terms){
		 
		HashMap <String, Double> termToFrequency = new HashMap <String, Double>();
		Double frequency = null;
		for(String term : terms){
			if((frequency = termToFrequency.get(term)) == null){
				termToFrequency.put(term, 1.0);
			}else {
				termToFrequency.put(term, frequency+1.0);
			}
		}
		return termToFrequency;
		
	}
	
	private HashMap <String, Integer> readTagIndexFile(String input) throws IOException{
		HashMap <String, Integer> tagToIndex = new HashMap <String, Integer>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input)), "UTF8"));
		String line = null;
		while((line = reader.readLine()) != null ){
			String [] tag_index = line.trim().split("\\s+");
			tagToIndex.put(tag_index[1], Integer.parseInt(tag_index[0]));
		}
		reader.close();
		return tagToIndex;
	
	}
	
	public int getNumberOfTerms(){
		return globalIndex;
	}
	
	
	private HashMap <String, Term> readIdfFile(String input) throws IOException{
		HashMap <String, Term> termToProperty = new HashMap <String, Term>(); 
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input)), "UTF8"));
		reader.readLine();// read header
		String line = null;
		while((line = reader.readLine()) != null ){
			
			String [] term_idf = line.trim().split("\\s+");
			if(Double.parseDouble(term_idf[1]) < idfUpperBound){
				globalIndex++;
				termToProperty.put(term_idf[0], new Term(globalIndex, Double.parseDouble(term_idf[1])) );
			}
			
			
		}
		
		reader.close();
		return termToProperty;
		
		
	}
	
	
}
