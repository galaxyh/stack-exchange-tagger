package org.h2t2.setagger.util;

import java.lang.Math.*;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import java.util.Iterator;

public class TfIdfDistance implements Serializable {

    static final long serialVersionUID = -5892732237635742347L;

    private int docCount = 0;
    private HashMap<String, Double> tokenIdf = new HashMap<String, Double>();

    public void print() {
        System.out.println("doc: " + docCount + ", term: " + tokenIdf.size());
        for(Map.Entry<String, Double> entry : tokenIdf.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    public double idf(String s) {
        Double idf = tokenIdf.get(s);

        if(idf == null)
            return 0;
        return idf;
    }

    public void featureExtract(int lowest) {
        for(Iterator<Map.Entry<String, Double>> it = tokenIdf.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Double> entry = it.next();
            if(entry.getValue() <= lowest) {
                it.remove();
            }
            else {
                entry.setValue(Math.log(docCount / entry.getValue()));
            }
        }
    }

    public void addDoc(HashMap<String, Double> map) {
        Double value;
        for(String token : map.keySet()) {
           if((value = tokenIdf.get(token)) != null) {
               tokenIdf.put(token, value+1);
           }
           else {
               tokenIdf.put(token, 1.0);
           }
        }

        docCount++;
    }

    public double proximity(HashMap<String, Double> doc1, HashMap<String, Double> doc2) {
        double proximity = 0;

        if(doc1.size() == 0 && doc2.size() == 0)
            return 1.0;

        for(Map.Entry<String, Double> entry : doc1.entrySet()) {
            Double value;
            if((value = doc2.get(entry.getKey())) != null) {
                proximity += value * entry.getValue();
            }
        }

        return proximity;
    }

    /*public double proximity(FeatureVector doc1, FeatureVector doc2) {
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
    }*/

    /*public double proximity(HashMap<String, Double> doc1, HashMap<String, Double> doc2) {
        double len1 = 0, len2 = 0, dotProduct = 0;

        for(Map.Entry<String, Double> entry : doc1.entrySet()) {
            String term = entry.getKey();
            double idf = idf(term);
            double tf1  = entry.getValue();
            [> logical view <]
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
            [> logical view <]
            // double tfIdf2 = Math.sqrt(tf2*idf(term));
            // len2 += tfIdf2*tfIdf2;
            len2 += tf2*idf(term);
        }

        if(len1 == 0)
            return (len2 == 0)? 1 : 0;
        if(len2 == 0)
            return 0;
        return dotProduct / Math.sqrt(len1 * len2);
    }*/
}
