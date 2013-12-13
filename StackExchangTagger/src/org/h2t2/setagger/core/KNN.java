package org.h2t2.setagger.core;

import org.h2t2.setagger.util.KnnClassifier;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import java.util.TreeMap;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.BufferedReader;


public class KNN implements Model {

    private KnnClassifier knn;
    StopWatch stopWatch = new StopWatch();

    @Override
    public void train(String trainFileName, String[] args) {
        try {
            //CSVReader reader = new CSVReader(new FileReader(trainFileName), ',', '"', '\0', 0);
            BufferedReader reader = new BufferedReader(new FileReader(trainFileName));
            knn = new KnnClassifier();

            String s;
            String[] record;
            int cnt = 0;
            while ((s = reader.readLine()) != null) {
                record = s.split(",");
                if(record.length != 5) {
                    System.err.println("BufferedReader error");
                    System.exit(1);
                }

                stopWatch.reset(); // analytic
                stopWatch.start(); // analytic
                knn.train(record);
                stopWatch.stop(); // analytic
                System.out.println("train time " + ++cnt + " : " + stopWatch); // analytic
            }
            knn.endTrain();

            reader.close();
        }
        catch(IOException i) {
            System.out.println("Fail to load predict data or write predict result!");
            i.printStackTrace();
        }
    }

    @Override
    public void predict(String predictFileName, String outputFileName, String[] args) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(outputFileName), ',');
            //CSVReader reader = new CSVReader(new FileReader(predictFileName), ',', '"', '\0', 0);
            BufferedReader reader = new BufferedReader(new FileReader(predictFileName));

            String[] idTags = {"ID", "Tags"};
            writer.writeNext(idTags);
            writer.flush();

            String s;
            String[] record;
            while ((s = reader.readLine()) != null) {
                record = s.split(",");
                if(record.length != 5){ // 5 for test, 4 for real
                    System.err.println("BufferedReader error");
                    System.exit(1);
                }

                stopWatch.reset(); // analytic
                stopWatch.start(); // analytic
                TreeMap<Double, String[]> nearestNeighbor = knn.classify(record);
                stopWatch.stop(); // analytic
                System.out.println("classify time: " + stopWatch); // analytic
                TreeMap<String, Double> tagRank = new TreeMap<String, Double>();
                TreeMap<Double, String> maxTags = new TreeMap<Double, String>();
                for(Map.Entry<Double, String[]> entry : nearestNeighbor.entrySet()) {
                    Double proximity = entry.getKey();
                    Double value;
                    for(String str : entry.getValue()) {
                        if((value = tagRank.get(str)) != null)
                            tagRank.put(str, value + proximity);
                        else
                            tagRank.put(str, proximity);
                    }
                }
                for(Map.Entry<String, Double> entry : tagRank.entrySet()) {
                    maxTags.put(entry.getValue(), entry.getKey());
                }

                Map.Entry<Double, String> tmpEntry;
                idTags = new String[2];
                idTags[0] = record[0];
                idTags[1] = maxTags.pollLastEntry().getValue();
                if((tmpEntry = maxTags.pollLastEntry()) != null) {
                    idTags[1] += " " + tmpEntry.getValue();
                }
                if((tmpEntry = maxTags.pollLastEntry()) != null) {
                    idTags[1] += " " + tmpEntry.getValue();
                }

                writer.writeNext(idTags);
                writer.flush();

                System.out.println("record " + record[0] + " done.");
                System.out.println();
            }

            writer.close();
            reader.close();
        }
        catch(IOException i) {
            System.out.println("Fail to load predict data or write predict result!");
            i.printStackTrace();
        }
    }

    @Override
    public void saveModel(String modelFile) {
        if (knn == null) {
            System.out.println("Warning! Model has not been trained yet. Model not Saved!");
            return;
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(modelFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(knn);
            out.close();
            fileOut.close();
        }
        catch(IOException i) {
            i.printStackTrace();
        }
    }

    @Override
    public void loadModel(String modelFile) {
        try {
            FileInputStream fileIn = new FileInputStream(modelFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            knn = (KnnClassifier)in.readObject();
            in.close();
            fileIn.close();
        }
        catch(IOException i) {
            i.printStackTrace();
        }
        catch(ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
        }
    }
}
