package org.h2t2.setagger.util;

import java.io.Serializable;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

import com.aliasi.tokenizer.*;

public class KnnClassifier implements Serializable {

    static final long serialVersionUID = -6160089638360209536L;

    private static LowerCaseTokenizerFactory lctf = new LowerCaseTokenizerFactory( IndoEuropeanTokenizerFactory.INSTANCE );
    private static EnglishStopTokenizerFactory estf = new EnglishStopTokenizerFactory(lctf);
    private static PorterStemmerTokenizerFactory pstf = new PorterStemmerTokenizerFactory(estf);
    private int K = 10;
    private TfIdfDistance tfIdf = new TfIdfDistance();
    private Vector<HashMap<String, Integer>> doc = new Vector<HashMap<String, Integer>>();
    private Vector<String[]> tag = new Vector<String[]>();

    public KnnClassifier() {}
    public KnnClassifier(int K) {this.K = K;}

    private HashMap<String, Integer> stringToMap(String s) {
        char[] chars = s.toCharArray();
        Tokenizer tokenizer = pstf.tokenizer(chars, 0, chars.length);

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        Integer value;
        for(String token : tokenizer) {
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
