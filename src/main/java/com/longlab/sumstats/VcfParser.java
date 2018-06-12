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

public class VcfParser {
    /*
    Order is as follows:
    1.  canda
    2.  cfx1
    3.  cfx2
    4.  crs1
    5.  delores
    6.  finola
    7.  grandi
    8.  joey
    9.  katani
    10. picolo
    11. silesia
    12. x59

    Key:
    0 - Variant not detected
    1 - Variant is to A
    2 - Variant is to C
    3 - Variant is to G
    4 - Variant is to T
    Larger positive number - Insertion
    Larger negative number - Deletion

    */

    private Map<String, int[][]> variants; // variant hashmap
    private List<String[]> currFile; // current vcf file read
    private TsvParser parser; // parser tool from univocity

    public VcfParser() {
        this.variants = new HashMap<String, int[][]>();
        this.currFile = new ArrayList<String[]>();

        initParser();

    }

    // 1. initiate parser settings:
    //     - automatic line separator detection
    //     - process file row by row
    // 2. initiate parser
    private void initParser() {
        TsvParserSettings parserSettings = new TsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        RowListProcessor rowProcessor = new RowListProcessor();
        parserSettings.setProcessor(rowProcessor);

        this.parser = new TsvParser(parserSettings);

    }

    // read current file using parser's parseAll method
    private void readCurrFile(String filePath) {
        try {
            this.currFile = this.parser.parseAll(new FileReader(filePath));

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }

    }

    // return genotype status
    private int gtStatus(String gt) {
        // 0 = homozygous alt
        // 1 = heterozygous
        // 2 = heterozygous with 2 alts

        int status = 0;

        if (gt.equals("0/1")) {
            status = 1;

        }

        // NOT NEEDED
        // } else if (gt.equals("1/2")) {
        //     status = 2;

        // }

        return status;

    }

    // given an allele (point mutation or indel), return value as per key above
    private int alleleToVal(String allele) {
        int value = 0;

        for (int i = 0; i < allele.length(); i++) {
            char base = allele.charAt(i);
            int mod = i + 1;

            if (base == 'A') {
                value += 1 * mod;

            } else if (base == 'C') {
                value += 2 * mod;

            } else if (base == 'G') {
                value += 3 * mod;

            } else if (base == 'T') {
                value += 4 * mod;

            }

        }

        return value;

    }

    // set single allele entry given reference allele and the variant allele
    private int setVarEntry(String refAllele, String varAllele) {
        int varEntry = alleleToVal(varAllele);

        if (refAllele.length() != varAllele.length()) {
            varEntry = varEntry - alleleToVal(refAllele);

        }

        return varEntry;

    }

    // set entry for both alleles given variant entr
    private int[] setEntry(int varEntryA1, int varEntryA2) {
        int[] entry = {varEntryA1, varEntryA2};
        return entry;

    }

    // NOT NEEDED
    // private int[] multiAlleles(String refAllele, String varAllele) {
    //     String[] varAlleles = varAllele.split(",");
    //     int varEntryA1 = setVarEntry(refAllele, varAlleles[0]);
    //     int varEntryA2 = setVarEntry(refAllele, varAlleles[1]);

    //     return setEntry(varEntryA1, varEntryA2);

    // }

    private int[] entry(int gtStatus, String refAllele, String varAllele) {
        int varEntryA1 = 0;
        int varEntryA2 = 0;

        // NOT NEEDED
        // if (gtStatus == 2) {
        //     return multiAlleles(refAllele, varAllele);

        // }

        if (gtStatus == 0) {
            varEntryA1 = setVarEntry(refAllele, varAllele);

        }

        varEntryA2 = setVarEntry(refAllele, varAllele);

        return setEntry(varEntryA1, varEntryA2);

    }


    private void processCurrFile(int currFileIdx) {
        for (String[] line : this.currFile) {
            String idPos = line[0] + "_" + line[1];
            String gt = line[9].split(":")[0];
            int gtStatus = gtStatus(gt);

            String refAllele = line[3];
            String varAllele = line[4];
            int[] entry = entry(gtStatus, refAllele, varAllele);

            if (this.variants.containsKey(idPos)) {
                this.variants.get(idPos)[currFileIdx] = entry;

            } else {
                int[][] newEntry = new int[12][2];
                newEntry[currFileIdx] = entry;

                this.variants.put(idPos, newEntry);

            }

        }

        this.currFile.clear();

    }

    public void readAllFiles(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        Arrays.sort(files);
        int currFileIdx = 0;

        for (File file : files) {
            if (file.isDirectory()) {
                continue;

            }

            readCurrFile(file.getPath());
            processCurrFile(currFileIdx);

            currFileIdx++;

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

        writer.writeHeaders("Scaffold_Position",
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

        for (Map.Entry<String, int[][]> entry : this.variants.entrySet()) {
            String[] row = new String[26];
            int varCount = 0;
            row[0] = entry.getKey();

            int idx = 1;
            for (int[] val : entry.getValue()) {

                if (val[0] != 0) {
                    varCount++;

                }

                if (val[1] != 0) {
                    varCount++;

                }

                row[idx] = Integer.toString(val[0]);
                row[idx + 1] = Integer.toString(val[1]);

                idx += 2;

            }

            row[25] = Double.toString((double)varCount / 24);
            writer.writeRow(row);

        }

        writer.flush();
        writer.close();

    }

}
