package org.h2t2.setagger.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.util.TreeSet;

import au.com.bytecode.opencsv.CSVReader;

public class DocumentTermProcessor {
	public void checkScanner(String input) {
		try {
			//
			CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(new File(input)), "UTF8"));
			
			
			String [] record = null;
			
			while ((record = reader.readNext()) != null) {
//				

				if(record.length != 5){
					System.out.println(record.length);
//					System.out.println(record[0]);
//					System.out.println(record[1]);
					System.out.println(record[0]);
					continue;
					
				}
			}
			
		}catch (Exception e){
			e.printStackTrace();
			
		}
		

	}
	
}
