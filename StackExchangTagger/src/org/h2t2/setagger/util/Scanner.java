package org.h2t2.setagger.util;

import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Scanner {
	private BufferedReader reader;
	
	private static int countOccurence(String source, String regex){
		Pattern pattern = Pattern.compile(regex);
        Matcher  matcher = pattern.matcher(source);
        int count = 0;
        while(matcher.find()){
        	count++;
        }
        return count;

	}
	
	private String trimDoubleQuote(String input){
		int start = 0;
		while(start < input.length() &&  input.charAt(start) == '"')start++;
		
		int end = input.length() -1;
		while(end >= 0 && input.charAt(end) == '"')end--;
		
		if(start < end)return input.substring(start, end);
		return "";
		
	}
	
	
	public String [] readNext() throws IOException{
		String line = reader.readLine();
		if(line == null)return null;
		String [] record  = line.split("(?<!\")\",\"(?!\")|\"\",\"(?!\")|(?<!\")\",\"\"");
		for(int i = 0;i < record.length;i++)record[i] = trimDoubleQuote(record[i]);
		return record;
	}
	
	public void close() throws IOException{
		if(reader != null)reader.close();
	} 
	
	// the input reader after scan processing
	public Scanner(Reader r){
		reader = new BufferedReader(r);
				
	}
	
	
	public static void scan(String input, String output) {
		
		try {
			String line = null;
			BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input)), "UTF8"));
			BufferedWriter wout = new BufferedWriter(new FileWriter(output));
			line = bf.readLine();// read in "Id","Title","Body","Tags"
			
			String record = "";
			while((line = bf.readLine()) != null){
				if( countOccurence(line, "^\"[0-9]+\",\"") > 0){
					if(!record.equals(""))wout.write(record.trim()+"\n");
					record = new String(line);
					
				}else {
					record += (" " + line);
				}
			}
			wout.write(record.trim()+"\n");
			wout.flush();


		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}

	
	
}
