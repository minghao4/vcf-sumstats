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
import java.util.Iterator;

public class PhaseInputFormatter {
    private List<String[]> input;
    private Map<String, List<String>> outputs;
    private TsvParser parser;

    public PhaseInputFormatter() {
        this.input = new ArrayList<String[]>();
        this.outputs = new HashMap<String, List<String>>();

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

    private List<String> formatOutput(int numLoci, List<String> pos,
        Map<String, List<List<String>>> idGt) {

        List<String> format = new ArrayList<String>();
        format.add("12");
        format.add(Integer.toString(numLoci));

        String posLine = "P ";
        for (String entry : pos) {
            posLine += entry + " ";

        }

        format.add(posLine);
        format.add(setLociTypes(numLoci));

        Iterator<Map.Entry<String, List<List<String>>>> iter = idGt.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, List<List<String>>> entry = iter.next();
            String[] alleles = formatGt(entry.getValue());
            String a1 = alleles[0];
            String a2 = alleles[1];

            format.add(entry.getKey());
            format.add(a1);
            format.add(a2);

        }

        return format;

    }

    private String[] formatGt(List<List<String>> gtLines) {
        String[] gt = new String[2];
        String a1 = "";
        String a2 = "";
        List<String> str1 = gtLines.get(0);
        List<String> str2 = gtLines.get(1);
        int numLoci = str1.size();

        for (int i = 0; i < numLoci; i++) {
            a1 += str1.get(i) + " ";
            a2 += str2.get(i) + " ";

        }

        gt[0] = a1;
        gt[1] = a2;
        return gt;

    }

    private Map<String, List<List<String>>> setId() {
        Map<String,List<List<String>>> idGt = new HashMap<String, List<List<String>>>();
        String[] id = {"Canda", "CFX1", "CFX2", "CRS1", "Delores", "Finola", "Grandi", "Joey",
            "Katani", "Picolo", "Silesia", "X59"};

        for (String cultivar : id) {
            List<String> a1 = new ArrayList<String>();
            List<String> a2 = new ArrayList<String>();
            List<List<String>> gt = new ArrayList<List<String>>();
            gt.add(a1);
            gt.add(a2);
            idGt.put(cultivar, gt);

        }

        return idGt;

    }

    private String setLociTypes(int numLoci) {
        String lociTypes = "";

        for (int i = 0; i < numLoci; i++) {
            lociTypes += "S";

        }

        return lociTypes;

    }

    private void setIdGt(String[] row, Map<String, List<List<String>>> idGt) {
        idGt.get("Canda").get(0).add(row[1]);
        idGt.get("Canda").get(1).add(row[2]);

        idGt.get("CFX1").get(0).add(row[3]);
        idGt.get("CFX1").get(1).add(row[4]);

        idGt.get("CFX2").get(0).add(row[5]);
        idGt.get("CFX2").get(1).add(row[6]);

        idGt.get("CRS1").get(0).add(row[7]);
        idGt.get("CRS1").get(1).add(row[8]);

        idGt.get("Delores").get(0).add(row[9]);
        idGt.get("Delores").get(1).add(row[10]);

        idGt.get("Finola").get(0).add(row[11]);
        idGt.get("Finola").get(1).add(row[12]);

        idGt.get("Grandi").get(0).add(row[13]);
        idGt.get("Grandi").get(1).add(row[14]);

        idGt.get("Joey").get(0).add(row[15]);
        idGt.get("Joey").get(1).add(row[16]);

        idGt.get("Katani").get(0).add(row[17]);
        idGt.get("Katani").get(1).add(row[18]);

        idGt.get("Picolo").get(0).add(row[19]);
        idGt.get("Picolo").get(1).add(row[20]);

        idGt.get("Silesia").get(0).add(row[21]);
        idGt.get("Silesia").get(1).add(row[22]);

        idGt.get("X59").get(0).add(row[23]);
        idGt.get("X59").get(1).add(row[24]);

    }

    private void processInput() {
        String scaffold = "";
        int numLoci = 0;
        List<String> positions = new ArrayList<String>();
        Map<String, List<List<String>>> idGt = setId();

        for (String[] row : this.input) {
            String currScaff = row[0].split("_")[0];

            if (scaffold.equals("")) {
                scaffold = currScaff;

            }

            String currPos = row[0].split("_")[1];

            if (currScaff.equals(scaffold)) {
                numLoci++;
                positions.add(currPos);
                setIdGt(row, idGt);

            } else {
                List<String> val = formatOutput(numLoci, positions, idGt);
                this.outputs.put(scaffold, val);

                scaffold = currScaff;
                numLoci = 1;
                positions = new ArrayList<String>();
                positions.add(currPos);
                idGt = setId();
                setIdGt(row, idGt);

            }

        }

        List<String> val = formatOutput(numLoci, positions, idGt);
        this.outputs.put(scaffold, val);

    }

    private void writeOutput(String outputFilePath, String scaffold) {
        TsvWriter writer = null;

        try {
            writer = new TsvWriter(new FileWriter(outputFilePath), new TsvWriterSettings());

        } catch (IOException e) {
            e.printStackTrace();

        }

        for (String row : this.outputs.get(scaffold)) {
            writer.writeRow(row);

        }

        writer.flush();
        writer.close();

    }

    public void formatPhaseInputs(String folderPath) {
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

            setInput(file.getPath());
            processInput();

            if (!outputFolder) {
                new File(folderPath + "/outputs").mkdirs();
                outputFolder = true;

            }

            String fileName = file.getName().substring(0, file.getName().length() - 4);
            Iterator<Map.Entry<String, List<String>>> iter = this.outputs.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry<String, List<String>> entry = iter.next();
                String scaffold = entry.getKey();
                String outputFileName = fileName + "_" + scaffold + ".tsv";
                String outputFilePath = folderPath + "/outputs/" + outputFileName;

                writeOutput(outputFilePath, scaffold);

            }

        }

    }

}
