package org.h2t2.setagger.core;

import java.io.IOException;

import org.h2t2.setagger.util.Preprocessor;

public class Tagger {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * Arguments: -p: pre-processing
		 */
		if (args.length < 3 && !"-p".equals(args[0])) {
			System.out.println("Dataset preprocessing\n  Usage: -p <input> <output>");
			return;
		}

		try {
			Preprocessor.process(args[1], args[2]);
		} catch (IOException e) {
			System.out.println("Fail to pre-process the dataset!");
			e.printStackTrace();
		}
	}

}
