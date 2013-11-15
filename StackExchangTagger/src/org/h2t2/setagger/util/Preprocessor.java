/**
 * 
 */
package org.h2t2.setagger.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.aliasi.tokenizer.*;

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
			record = getUsefulToken(record);
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

    /**
      This function removes stop words and applys stemming to the title and body fields.
      @param record Contain 5 fields: ID, title, body, code, tags.
      @return 5 fields same as parameter but remove stop words in title and body and also do the stemming.
      @author Isaac
    */
	private static String[] getUsefulToken(String[] record) {
        RegExTokenizerFactory RTF = new RegExTokenizerFactory("(\\w\\S*\\w)|([a-zA-Z])");
        LowerCaseTokenizerFactory LTF = new LowerCaseTokenizerFactory(RTF);
        EnglishStopTokenizerFactory ETF = new EnglishStopTokenizerFactory(LTF);
        PorterStemmerTokenizerFactory PTF = new PorterStemmerTokenizerFactory(ETF);
        String token;

        char [] chars = record[1].toCharArray();
        Tokenizer tokenizer = PTF.tokenizer(chars, 0, chars.length);
        record[1] = "";
        while((token = tokenizer.nextToken()) != null){
            record[1] += token + " ";
        }

        chars = record[2].toCharArray();
        tokenizer = PTF.tokenizer(chars, 0, chars.length);
        record[2] = "";
        while((token = tokenizer.nextToken()) != null){
            record[2] += token + " ";
        }

		return record;
	}
}
