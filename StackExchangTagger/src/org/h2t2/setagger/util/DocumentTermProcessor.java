package org.h2t2.setagger.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;

import au.com.bytecode.opencsv.CSVReader;

public class DocumentTermProcessor {
	public void checkOpenCSV(String input) {
		try {
			BufferedReader lineReader = new BufferedReader(new FileReader(input));
			//CSVReader reader = new CSVReader(new FileReader(input), ',', '"', '\0');
			
			
			String [] record = null;
			String line = null;
			while ((line = lineReader.readLine()) != null) {
				record = new CSVReader(new FileReader(line), ',', '"', '\0').readNext();
				if(record.length != 4){
					System.out.println(line);
					System.exit(-1);
					
				}
			}
			
		}catch (Exception e){
			e.printStackTrace();
			
		}
		

	}
	
}
