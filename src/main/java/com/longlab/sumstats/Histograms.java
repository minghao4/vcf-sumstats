package com.longlab.sumstats;

import com.univocity.parsers.tsv.*;
import com.univocity.parsers.common.processor.RowListProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Histograms {
    private Map<String, List<String[]>> regions;
    private List<String[]> currFile;
    private String[][] cleanOutput;
    private String[][] currOutput;
    private TsvParser parser;


    // Input Format(TSV): Scaffold, Position,

    public Histograms() {
        this.regions = new HashMap<String, List<String[]>>();
        this.currFile = new ArrayList<String[]>();
        initOutput();
        initParser();

    }

    private void initOutput() {
        this.currOutput = new String[4][25];
        this.currOutput[0][0] = "#Region";

        for (int i = 1; i < 25; i++) {
            this.currOutput[0][i] = i + "/24";

        }

        this.currOutput[1][0] = "exons";
        this.currOutput[2][0] = "introns";
        this.currOutput[3][0] = "intergenic";

        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < 25; j++) {
                this.currOutput[i][j] = "0";

            }

        }

        this.cleanOutput = this.currOutput;

    }

    private void initParser() {
        TsvParserSettings parserSettings = new TsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        RowListProcessor rowProcessor = new RowListProcessor();
        parserSettings.setProcessor(rowProcessor);

        this.parser = new TsvParser(parserSettings);

    }

    private void readCurrFile(String filePath) {
        try {
            this.currFile = this.parser.parseAll(new FileReader(filePath));

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }

    }

    public void setRegions(String regionsFilePath) {
        readCurrFile(regionsFilePath);

        for (String[] row : this.currFile) {
            String scaffold = row[0];
            String[] info = Arrays.copyOfRange(row, 1, row.length);

            if (this.regions.containsKey(scaffold)) {
                this.regions.get(scaffold).add(info);

            } else {
                ArrayList<String[]> list = new ArrayList<String[]>();
                list.add(info);

                this.regions.put(scaffold, list);

            }

        }

    }

    private boolean contains(int varPos, int rgnStart, int rgnEnd) {
        boolean status = false;

        if (varPos >= rgnStart && varPos <= rgnEnd) {
            status = true;

        }

        return status;

    }

    private void addOutputEntry(String annotation, String varFreq) {
        int entryIdx = 0;
        int rgnIdx = 0;

        for (int i = 1; i < 25; i++) {
            if (this.currOutput[0][i].equals(varFreq)) {
                entryIdx = i;
                break;

            }

        }

        for (int i = 1; i < 4; i++) {
            if (this.currOutput[i][0].contains(annotation)) {
                rgnIdx = i;
                break;

            }

        }

        int currEntry = Integer.parseInt(this.currOutput[rgnIdx][entryIdx]);
        currEntry++;
        this.currOutput[rgnIdx][entryIdx] = Integer.toString(currEntry);

    }

    private void locateVariant(String scaffold, int varPos, String varFreq) {
        List<String[]> annotatedRgns = this.regions.get(scaffold);

        if (annotatedRgns == null) {
            addOutputEntry("intergenic", varFreq);

        } else {

            for (String[] region : this.regions.get(scaffold)) {
                int rgnStart = Integer.parseInt(region[0]);
                int rgnEnd = Integer.parseInt(region[1]);
                String annotation = region[2];

                if (contains(varPos, rgnStart, rgnEnd)) {
                    addOutputEntry(annotation, varFreq);

                }

            }

        }

    }

    private void processVariants() {
        // Assumes current file is a variant file
        for (String[] row : this.currFile) {
            String[] scaffold_pos = row[0].split("_");
            String scaffold = scaffold_pos[0];
            int varPos = Integer.parseInt(scaffold_pos[1]);
            Double varFreqCount = Double.parseDouble(row[row.length - 1]) * 24;
            String varFreq = varFreqCount.intValue() + "/24";

            locateVariant(scaffold, varPos, varFreq);

        }

        this.currFile.clear();

    }

    private void writeVariantOutput(String outputFilePath) {
        TsvWriter writer = null;

        try {
            writer = new TsvWriter(new FileWriter(outputFilePath),
                new TsvWriterSettings());

        } catch (IOException e) {
            e.printStackTrace();

        }

        for (int i = 0; i < 4; i++) {
            writer.writeRow(this.currOutput[i]);

        }

        this.currOutput = this.cleanOutput;
        writer.flush();
        writer.close();

    }

    public void readWriteVariantFiles(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                continue;

            }

            readCurrFile(file.getPath());
            processVariants();

            String fileName = file.getName().substring(0, file.getName().length() - 4);
            String outputFileName = fileName + "_output.tsv";
            String outputFilePath = folderPath + "/" + outputFileName;
            writeVariantOutput(outputFilePath);

        }

    }

}
