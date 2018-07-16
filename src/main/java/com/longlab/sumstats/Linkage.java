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

public class Linkage {
    private Map<String, List<String[]>> scaffolds;
    private List<String[]> input;
    private List<double[]> output;
    private TsvParser parser;
    private int cultivars;

    public Linkage(int cultivars) {
        this.scaffolds = new HashMap<String,List<String[]>>();
        this.input = new ArrayList<String[]>();
        this.output = new ArrayList<double[]>();
        this.cultivars = cultivars;

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
        String prevScaffold = "";

        for (String[] row : this.input) {
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
        Iterator<Map.Entry<String, List<String[]>>> iter = this.scaffolds.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<String, List<String[]>> entry = iter.next();

            if (entry.getValue().size() < 2) {
                iter.remove();

            }

        }

    }

    private int status(String[] varA, String[] varB) {
        // 0 = hom
        // 1 = varA anomaly
        // 2 = varB anom

        int status = 0;
        int totalAlleles = this.cultivars * 2;

        if (Double.parseDouble(varA[totalAlleles + 1]) == 1) {
            status = 1;

        } else if (Double.parseDouble(varB[totalAlleles + 1]) == 1) {
            status = 2;

        } else {
            for (int i = 1; i <= (totalAlleles - 1); i += 2) {
                if (!varA[i].equals(varA[i + 1])) {
                    status = 1;

                } else if (!varB[i].equals(varB[i + 1])) {
                    status = 2;

                }

            }

        }

        return status;

    }

    private double rSquared(String[] varA, String[] varB) {
        int totalAlleles = this.cultivars * 2;

        double pA = Double.parseDouble(varA[totalAlleles + 1]);
        double pB = Double.parseDouble(varB[totalAlleles + 1]);
        double countAB = 0;

        for (int i = 1; i <= (totalAlleles - 1); i += 2) {
            if (!varA[i].equals("0") && !varB[i].equals("0")) {
                countAB += 2;

            }

        }

        double d = (countAB / totalAlleles) - (pA * pB);
        double r = d / Math.sqrt(pA * pB * (1 - pA) * (1 - pB));

        return Math.pow(r, 2);

    }

    private void setOutput() {
        for (Map.Entry<String, List<String[]>> entry : this.scaffolds.entrySet()) {
            List<String[]> vars = entry.getValue();
            int varsLen = vars.size();

            for (int i = 0; i < varsLen; i++) {
                String[] varA = vars.get(i);

                innerLoop:
                for (int j = i + 1; j < varsLen; j++) {
                    String[] varB = vars.get(j);
                    int status = status(varA, varB);

                    if (status == 1) {
                        break innerLoop;

                    } else if (status == 2) {
                        continue innerLoop;

                    }

                    double[] newOutputEntry = new double[2];
                    int distance = Math.abs(Integer.parseInt(varA[0].split("_")[1]) -
                        Integer.parseInt(varB[0].split("_")[1]));

                    newOutputEntry[0] = (double)distance;
                    newOutputEntry[1] = rSquared(varA, varB);
                    this.output.add(newOutputEntry);

                }

            }

        }

    }

    private void writeOutput(String outputFilePath) {
        TsvWriter writer = null;

        try {
            writer = new TsvWriter(new FileWriter(outputFilePath), new TsvWriterSettings());

        } catch (IOException e) {
            e.printStackTrace();

        }

        writer.writeHeaders("#Distance", "rSquared");
        for (double[] row : this.output) {
            if (row[0] == 0 && row[1] == 0) {
                break;

            }

            String[] writeable = new String[2];
            writeable[0] = Double.toString(row[0]);
            writeable[1] = Double.toString(row[1]);

            writer.writeRow(writeable);

        }

        writer.flush();
        writer.close();

    }

    public void calcLinkage(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        boolean outputFolder = false;

        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getName().equals("output")) {
                    outputFolder = true;

                }

                continue;

            }

            setInput(file.getPath());
            setScaffolds();
            filterScaffolds();
            setOutput();

            if (!outputFolder) {
                new File(folderPath + "/output").mkdirs();
                outputFolder = true;

            }

            String fileName = file.getName().substring(0, file.getName().length() - 4);
            String outputFileName = fileName + "_linkage_output.tsv";
            String outputFilePath = folderPath + "/output/" + outputFileName;
            writeOutput(outputFilePath);

        }

    }

}
