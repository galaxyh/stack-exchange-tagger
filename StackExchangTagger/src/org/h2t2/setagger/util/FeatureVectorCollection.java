package org.h2t2.setagger.util;

import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;

import java.io.Serializable;

import com.aliasi.tokenizer.*;

public class FeatureVectorCollection implements Iterable<HashMap<String, Integer>>, Serializable {

    static final long serialVersionUID = -4973250896015490723L;

    private static LowerCaseTokenizerFactory lctf = new LowerCaseTokenizerFactory( IndoEuropeanTokenizerFactory.INSTANCE );
    private static EnglishStopTokenizerFactory estf = new EnglishStopTokenizerFactory(lctf);
    private static PorterStemmerTokenizerFactory pstf = new PorterStemmerTokenizerFactory(estf);
    private Vector<HashMap<String, Integer>> data;

    public FeatureVectorCollection() {
        data = new Vector<HashMap<String, Integer>>();
    }
    public FeatureVectorCollection(int capacity) {
        data = new Vector<HashMap<String, Integer>>(capacity);
    }

    public static HashMap<String, Integer> stringToMap(String s) {
        char[] chars = s.toCharArray();
        Tokenizer tokenizer = pstf.tokenizer(chars, 0, chars.length);

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        Integer value;
        for(String token : tokenizer) {
            if((value = map.get(token)) != null) {
                map.put(token, value+1);
            }
            else {
                map.put(token, 1);
            }
        }

        return map;
    }

    public boolean add(HashMap<String, Integer> m) {
        return data.add(m);
    }
    public boolean add(String s) {
        return data.add(stringToMap(s));
    }

    public Iterator<HashMap<String, Integer>> iterator() {
        return data.iterator();
    }
}
