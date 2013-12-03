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
			CSVReader reader = new CSVReader(new FileReader(input), ',', '"', '\0');
			
			
			String [] record = null;		
			while ((record = reader.readNext()) != null) {
				if(record.length != 4){
					System.out.println(record[0]);
					System.exit(-1);
					
				}
			}
			
		}catch (Exception e){
			e.printStackTrace();
			
		}
		

	}
	
}
