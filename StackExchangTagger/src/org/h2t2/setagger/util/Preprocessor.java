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

import com.aliasi.tokenizer.*;

/**
 * StackExchange dataset preprocessor
 * 
 */
public class Preprocessor {
	/**
	 * Pattern matching regular expression for extracting code.
	 */
	private static final Pattern codePattern = Pattern.compile("(?is)<code>(.*?)</code>");
	private static PorterStemmerTokenizerFactory psTokenizer;

	public Preprocessor() {
		// Initialize psTokenizer for this.getUsefulToken(String[] record).
		RegExTokenizerFactory rtf = new RegExTokenizerFactory("(\\w\\S*\\w)|([a-zA-Z])");
		LowerCaseTokenizerFactory ltf = new LowerCaseTokenizerFactory(rtf);
		EnglishStopTokenizerFactory etf = new EnglishStopTokenizerFactory(ltf);
		psTokenizer = new PorterStemmerTokenizerFactory(etf);
	}

	/**
	 * Pre-process StackExchange dataset.
	 * 
	 * @param input
	 *            A CSV file, each line contains 4 fields: ID, title, body, code, tags.
	 * @param output
	 *            A CSV file, each line contains 5 fields: ID, title, body without code and unnecessary words, code,
	 *            tags.
	 * @throws IOException
	 */
	public void process(String input, String output) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(input));
		CSVWriter writer = new CSVWriter(new FileWriter(output), ',');

		String[] record;
		while ((record = reader.readNext()) != null) {
			record = extractCode(record);
			record = removeHtmlTag(record);
			record = getUsefulToken(record);
			writer.writeNext(record);
		}

		writer.close();
		reader.close();
	}

	/**
	 * @param record
	 *            Contain 4 fields: ID, title, body with code, tags.
	 * @return A String array contain 5 fields: ID, title, body without code, code, tags.
	 * @author Yu-chun Huang
	 */
	private String[] extractCode(String[] record) {
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

	private String[] removeHtmlTag(String[] record) {
		return record;
	}

	/**
	 * This function removes stop words and applys stemming to the title and body fields.
	 * 
	 * @param record
	 *            Contain 5 fields: ID, title, body, code, tags.
	 * @return 5 fields same as parameter but remove stop words in title and body and also do the stemming.
	 * @author Isaac
	 */
	private String[] getUsefulToken(String[] record) {
		String token;

		char[] chars = record[1].toCharArray();
		Tokenizer tokenizer = psTokenizer.tokenizer(chars, 0, chars.length);
		record[1] = "";
		while ((token = tokenizer.nextToken()) != null) {
			record[1] += token + " ";
		}

		chars = record[2].toCharArray();
		tokenizer = psTokenizer.tokenizer(chars, 0, chars.length);
		record[2] = "";
		while ((token = tokenizer.nextToken()) != null) {
			record[2] += token + " ";
		}

		return record;
	}
}
