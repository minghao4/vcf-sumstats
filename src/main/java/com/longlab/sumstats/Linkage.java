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
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.math3.util.CombinatoricsUtils;

public class Linkage {
    private Map<String, List<String[]>> scaffolds;
    private List<String[]> input;
    private double[][] output;
    private TsvParser parser;

    public Linkage() {
        this.scaffolds = new HashMap<String,List<String[]>>();
        this.input = new ArrayList<String[]>();
        this.output = new double[1][1];

        initParser();

    }

    private void initParser() {
        TsvParserSettings parserSettings = new TsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        RowListProcessor rowProcessor = new RowListProcessor();
        parserSettings.setProcessor(rowProcessor);

        this.parser = new TsvParser(parserSettings);

    }

    private void setInput(String filePath) {
        try {
            this.input = this.parser.parseAll(new FileReader(filePath));

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }

    }

    private void setScaffolds() {
        for (String[] row : this.input) {
            String prevScaffold = "";
            String currScaffold = row[0].split("_")[0];

            if (currScaffold.equals(prevScaffold)) {
                this.scaffolds.get(prevScaffold).add(row);

            } else {
                List<String[]> val = new ArrayList<String[]>();
                val.add(row);

                this.scaffolds.put(currScaffold, val);
                prevScaffold = currScaffold;

            }

        }

        this.input.clear();

    }

    private void filterScaffolds() {
        for (Map.Entry<String, List<String[]>> entry : this.scaffolds.entrySet()) {
            if (entry.getValue().size() < 2) {
                this.scaffolds.remove(entry.getKey());

            }

        }

    }

    private void getOutputDim() {
        int dimCol = 2;
        int dimRow = 0;

        for (Map.Entry<String, List<String[]>> entry : this.scaffolds.entrySet()) {
            dimRow += CombinatoricsUtils.binomialCoefficient(entry.getValue().size(), 2);

        }

        this.output = new double[dimRow][dimCol];

    }

    private double rSquared(String[] varA, String[] varB) {
        double pA = Double.parseDouble(varA[25]);
        double pB = Double.parseDouble(varB[25]);
        int countAB = 0;

        for (int i = 1; i < 24; i++) {
            if (!varA[i].equals("0") && !varB[i].equals("0")) {
                countAB++;

            }

        }

        double d = (countAB / 24) - (pA * pB);
        double r = d / Math.sqrt(pA * pB * (1 - pA) * (1 - pB));

        return Math.pow(r, 2);

    }

    private void setOutput() {
        getOutputDim();

        for (Map.Entry<String, List<String[]>> entry : this.scaffolds.entrySet()) {
            int row = 0;
            List<String[]> vars = entry.getValue();
            int varsLen = vars.size();

            for (int i = 0; i < varsLen - 1; i++) {
                String[] varA = vars.get(i);
                String[] varB = vars.get(i + 1);

                double[] newOutputEntry = new double[2];
                int distance = Math.abs(Integer.parseInt(varA[0].split("_")[1]) -
                    Integer.parseInt(varB[0].split("_")[1]));

                newOutputEntry[0] = (double)distance;
                newOutputEntry[1] = rSquared(varA, varB);

                this.output[row] = newOutputEntry;
                row++;

            }

        }

    }

    private void writeOutput(String outputFilePath) {
        TsvWriter writer = null;

        try {
            writer = new TsvWriter(new FileWriter(outputFilePath),
                new TsvWriterSettings());

        } catch (IOException e) {
            e.printStackTrace();

        }

        writer.writeHeaders("#Distance", "rSquared");
        for (double[] row : this.output) {
            writer.writeRow(row);

        }

        writer.flush();
        writer.close();

    }

    public void calcLinkage(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                continue;

            }

            setInput(file.getPath());
            setScaffolds();
            filterScaffolds();
            setOutput();

            String fileName = file.getName().substring(0, file.getName().length() - 4);
            String outputFileName = fileName + "_output.tsv";
            String outputFilePath = folderPath + "/" + outputFileName;
            writeOutput(outputFilePath);

        }

    }

}
