package org.h2t2.setagger.util;

import java.io.Serializable;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

import java.util.StringTokenizer;

public class KnnClassifier implements Serializable {

    static final long serialVersionUID = -6160089638360209536L;

    private int K = 10;
    private TfIdfDistance tfIdf = new TfIdfDistance();
    private Vector<HashMap<String, Integer>> doc = new Vector<HashMap<String, Integer>>();
    private Vector<String[]> tag = new Vector<String[]>();

    public KnnClassifier() {}
    public KnnClassifier(int K) {this.K = K;}

    private HashMap<String, Integer> stringToMap(String s) {
        StringTokenizer tokenizer = new StringTokenizer(s);

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        Integer value;
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if((value = map.get(token)) != null)
                map.put(token, value+1);
            else
                map.put(token, 1);
        }

        return map;
    }

    public void train(String[] record) {
        HashMap<String, Integer> map = stringToMap(record[1] + " " + record[2]);
        doc.add(map);
        tag.add(record[4].split("\\s+"));
        tfIdf.addDoc(map);
    }

    public TreeMap<Double, String[]> classify(String[] record) {
        HashMap<String, Integer> input = stringToMap(record[1] + " " + record[2]);
        TreeMap<Double, String[]> nearestNeighbor = new TreeMap<Double, String[]>(); // should use multimap

        int i;
        int initSize = (K < doc.size()) ? K : doc.size() ;
        for(i = 0;i < initSize;i++) {
            nearestNeighbor.put(tfIdf.proximity(doc.elementAt(i), input), tag.elementAt(i));
        }
        for(;i < doc.size();i++) {
            double proximity = tfIdf.proximity(doc.elementAt(i), input);
            if(proximity > nearestNeighbor.firstKey()) {
                nearestNeighbor.pollFirstEntry();
                nearestNeighbor.put(proximity, tag.elementAt(i));
            }
        }

        return nearestNeighbor;
    }
}
