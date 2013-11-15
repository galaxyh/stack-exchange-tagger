/**
 * 
 */
package org.h2t2.setagger.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * StackExchange dataset preprocessor
 * 
 */
public class Preprocessor {

	public static void process(String input, String output) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(input));
		CSVWriter writer = new CSVWriter(new FileWriter(output), ',');

		String[] record;
		while ((record = reader.readNext()) != null) {
			record = extractCode(record);
			record = removeHtmlTag(record);
			record = removeStopWords(record);
			record = lemmatize(record);
			writer.writeNext(record);
		}

		writer.close();
		reader.close();
	}

	private static String[] extractCode(String[] record) {
		return record;
	}

	private static String[] removeHtmlTag(String[] record) {
		return record;
	}

	private static String[] removeStopWords(String[] record) {
		return record;
	}

	private static String[] lemmatize(String[] record) {
		return record;
	}
}
