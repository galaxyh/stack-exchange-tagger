package org.h2t2.setagger.core;

import java.io.Serializable;
import java.util.*;

public class Association implements Serializable {

    private int tfInDoc;

    private HashMap<String, Integer> tagToCooccurrence;

    private Double attentionWeight;

    public Association () {
        this(0, new HashMap<String, Integer>(), null);
    }

    public Association (int tfInDoc, HashMap<String, Integer> tagToCooccurrence, Double attentionWeight) {
        this.tfInDoc = tfInDoc;
        this.tagToCooccurrence = tagToCooccurrence;
        this.attentionWeight = attentionWeight;
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

    public void setAttentionWeight (Double attentionWeight) {
        this.attentionWeight = attentionWeight;
    }

    public Double getAttentionWeight () {
        return attentionWeight;
    }

    public HashMap<String, Integer> getTagToCooccurrence () {
        return this.tagToCooccurrence;
    }

    public void setTagToCooccurrence (HashMap<String, Integer> tagToCooccurrence) {
        this.tagToCooccurrence = tagToCooccurrence;
    }

    public int getTfInDoc () {
        return this.tfInDoc;
    }

    public void setTfInDoc (int tfInDoc) {
        this.tfInDoc = tfInDoc;
    }
}