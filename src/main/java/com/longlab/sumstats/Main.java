package com.longlab.sumstats;

public class Main {
    // public static void main(String[] args) {
    //     String inputFiles = args[0];
    //     String outputFile = args[1];
    //     VcfParser vp = new VcfParser();

    //     System.out.println("Reading files");
    //     vp.readAllFiles(inputFiles);
    //     vp.writeOutput(outputFile);

    // }

    // public static void main(String[] args) {
    //     String scaffoldFile = args[0];
    //     String exonFile = args[1];
    //     String outputFile = args[2];

    //     RegionAnnotater ra = new RegionAnnotater();
    //     ra.annotateRegions(scaffoldFile, exonFile);
    //     ra.writeOutput(outputFile);

    // }

    // public static void main(String[] args) {
    //     String folderPath = args[0];
    //     String regionsFilePath = args[1];
    //     Histograms hist = new Histograms();

    //     hist.setRegions(regionsFilePath);
    //     hist.readWriteVariantFiles(folderPath);

    // }

    public static void main(String[] args) {
        String folderPath = args[0];
        Linkage link = new Linkage();

        link.calcLinkage(folderPath);

    }

    // public static void main(String[] args) {
    //     String folderPath = args[0];
    //     SeqDivergence div = new SeqDivergence();

    // }

}
