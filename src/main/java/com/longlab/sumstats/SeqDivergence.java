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

import org.apache.commons.math3.util.CombinatoricsUtils;;

public class SeqDivergence {
    private Map<String, List<String[]>> scaffolds;
    private List<String[]> input;

    private TsvParser parser;

    public SeqDivergence() {
        this.scaffolds = new HashMap<String,List<String[]>>();
        this.input = new ArrayList<String[]>();

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

}
