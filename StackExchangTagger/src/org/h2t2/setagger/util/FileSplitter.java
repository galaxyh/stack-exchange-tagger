package org.h2t2.setagger.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileSplitter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(args[0]));
			BufferedWriter writer = new BufferedWriter(new FileWriter(args[1] + "_1.csv"));
			int rowsPerFile = Integer.parseInt(args[2]);
			int curCount = 0;
			int sequence = 1;

			String line;
			while ((line = reader.readLine()) != null) {
				curCount++;
				if (curCount % rowsPerFile == 0) {
					writer.close();
					sequence++;
					System.out.println("Part " + (sequence - 1) + " Done.");
					writer = new BufferedWriter(new FileWriter(args[1] + "_" + sequence + ".csv"));
				}

				writer.write(line);
				writer.write("\n");
			}
			reader.close();
			System.out.println("All Done.");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
