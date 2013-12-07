package org.h2t2.setagger.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class DivideTrainingSet {

    private static final int RECORDS_SIZE = 6034195;
    private static final double RATIO = 0.75;
    private static final int TRAIN_SIZE = (int)(RECORDS_SIZE*RATIO);

    public static void main(String[] args) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(args[0]), ',', '"', '\0', 1);
        CSVWriter writer = new CSVWriter(new FileWriter(args[1]), ',');

        String[] record;
        int cnt;
        for(cnt = 0; cnt < TRAIN_SIZE && (record = reader.readNext()) != null; cnt++) {
            if(record.length != 4){
                System.err.println("csv read error");
                System.exit(1);
            }
            writer.writeNext(record);
        }
        writer.close();

        writer = new CSVWriter(new FileWriter(args[2]), ',');
        for(; cnt < RECORDS_SIZE && (record = reader.readNext()) != null; cnt++) {
            if(record.length != 4){
                System.err.println("csv read error");
                System.exit(1);
            }
            writer.writeNext(record);
        }
        writer.close();
    }
}
