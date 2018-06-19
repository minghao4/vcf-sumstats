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

import java.lang.StringBuilder;

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

    private List<String> formatOutput(int numLoci, List<String> pos, String lociTypes,
        String[][] idGt) {

        List<String> format = new ArrayList<String>();
        format.add("12");
        format.add(Integer.toString(numLoci));

        String posLine = "P ";
        for (String entry : pos) {
            posLine += entry + " ";

        }

        format.add(posLine);
        format.add(lociTypes);

        for (int i = 0; i < 12; i++) {
            format.add(idGt[i][0]);
            format.add(idGt[i][1] + "\n");

        }

        return format;

    }

    private String[][] setId() {
        String[][] idGt = new String[12][2];
        String[] id = {"Canda", "CFX1", "CFX2", "CRS1", "Delores", "Finola", "Grandi", "Joey",
            "Katani", "Picolo", "Silesia", "X59"};

        for (int i = 0; i < 12; i++) {
            idGt[i][0] = id[i];

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

    private void setIdGt(String[] row, String[][] idGt) {


        for (int i = 1; i < 23; i += 2) {
            int idIdx = (i - 1) / 2;

            if (row[i].equals(row[i + 1])) {
                // idGt[idIdx][1] =

            }

        }

    }

    private void processInput() {
        String[][] idGtFormat = setId();

        String scaffold = "";
        int numLoci = 0;
        List<String> positions = new ArrayList<String>();
        String lociTypes = "";
        String[][] idGt = idGtFormat;

        for (String[] row : this.input) {
            String currScaff = row[0].split("_")[0];

            if (scaffold.equals("")) {
                scaffold = currScaff;

            }

            String currPos = row[0].split("_")[1];

            if (currScaff.equals(scaffold)) {
                numLoci++;
                positions.add(currPos);


            }

        }

    }

}
