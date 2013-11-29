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
	private static PorterStemmerTokenizerFactory psTokenizer = new PorterStemmerTokenizerFactory(
	        new EnglishStopTokenizerFactory(new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE)));

	/**
	 * Pre-process StackExchange dataset.
	 * 
	 * @param input
	 *            A CSV file, each line contains 4 fields (3 fields if the input is the data to be predicted): ID,
	 *            title, body, code, tags.
	 * @param output
	 *            A CSV file, each line contains 5 fields (4 fields if the input is the data to be predicted): ID,
	 *            title, body without code and unnecessary words, code, tags.
	 * @throws IOException
	 */
	public void process(String input, String output) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(output), ',');
		CSVReader reader = new CSVReader(new FileReader(input), ',', '"', '\0', 1);

		String[] record;
		while ((record = reader.readNext()) != null) {
			try {
				record = extractCode(record);
				record = reduceCodeSyntax(record);
				record = removeHtmlTags(record);
				record = getUsefulToken(record);
				writer.writeNext(record);
			} catch (ArrayIndexOutOfBoundsException e) {
				continue;
			}
		}

		writer.close();
		reader.close();
	}

	/**
	 * @param record
	 *            Contain 4 fields (3 fields if the input is the data to be predicted): ID, title, body with code, tags.
	 * @return A String array contain 5 fields (4 fields if the input is the data to be predicted): ID, title, body
	 *         without code, code, tags.
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

		String[] newRecord;
		if (record.length == 3) {
			newRecord = new String[4];
		} else {
			newRecord = new String[5];
		}

		newRecord[0] = record[0];
		newRecord[1] = record[1];
		newRecord[2] = newContent.toString();
		newRecord[3] = codeContent.toString();
		if (record.length == 4) {
			newRecord[4] = record[3];
		}

		return newRecord;
	}

	/**
	 * @param record
	 *            Contain 5 fields : ID, title, body without code, code, tags.
	 * @return A String array contain 5 fields : ID, title, body without HTML tags and codes, one line code with reduced
	 *         syntax , tags.
	 * @author Li-Yuan
	 */
	private String[] reduceCodeSyntax(String[] record) {
		record[3] = record[3].replaceAll("[^a-zA-Z ]", " ");
		return record;
	}

	/**
	 * @param record
	 *            Contain 5 fields (4 fields if the input is the data to be predicted): ID, title, body without code,
	 *            code, tags.
	 * @return A String array contain 5 fields (4 fields if the input is the data to be predicted): ID, title, body
	 *         without HTML tags and codes, code, tags.
	 * @author TL
	 */
	private String[] removeHtmlTags(String[] record) {
		record[2] = HtmlTagHandler.removeHtmlTags(record[2]);
		return record;
	}

	/**
	 * This function removes stop words and applys stemming to the title and body fields.
	 * 
	 * @param record
	 *            Contain 5 fields (4 fields if the input is the data to be predicted): ID, title, body, code, tags.
	 * @return 5 fields (4 fields if the input is the data to be predicted) same as parameter but remove stop words in
	 *         title and body and also do the stemming.
	 * @author Isaac
	 */
	private String[] getUsefulToken(String[] record) {
		String token;
		StringBuilder str = new StringBuilder();
		boolean isFirst = true;

		char[] chars = record[1].toCharArray();
		Tokenizer tokenizer = psTokenizer.tokenizer(chars, 0, chars.length);
		while ((token = tokenizer.nextToken()) != null) {
			if (isFirst) {
				str.append(token);
				isFirst = false;
			} else {
				str.append(" ").append(token);
			}
		}
		record[1] = str.toString();

		str.delete(0, str.length());
		isFirst = true;
		chars = record[2].toCharArray();
		tokenizer = psTokenizer.tokenizer(chars, 0, chars.length);
		while ((token = tokenizer.nextToken()) != null) {
			if (isFirst) {
				str.append(token);
				isFirst = false;
			} else {
				str.append(" ").append(token);
			}
		}
		record[2] = str.toString();

		return record;
	}
}
