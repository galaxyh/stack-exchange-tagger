package org.h2t2.setagger.util;

import java.lang.Math.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TfIdfDistance implements Serializable {

    static final long serialVersionUID = -5892732237635742347L;

    private int docCount = 0;
    private HashMap<String, Integer> tokenDf = new HashMap<String, Integer>();

    public double idf(String s) {
        Integer df = tokenDf.get(s);

        if(df == null)
            return 0;
        return Math.log(((double)docCount) / ((double)df));

    }

    public void addDoc(HashMap<String, Integer> map) {
        Integer value;
        for(String token : map.keySet()) {
           if((value = tokenDf.get(token)) != null) {
               tokenDf.put(token, value+1);
           }
           else {
               tokenDf.put(token, 1);
           }
        }

        docCount++;
    }

    public double proximity(HashMap<String, Integer> doc1, HashMap<String, Integer> doc2) {
        double len1 = 0, len2 = 0, dotProduct = 0;

        for(Map.Entry<String, Integer> entry : doc1.entrySet()) {
            String term = entry.getKey();
            double idf = idf(term);
            Integer tf1  = entry.getValue();
            /* logical view */
            // double tfIdf1 = Math.sqrt(tf1*idf);
            // len1 += tfIdf1*tfIdf1;
            // Integer tf2 = doc2.get(term);
            // if(tf2 == null)
                // continue;
            // double tfIdf2 = Math.sqrt(tf2*idf);
            // dotProduct += tfIdf1*tfIdf2;

            double tfIdf1 = tf1 * idf;
            len1 += tfIdf1;
            Integer tf2 = doc2.get(term);
            if(tf2 == null)
                continue;
            double tfIdf2 = tf2 * idf;
            dotProduct += Math.sqrt(tfIdf1*tfIdf2);
        }
        for(Map.Entry<String, Integer> entry : doc2.entrySet()) {
            String term = entry.getKey();
            Integer tf2 = entry.getValue();
            /* logical view */
            // double tfIdf2 = Math.sqrt(tf2*idf(term));
            // len2 += tfIdf2*tfIdf2;
            len2 += tf2*idf(term);
        }

        if(len1 == 0)
            return (len2 == 0)? 1 : 0;
        if(len2 == 0)
            return 0;
        return dotProduct / Math.sqrt(len1 * len2);
    }
}
