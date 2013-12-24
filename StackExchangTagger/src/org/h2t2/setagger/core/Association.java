package org.h2t2.setagger.core;

import java.util.*;

public class Association {
    public int tfInDoc = 0;
    public HashMap<String, Integer> tagToCooccurrence;
    public Double attentionWeight;
    public Association () {
        this.tagToCooccurrence = new HashMap<String, Integer>();
    }

    public double getProbabilityOfTagOverTerm (String tag) {
        if (tagToCooccurrence.get(tag) == null) {
            return 0.0;
        }
        return (double) tagToCooccurrence.get(tag) / tfInDoc;
    }

    // attentionWeight will represent entropy at an early stage
    public double getEntropy () {
        if (this.attentionWeight == null) {
            this.attentionWeight = 0.0;
            for (String tag : tagToCooccurrence.keySet()) {
                double probabilityOfTagOverTerm = getProbabilityOfTagOverTerm(tag);
                this.attentionWeight -= probabilityOfTagOverTerm * Math.log(probabilityOfTagOverTerm);
            }
        }
        return this.attentionWeight;
    }

    // attentionWeight will represent scaledEntropy at an early stage
    public void setScaledEntropy (double scaledEntropy) {
        this.attentionWeight = scaledEntropy;
    }

    public double getScaledEntropy () {
        return this.attentionWeight;
    }

    public void setAttentionWeight (double attentionWeight) {
        this.attentionWeight = attentionWeight;
    }

    public double getAttentionWeight () {
        return attentionWeight;
    }
}