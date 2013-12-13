package org.h2t2.setagger.util;

import java.util.regex.Pattern;

import com.aliasi.tokenizer.*;
import com.aliasi.spell.TfIdfDistance;

public class tfIdfHandler {
    //private static final Pattern tokenPattern = Pattern.compile("(\\w\\S*\\w)|([a-zA-Z])");
    /*private static TfIdfDistance tfIdf = new TfIdfDistance( new PorterStemmerTokenizerFactory(
                                                            new EnglishStopTokenizerFactory(
                                                            new LowerCaseTokenizerFactory(
                                                            new RegExTokenizerFactory( tokenPattern )))));*/

    private static TfIdfDistance tfIdf = new TfIdfDistance( 
                                         new PorterStemmerTokenizerFactory(
                                         new EnglishStopTokenizerFactory(
                                         new LowerCaseTokenizerFactory( IndoEuropeanTokenizerFactory.INSTANCE ))));

    public static void addDoc(String[] doc){
        tfIdf.handle( doc[1]    // title
                      + " " 
                      + doc[2]  // body
                    );
    }

    public static void print(){
        System.out.println("Documents: " + tfIdf.numDocuments() + ", Terms: " + tfIdf.numTerms());
        System.out.printf("  %18s  %8s  %8s\n", "Term", "Doc Freq", "IDF");
        for (String term : tfIdf.termSet())
            System.out.printf("  %18s  %8d  %8.2f\n", term, tfIdf.docFrequency(term), tfIdf.idf(term));
    }
}
