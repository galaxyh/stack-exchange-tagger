package org.h2t2.setagger.util;

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
	
	private int countOccurence(String source, String regex){
		Pattern pattern = Pattern.compile(regex);
        Matcher  matcher = pattern.matcher(source);
        int count = 0;
        while(matcher.find()){
        	count++;
        }
        return count;

	}
	
	
	public void scan(String input, String output) {
		
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
