package com.longlab.sumstats;

import com.univocity.parsers.tsv.*;
import com.univocity.parsers.common.processor.RowListProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class SeqDivergence {
    private Map<String, Integer> scaffoldSizes;
    private Map<String, List<String[]>> scaffoldData;
    private Map<String, List<String[]>> outputs;

    private TsvParser parser;

    public SeqDivergence() {
        this.scaffoldSizes = new HashMap<String,Integer>();
        this.scaffoldData = new HashMap<String, List<String[]>>();
        this.outputs = new HashMap<String,List<String[]>>();

        initParser();

    }

    // Parser from Univocity
    private void initParser() {
        TsvParserSettings parserSettings = new TsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        RowListProcessor rowProcessor = new RowListProcessor();
        parserSettings.setProcessor(rowProcessor);

        this.parser = new TsvParser(parserSettings);

    }

    // Reading a file and returning as a List of String arrays
    private List<String[]> readFile(String filePath) {
        List<String[]> newFile = new ArrayList<String[]>();

        try {
            newFile = this.parser.parseAll(new FileReader(filePath));

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }

        return newFile;

    }

    /*
        Reading the scaffold sizes file into a HashMap (String to Integer)
        2 columns in the input file:
        - Scaffold name (String)
        - Scaffold size in bp (Integer)
    */
    private void setScaffSizes(String scaffSizesFilePath) {
        List<String[]> scaffSizes = readFile(scaffSizesFilePath);

        for (String[] row : scaffSizes) {
            this.scaffoldSizes.put(row[0], Integer.parseInt(row[1]));

        }

    }

    /*
        Map scaffold name to alternate allele frequencies (String -> List of String arrays)
        row[0] contains scaffold and position in this format: scaffoldName_position
    */
    private void setScaffData(String scaffDataFilePath) {
        List<String[]> scaffData = readFile(scaffDataFilePath);
        String scaffold = "";

        for (String[] row : scaffData) {
            String currScaff = row[0].split("_")[0];
            String[] newEntry = {row[0], row[25]};

            if (currScaff.equals(scaffold)) {
                this.scaffoldData.get(scaffold).add(newEntry);

            } else if (this.scaffoldSizes.containsKey(currScaff)) {
                List<String[]> val = new ArrayList<String[]>();
                val.add(newEntry);

                this.scaffoldData.put(currScaff, val);
                scaffold = currScaff;

            }

        }

    }

    /*
        Maps scaffold name (String) to List of String arrays containing:
        - Bin (midpoint, e.g. 0-1000 would be labeled as 500)
        - Pi

        This method processes a single scaffold
    */
    private void processScaffold(String scaffold, List<String[]> scaffData, int scaffSize) {
        List<String[]> val = new ArrayList<String[]>();
        int fullBins = scaffSize / 1000; // Int so no remainders
        int binMax = 1000; // probably should rename to binUpperBound

        /*
            pi = 2 * (varSites / 1000) * (temp_1 + temp_2 ... + temp_n)

            temp_i = (# alts * # refs) / (24 * 23)
        */

        int varSites = 0; // number of variant sites within bin
        double tempSum = 0;
        int binIdx = 0;

        while (binIdx < fullBins) {
            for (String[] row : scaffData) {
                int pos = Integer.parseInt(row[0].split("_")[1]);

                if (pos <= binMax) { // within current bin
                    varSites++;

                } else { // move to next bin
                    binIdx++; // update bin index

                     // calculate pi for previous bin
                    double pi = 2 * (double)varSites / 1000 * tempSum;

                    // MapEntry value update for previous bin
                    String[] newEntry = {Integer.toString(binMax - 500), Double.toString(pi)};
                    val.add(newEntry);

                    binMax += 1000; // update binUpperBound for new bin
                    varSites = 1; // current variant site is part of new bin
                    tempSum = 0;

                }

                // update tempSum with current variant site
                double snps = Double.parseDouble(row[1]) * 24; // allele frequency * 24 = number of
                                                               // cultivars with this variant
                double temp = (snps * (24 - snps)) / (24 * 23);
                tempSum += temp;

            }

        }

        // For the final bin
        double pi = 2 * (double)varSites / 1000 * tempSum;
        String[] newEntry = {Integer.toString(binMax - 500), Double.toString(pi)};
        val.add(newEntry);
        this.outputs.put(scaffold, val); // Map

    }

    // Calculate bins and pi for every scaffold
    private void processAllScaffs() {
        Iterator<Map.Entry<String, List<String[]>>> iter = this.scaffoldData.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, List<String[]>> entry = iter.next();
            String scaffold = entry.getKey();
            List<String[]> scaffData = entry.getValue();
            int scaffSize = this.scaffoldSizes.get(scaffold);

            processScaffold(scaffold, scaffData, scaffSize);

        }

    }

    // Write a single output
    private void writeOutput(String outputFilePath, String scaffold) {
        TsvWriter writer = null;

        try {
            writer = new TsvWriter(new FileWriter(outputFilePath), new TsvWriterSettings());

        } catch (IOException e) {
            e.printStackTrace();

        }

        writer.writeHeaders("#Bin", "Pi");
        for (String[] row : this.outputs.get(scaffold)) {
            writer.writeRow(row);

        }

        writer.flush();
        writer.close();

    }

    public void calcSeqDivergence(String sizesFile, String dataFile) {
        String folderPath = "";
        String[] split = sizesFile.split("/");
        for (int i = 0; i < split.length - 1; i++) {
            folderPath += split[i] + "/";

        }

        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        boolean outputFolder = false;

        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getName().equals("outputs")) {
                    outputFolder = true;

                }

                continue;

            }

        }

        setScaffSizes(sizesFile);
        setScaffData(dataFile);
        processAllScaffs();

        if (!outputFolder) {
            new File(folderPath + "/outputs").mkdirs();
            outputFolder = true;

        }

        // Write all outputs
        Iterator<Map.Entry<String, List<String[]>>> iter = this.outputs.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, List<String[]>> entry = iter.next();
            String scaffold = entry.getKey();
            String outputFileName = scaffold + "_pi" + ".tsv";
            String outputFilePath = folderPath + "/outputs/" + outputFileName;

            writeOutput(outputFilePath, scaffold);

        }

    }

}
