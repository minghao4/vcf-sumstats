package com.longlab.sumstats;

public class UserInterface {
    private int cultivars;
    private String command;
    private String filePath1;
    private String filePath2;

    public UserInterface(String[] args) {
        if (args.length < 2) {
            instructions(0);

        } else {
            try {
                this.cultivars = Integer.parseInt(args[0]);

            } catch (Exception e) {
                instructions(1);

            }

            this.command = args[1];

        }

        if (args.length > 2) {
            this.filePath1 = args[2];

        }

        if (args.length > 3) {
            this.filePath2 = args[3];

        }

        start();

    }

    private void instructions(int exitCode) {
        System.out.println("VCF Summary Statistics.");
        System.out.println();
        System.out.println("Please enter commands in the following format (order too please!):");
        System.out.println("vcf-sumstats.jar <number of cultivars> <command> [files]");
        System.out.println();
        System.out.println("Run the commands for further instructions on file input.");
        System.out.println("------------------------");
        System.out.println("Available commands:");
        System.out.println("VcfParser");
        System.out.println("Histograms");
        System.out.println("Linkage");
        System.exit(exitCode);

    }

    private void start() {
        String[] commands = {"VcfParser", "Histograms", "Linkage"};

        for (String command : commands) {
            if (this.command.equals(command)) {
                fileCheck(this.command);
                return;

            }

        }

        instructions(1);

    }

    private void fileCheck(String command) {
        if (command.equals("VcfParser")) {
            if (this.filePath1 == null || this.filePath2 == null) {
                    System.out.println("VCF Summary Statistics - VCF Parser.");
                    System.out.println();
                    System.out.println(
                        "Please check if you have the correct inputs/order of inputs:");
                    System.out.println(
                        "vcf-sumstats.jar <number of cultivars> VcfParser <folder containing VCFs> <output.tsv>");
                    System.exit(1);

            }

            VcfParser vp = new VcfParser(this.cultivars);
            System.out.println("Reading files...");
            vp.readAllFiles(this.filePath1);
            System.out.println("Writing output...");
            vp.writeOutput(this.filePath2);
            System.out.println("All done!");

        } else if (command.equals("Histograms")) {
            if (this.filePath1 == null || this.filePath2 == null) {
                System.out.println("VCF Summary Statistics - Histograms.");
                System.out.println();
                System.out.println("Please check if you have the correct inputs/order of inputs:");
                System.out.println(
                    "vcf-sumstats.jar <number of cultivars> Histograms <folder containing *SORTED* variant TSVs> <annotatedRegions.tsv>");
                System.exit(1);

            }

            Histograms hist = new Histograms(this.cultivars);
            System.out.println("Setting regions...");
            hist.setRegions(this.filePath2);
            System.out.println("Reading variant inputs and writing outputs...");
            hist.readWriteVariantFiles(this.filePath1);
            System.out.println("All done!");

        } else {
            if (this.filePath1 == null) {
                System.out.println("VCF Summary Statistics - Linkage.");
                System.out.println();
                System.out.println("Please check if you have the correct inputs/order of inputs:");
                System.out.println(
                    "vcf-sumstats.jar <number of cultivars> Linkage <folder containing *SORTED* SNP TSV>");
                System.exit(1);

            }

            Linkage link = new Linkage(this.cultivars);
            link.calcLinkage(this.filePath1);

        }

    }

}
