/**
 * 
 */
package org.h2t2.setagger.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * StackExchange dataset preprocessor
 * 
 */
public class Preprocessor {
	/**
	 * Pattern matching regular expression for extracting code.
	 */
	private static final Pattern codePattern = Pattern.compile("(?is)<code>(.*?)</code>");

	/**
	 * Pre-process StackExchange dataset.
	 * 
	 * @param input
	 * @param output
	 * @throws IOException
	 */
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

	/**
	 * @param record
	 * @return A String array contains: Line number, Title, Body without code, Code, Tags
	 */
	private static String[] extractCode(String[] record) {
		StringBuffer newContent = new StringBuffer();
		StringBuffer codeContent = new StringBuffer();

		Matcher matcher = codePattern.matcher(record[2]);
		while (matcher.find()) {
			String codeString = matcher.group(1);
			codeContent.append(codeString + " ");
			if (codeString != null) {
				matcher.appendReplacement(newContent, " ");
			}
		}
		matcher.appendTail(newContent);

		String[] newRecord = new String[5];
		newRecord[0] = record[0];
		newRecord[1] = record[1];
		newRecord[2] = newContent.toString();
		newRecord[3] = codeContent.toString();
		newRecord[4] = record[3];
		return newRecord;
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
