package org.h2t2.setagger.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Scanner {
	
	
	public void scan(String input, String output) {
		try{
			BufferedReader reader = new BufferedReader(new FileReader(input));
			BufferedWriter writer = new BufferedWriter(new FileWriter(output));
			String line;
			while((line = reader.readLine()) != null){
				writer.write(line.replaceAll("\"\"", " ")+"\n");
				
			}
			
		}catch(Exception e){
			e.printStackTrace();
			
		}
		
		
	}

	
	
}
