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

    private void initParser() {
        TsvParserSettings parserSettings = new TsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        RowListProcessor rowProcessor = new RowListProcessor();
        parserSettings.setProcessor(rowProcessor);

        this.parser = new TsvParser(parserSettings);

    }

    private List<String[]> readFile(String filePath) {
        List<String[]> newFile = new ArrayList<String[]>();

        try {
            newFile = this.parser.parseAll(new FileReader(filePath));

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }

        return newFile;

    }

    private void setScaffSizes(String scaffSizesFilePath) {
        List<String[]> scaffSizes = readFile(scaffSizesFilePath);

        for (String[] row : scaffSizes) {
            this.scaffoldSizes.put(row[0], Integer.parseInt(row[1]));

        }

    }

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

    private void processScaffold(String scaffold, List<String[]> scaffData, int scaffSize) {
        List<String[]> val = new ArrayList<String[]>();
        int fullBins = scaffSize / 1000;
        int binMax = 1000;

        /*
            pi = 2 * (varSites / 1000) * (temp1 + temp2 ... + tempN)

            tempI = (# alts * # refs) / (24 * 23)
        */

        int varSites = 0;
        double tempSum = 0;

        int binIdx = 0;

        while (binIdx < fullBins) {
            for (String[] row : scaffData) {
                int pos = Integer.parseInt(row[0].split("_")[1]);

                if (pos <= binMax) {
                    varSites++;

                } else {
                    binIdx++;
                    double pi = 2 * (double)varSites / 1000 * tempSum;
                    String[] newEntry = {Integer.toString(binMax - 500),
                        Double.toString(pi)};

                    val.add(newEntry);
                    binMax += 1000;
                    varSites = 1;
                    tempSum = 0;

                }

                double snps = Double.parseDouble(row[1]) * 24;
                double temp = (snps * (24 - snps)) / (24 * 23);
                tempSum += temp;

            }

        }

        double pi = 2 * (double)varSites / 1000 * tempSum;
        String[] newEntry = {Integer.toString(binMax - 500),
            Double.toString(pi)};

        val.add(newEntry);
        this.outputs.put(scaffold, val);

    }

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
