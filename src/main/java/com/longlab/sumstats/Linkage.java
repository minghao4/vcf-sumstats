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
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.math3.util.CombinatoricsUtils;

public class Linkage {
    private Map<String, List<String[]>> scaffolds;
    private List<String[]> input;
    private double[][] outputHom;
    private Set<String[]> outputHet;
    private TsvParser parser;

    public Linkage() {
        this.scaffolds = new HashMap<String,List<String[]>>();
        this.input = new ArrayList<String[]>();
        this.outputHom = new double[1][1];
        this.outputHet = new HashSet<String[]>();
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

    private void getOutputDim() {
        int dimCol = 2;
        int dimRow = 0;

        for (Map.Entry<String, List<String[]>> entry : this.scaffolds.entrySet()) {
            dimRow += CombinatoricsUtils.binomialCoefficient(entry.getValue().size(), 2);

        }

        this.outputHom = new double[dimRow][dimCol];

    }

    private double rSquared(String[] varA, String[] varB) {
        double pA = Double.parseDouble(varA[25]);
        double pB = Double.parseDouble(varB[25]);
        double countAB = 0;

        for (int i = 1; i < 23; i += 2) {
            boolean aHet = false;
            boolean bHet = false;
            if (!varA[i].equals(varA[i + 1])) {
                this.outputHet.add(varA);
                aHet = true;

            } else if (!varB[i].equals(varB[i + 1])) {
                this.outputHet.add(varB);
                bHet = true;

            }

            if (aHet || bHet) {
                continue;

            }

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
        int row = 0;

        for (Map.Entry<String, List<String[]>> entry : this.scaffolds.entrySet()) {

            List<String[]> vars = entry.getValue();
            int varsLen = vars.size();

            for (int i = 0; i < varsLen; i++) {
                String[] varA = vars.get(i);

                for (int j = i + 1; j < varsLen; j++) {
                    String[] varB = vars.get(j);

                    double[] newOutputEntry = new double[2];
                    int distance = Math.abs(Integer.parseInt(varA[0].split("_")[1]) -
                        Integer.parseInt(varB[0].split("_")[1]));

                    newOutputEntry[0] = (double)distance;
                    newOutputEntry[1] = rSquared(varA, varB);

                    this.outputHom[row] = newOutputEntry;
                    row++;

                }

            }

        }

    }

    private void writeOutput(String outputHomFilePath, String outputHetFilePath) {
        TsvWriter writerHom = null;
        TsvWriter writerHet = null;

        try {
            writerHom = new TsvWriter(new FileWriter(outputHomFilePath), new TsvWriterSettings());
            writerHet = new TsvWriter(new FileWriter(outputHetFilePath), new TsvWriterSettings());

        } catch (IOException e) {
            e.printStackTrace();

        }

        writerHom.writeHeaders("#Distance", "rSquared");
        for (double[] row : this.outputHom) {
            if (row[0] == 0 && row[1] == 0) {
                break;

            }

            String[] writeable = new String[2];
            writeable[0] = Double.toString(row[0]);
            writeable[1] = Double.toString(row[1]);

            writerHom.writeRow(writeable);

        }

        writerHet.writeHeaders("Scaffold_Position",
        "Canda_A1",   "Canda_A2",
        "CFX1_A1",    "CFX1_A2",
        "CFX2_A1",    "CFX2_A2",
        "CRS1_A1",    "CRS1_A2",
        "Delores_A1", "Delores_A2",
        "Finola_A1",  "Finola_A2",
        "Grandi_A1",  "Grandi_A2",
        "Joey_A1",    "Joey_A2",
        "Katani_A1",  "Katani_A2",
        "Picolo_A1",  "Picolo_A2",
        "Silesia_A1", "Silesia_A2",
        "X59_A1",     "X59_A2",
        "Allele_Frequency");

        for (String[] row : this.outputHet) {
            writerHet.writeRow(row);

        }

        writerHom.flush();
        writerHet.flush();
        writerHom.close();
        writerHet.close();

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
            String outputHomFileName = fileName + "_outputHom.tsv";
            String outputHetFileName = fileName + "_outputHet.tsv";
            String outputHomFilePath = folderPath + "/output/" + outputHomFileName;
            String outputHetFilePath = folderPath + "/output/" + outputHetFileName;
            writeOutput(outputHomFilePath, outputHetFilePath);

        }

    }

}
