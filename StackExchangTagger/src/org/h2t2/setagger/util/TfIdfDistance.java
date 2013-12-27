package org.h2t2.setagger.util;

import java.lang.Math.*;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import java.util.Iterator;

public class TfIdfDistance implements Serializable {

    static final long serialVersionUID = -5892732237635742347L;

    private int docCount = 0;
    private HashMap<String, Pair> tokenIdf = new HashMap<String, Pair>();

    public double idf(String s) {
        Pair pair = tokenIdf.get(s);
        if(pair == null)
            return 0;

        return pair.getFirst();
    }

    public int serialNumber(String s) {
        Pair pair = tokenIdf.get(s);
        if(pair == null)
            return -1;

        return pair.getSecond();
    }

    public void featureExtract(int lowest) {
        for(Iterator<Map.Entry<String, Pair>> it = tokenIdf.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Pair> entry = it.next();
            Pair pair = entry.getValue();
            if(pair.getFirst() <= lowest) {
                it.remove();
            }
            else {
                pair.setFirst(Math.log(docCount / pair.getFirst()));
            }
        }
    }

    public void generateSerialNumber() {
        int i = 0;
        for(Map.Entry<String, Pair> entry : tokenIdf.entrySet()) {
            entry.getValue().setSecond(i);
            i++;
        }
    }

    public void addDoc(HashMap<String, Double> map) {
        Pair pair;
        for(String token : map.keySet()) {
           if((pair = tokenIdf.get(token)) != null) {
               pair.setFirst(pair.getFirst()+1);
           }
           else {
               tokenIdf.put(token, new Pair(1.0, null));
           }
        }

        docCount++;
    }

    public double proximity(HashMap<Integer, Double> doc1, HashMap<Integer, Double> doc2) {
        double proximity = 0;

        if(doc1.size() == 0 && doc2.size() == 0)
            return 1.0;

        for(Map.Entry<Integer, Double> entry : doc1.entrySet()) {
            Double value;
            if((value = doc2.get(entry.getKey())) != null) {
                proximity += value * entry.getValue();
            }
        }

        return proximity;
    }

    private class Pair {
        private Double first;
        private Integer second;

        public Pair() {}
        public Pair(Double f, Integer s) {first = f; second = s;}

        public double getFirst() {return first;}
        public int getSecond() {return second;}
        public void setFirst(Double f) {first = f;}
        public void setSecond(Integer s) {second = s;}
    }
}
