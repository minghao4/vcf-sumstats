package com.longlab.sumstats;

import com.univocity.parsers.tsv.*;
import com.univocity.parsers.common.processor.RowListProcessor;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


public class RegionAnnotater {
    private Map<String, Integer> scaffolds;
    private List<String[]> exons;

    private List<String[]> output;
    private TsvParser parser;

    public RegionAnnotater() {
        this.scaffolds = new HashMap<String,Integer>();
        this.exons = new ArrayList<String[]>();
        this.output = new ArrayList<String[]>();

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

    private void setScaffolds(String scaffoldFile) {
        for (String[] row : readFile(scaffoldFile)) {
            this.scaffolds.put(row[0], Integer.parseInt(row[1]));

        }

    }

    private void setExons(String exonFile) {
        this.exons = readFile(exonFile);

    }

    private String[] formatEntry(String scaffold, int start, int end, String annotation,
        String strand, String geneId) {

        String[] entry = {scaffold, Integer.toString(start), Integer.toString(end), annotation,
            strand, geneId};

        return entry;

    }

    public void annotateRegions(String scaffoldFile, String exonFile) {
        setScaffolds(scaffoldFile);
        setExons(exonFile);

        String scaffold = "";
        int prevStart = 1;
        int prevEnd = 1;
        String prevStrand = "";
        String prevGeneId = "";

        for (int i = 0; i < this.exons.size(); i++) {
            String[] exon = this.exons.get(i);
            String currScaffold = exon[0];
            int currStart = Integer.parseInt(exon[3]);
            int currEnd = Integer.parseInt(exon[4]);
            String annotation = "exon";
            String strand = exon[6];
            String geneId = exon[8].split(" ")[1];
            geneId = geneId.substring(1, geneId.length() - 2); // getting rid of quotation marks and
                                                               // semicolon

            String[] currEntry = formatEntry(currScaffold, currStart, currEnd, annotation, strand,
                geneId);

            // First region
            if (i == 0) {
                this.output.add(currEntry);
                scaffold = currScaffold;
                prevEnd = currEnd;
                prevStrand = strand;
                prevGeneId = geneId;
                continue;

            // First region on new scaffold
            } else if (!currScaffold.equals(scaffold)) {
                if (prevEnd < this.scaffolds.get(scaffold)) {
                    prevStart = prevEnd + 1;
                    prevEnd = this.scaffolds.get(scaffold);
                    this.output.add(formatEntry(scaffold, prevStart, prevEnd, "intergenic", "n/a",
                        "n/a"));

                }

                // If first region on new scaffold doesn't start at 1
                if (currStart > 1) {
                    this.output.add(formatEntry(currScaffold, 1, currStart - 1, "intergenic", "n/a",
                        "n/a"));

                }


            } else {
                if (currStart > prevEnd + 1) {
                    prevStart = prevEnd + 1;
                    prevEnd = currStart - 1;
                    String prevAnnotation = "intron";

                    if (!prevGeneId.equals(geneId)) {
                        prevAnnotation = "intergenic";
                        prevStrand = "n/a";
                        prevGeneId = "n/a";

                    }

                    this.output.add(formatEntry(currScaffold, prevStart, prevEnd, prevAnnotation,
                        prevStrand, prevGeneId));

                }

            }

            this.output.add(currEntry);
            scaffold = currScaffold;
            prevStart = currStart;
            prevEnd = currEnd;
            prevStrand = strand;
            prevGeneId = geneId;


        }

    }

    public void writeOutput(String outputFilePath) {
        TsvWriter writer = null;

        try {
            writer = new TsvWriter(new FileWriter(outputFilePath),
                new TsvWriterSettings());

        } catch (IOException e) {
            e.printStackTrace();

        }

        writer.writeHeaders("Scaffold", "Start", "End", "Annotation", "Strand", "GeneId");
        for (String[] row : this.output) {
            writer.writeRow(row);

        }

        writer.flush();
        writer.close();

    }

}
