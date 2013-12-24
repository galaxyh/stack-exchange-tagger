package org.h2t2.setagger.core;

import java.io.Serializable;
import java.util.*;

public class CBTrainModel implements Serializable {

    private ArrayList<HashMap<String, Integer>> termMapping;

    private HashMap<String, Integer> tagToDocumentFrequency;

    private Long numOfDocuments;

    private HashSet<String> allTagsSet;

    public CBTrainModel () {
        this(null, null, 0L, null);
    }

    public CBTrainModel (ArrayList<HashMap<String, Integer>> termMapping, HashMap<String, Integer> tagToDocumentFrequency, Long numOfDocuments, HashSet<String> allTagsSet) {
        this.termMapping = termMapping;
        this.tagToDocumentFrequency = tagToDocumentFrequency;
        this.numOfDocuments = numOfDocuments;
        this.allTagsSet = allTagsSet;
    }

    public ArrayList<HashMap<String, Integer>> getTermMapping() {
        return this.termMapping;
    }

    public void setTermMapping(ArrayList<HashMap<String, Integer>> termMapping) {
        this.termMapping = termMapping;
    }

    public HashMap<String, Integer> getTagToDocumentFrequency() {
        return this.tagToDocumentFrequency;
    }

    public void setTagToDocumentFrequency(HashMap<String, Integer> tagToDocumentFrequency) {
        this.tagToDocumentFrequency = tagToDocumentFrequency;
    }

    public Long getNumOfDocuments() {
        return this.numOfDocuments;
    }

    public void setNumOfDocuments(Long numOfDocuments) {
        this.numOfDocuments = numOfDocuments;
    }

    public HashSet<String> getAllTagsSet() {
        return this.allTagsSet;
    }

    public void setAllTagsSet(HashSet<String> allTagsSet) {
        this.allTagsSet = allTagsSet;
    }

    public static void writeToFile (String file, CBTrainModel model) {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        out.writeObject(model);
        out.close();
    }

    public static CBTrainModel readFromFile (String file) {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        CBTrainModel model = (CBTrainModel) in.readObject();
        in.close();
        return model;
    }

    public Association getAssociation (int i, String term) {
        return this.termMapping.get(i).get(term);
    }

    public double getStrengthAssociation (int index, String term, String tag) {
        double probabilityOfTagOverTerm = this.termMapping.get(index).get(term).getProbabilityOfTagOverTerm(tag);
        if (probabilityOfTagOverTerm != 0.0)
            return Math.log(probabilityOfTagOverTerm / ((double) this.tagToDocumentFrequency.get(tag) / this.numberOfDocuments));
        return 0.0;
    }

    public double getTagIdf (String tag) {
        Integer frequency;
        if ((frequency = this.tagToDocumentFrequency.get(tag)) != null)
            return (double) frequency / this.numberOfDocuments;
        else
            return 0.0;
    }

}
