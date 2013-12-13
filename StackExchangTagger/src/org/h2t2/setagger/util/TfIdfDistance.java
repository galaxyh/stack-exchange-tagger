package org.h2t2.setagger.util;

import java.lang.Math.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;

public class TfIdfDistance implements Serializable {

    static final long serialVersionUID = -5892732237635742347L;

    private int docCount = 0;
    private HashMap<String, Integer> tokenDf = new HashMap<String, Integer>();
    private TreeMap<String, Integer> tmpMap = new TreeMap<String, Integer>();

    public void print() {
        System.out.println("doc: " + docCount + ", term: " + tokenDf.size());
        for(Map.Entry<String, Integer> entry : tokenDf.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    public double idf(String s) {
        Integer df = tokenDf.get(s);

        if(df == null)
            return 0;
        return Math.log(((double)docCount) / ((double)df));

    }

    public void addDoc(HashMap<String, Double> map) {
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

    public void featureExtract() {

    }

    public double proximity(FeatureVector doc1, FeatureVector doc2) {
        double dotProduct = 0;

        for(Map.Entry<String, Double> entry : doc1.vector.entrySet()) {
            Double tfIdf2;
            if(( tfIdf2 = doc2.vector.get(entry.getKey()) ) != null) {
                dotProduct += tfIdf2 * entry.getValue();
            }
        }

        if(doc1.length == 0)
            return (doc2.length == 0)? 1 : 0;
        if(doc2.length == 0)
            return 0;
        return dotProduct / (doc1.length * doc2.length);
    }

    public double proximity(HashMap<String, Double> doc1, HashMap<String, Double> doc2) {
        double len1 = 0, len2 = 0, dotProduct = 0;

        for(Map.Entry<String, Double> entry : doc1.entrySet()) {
            String term = entry.getKey();
            double idf = idf(term);
            double tf1  = entry.getValue();
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
            Double tf2 = doc2.get(term);
            if(tf2 == null)
                continue;
            double tfIdf2 = tf2 * idf;
            dotProduct += Math.sqrt(tfIdf1*tfIdf2);
        }
        for(Map.Entry<String, Double> entry : doc2.entrySet()) {
            String term = entry.getKey();
            double tf2 = entry.getValue();
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
