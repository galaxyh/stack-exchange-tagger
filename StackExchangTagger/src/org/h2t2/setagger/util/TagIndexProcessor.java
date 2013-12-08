package org.h2t2.setagger.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeSet;



public class TagIndexProcessor{
	private TreeSet<String> tagsSet;
	
	public void process(String input, String output) throws IOException{
		//CSVReader reader = new CSVReader(new FileReader(input), ',', '"', '\0');
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input)), "UTF8"));
		tagsSet = new TreeSet<String>();
		String line;
		String separater = ",\"";
		
		while ((line = reader.readLine()) != null) {
			int lastIndex = line.lastIndexOf(separater);
			String [] tags = line.substring(lastIndex+separater.length(), line.length()-1).split("\\s+");
			for(String tag : tags){
				if(!tag.equals(""))tagsSet.add(tag);
			}
			
		}
		
		
		int index = 1;
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		for(String tag : tagsSet){
			writer.write(index + "\t" + tag + "\n");
			index++;
		}

		writer.close();
		reader.close();
		
	}

}
