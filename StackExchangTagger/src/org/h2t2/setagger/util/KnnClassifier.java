package org.h2t2.setagger.util;

import java.io.Serializable;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.StringTokenizer;

public class KnnClassifier implements Serializable {

    static final long serialVersionUID = -6160089638360209536L;

    private int K = 10;
    private TfIdfDistance tfIdf = new TfIdfDistance();
    private ArrayList<ArrayList<String>> tag = new ArrayList<ArrayList<String>>();
    private ArrayList<HashMap<String, Double>> doc = new ArrayList<HashMap<String, Double>>();

    public KnnClassifier() {}
    public KnnClassifier(int K) {this.K = K;}

    private HashMap<String, Double> stringToMapRefine(String s) {
        StringTokenizer tokenizer = new StringTokenizer(s);

        HashMap<String, Double> map = new HashMap<String, Double>();
        Double value;
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if(tfIdf.idf(token) == 0)
                continue;
            if((value = map.get(token)) != null)
                map.put(token, value+1);
            else
                map.put(token, 1.0);
        }

        double length = 0;
        double tmp;
        for(Map.Entry<String, Double> entry : map.entrySet()) {
            tmp = tfIdf.idf(entry.getKey());
            tmp = Math.sqrt(entry.getValue() * tmp);
            entry.setValue(tmp);
            length += tmp*tmp;
        }
        length = Math.sqrt(length);
        for(Map.Entry<String, Double> entry : map.entrySet()) {
            entry.setValue(entry.getValue()/length);
        }

        return map;
    }

    private HashMap<String, Double> stringToMap(String s) {
        StringTokenizer tokenizer = new StringTokenizer(s);

        HashMap<String, Double> map = new HashMap<String, Double>();
        Double value;
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if((value = map.get(token)) != null)
                map.put(token, value+1);
            else
                map.put(token, 1.0);
        }

        return map;
    }

    public void trainTfIdf(String[] record) {
        HashMap<String, Double> map = stringToMap(record[1] + " " + record[2]);
        tfIdf.addDoc(map);
    }

    public void endTrainTfIdf() {
        tfIdf.featureExtract(80);
    }

    public void trainFeatureVector(String[] record) {
        HashMap<String, Double> map = stringToMapRefine(record[1] + " " + record[2]);
        doc.add(map);
        tag.add(new ArrayList<String>(Arrays.asList(record[4].split("\\s+"))));
    }

    /*public void train(String[] record) {
        HashMap<String, Double> map = stringToMap(record[1] + " " + record[2]);
        doc.add(new FeatureVector(map));
        tag.add(new ArrayList<String>(Arrays.asList(record[4].split("\\s+"))));
        tfIdf.addDoc(map);
    }*/

    /*public void endTrain() {
        tfIdf.featureExtract(80);
        for(FeatureVector fv : doc) {
            fv.refine(tfIdf);
        }
    }*/

    public TreeMap<Double, ArrayList<String>> classify(String[] record) {
        HashMap<String, Double> input = stringToMapRefine(record[1] + " " + record[2]);
        TreeMap<Double, ArrayList<String>> nearestNeighbor = new TreeMap<Double, ArrayList<String>>();

        int i;
        int initSize = (K < doc.size()) ? K : doc.size() ;
        ArrayList<String> oldTags;
        for(i = 0;i < initSize;i++) {
            double proximity = tfIdf.proximity(doc.get(i), input);
            if((oldTags = nearestNeighbor.get(proximity)) != null) {
                oldTags.addAll(tag.get(i));
            }
            else {
                nearestNeighbor.put(proximity, tag.get(i));
            }
        }
        for(;i < doc.size();i++) {
            double proximity = tfIdf.proximity(doc.get(i), input);
            if(proximity > nearestNeighbor.firstKey()) {
                nearestNeighbor.pollFirstEntry();
                if((oldTags = nearestNeighbor.get(proximity)) != null) {
                    oldTags.addAll(tag.get(i));
                }
                else {
                    nearestNeighbor.put(proximity, tag.get(i));
                }
            }
        }

        return nearestNeighbor;
    }
}
