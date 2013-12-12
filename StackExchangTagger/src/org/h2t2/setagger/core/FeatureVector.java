package org.h2t2.setagger.core;

import org.h2t2.setagger.util.TfIdfDistance;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import java.lang.Math.*;

public class FeatureVector implements Serializable {

    public HashMap<String, Double> vector;
    public String[] tag;
    public double length = 0;

    public void refine(TfIdfDistance tfIdf) {
        for(Map.Entry<String, Double> entry : vector.entrySet()) {
            String key = entry.getKey();
            double tmp = tfIdf.idf(key);
            if(tmp == 0) {
                vector.remove(key);
                continue;
            }
            tmp = Math.sqrt(entry.getValue() * tmp);

            entry.setValue(tmp);
            length += tmp*tmp;
        }
        length = Math.sqrt(length);
    }
}
