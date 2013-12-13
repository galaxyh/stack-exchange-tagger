package org.h2t2.setagger.util;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import java.lang.Math.*;

public class FeatureVector implements Serializable {

    static final long serialVersionUID = -7393893773241082578L;

    public HashMap<String, Double> vector;
    public double length = 0;

    public FeatureVector(HashMap<String, Double> v) {
        vector = v;
    }

    public void refine(TfIdfDistance tfIdf) {
        for(Iterator<Map.Entry<String, Double>> it = vector.entrySet().iterator();it.hasNext();) {
            Map.Entry<String, Double> entry = it.next();
            String key = entry.getKey();
            double tmp = tfIdf.idf(key);
            if(tmp == 0) {
                it.remove();
                continue;
            }
            tmp = Math.sqrt(entry.getValue() * tmp);

            entry.setValue(tmp);
            length += tmp*tmp;
        }
        length = Math.sqrt(length);
    }
}
