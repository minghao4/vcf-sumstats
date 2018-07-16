# vcf-sumstats

Usage:

`java -jar vcf-sumstats.jar <number of cultivars> <command> [files]`

Available commands:

* VcfParser
* Histograms
* Linkage

Formats for each:

`java -jar vcf-sumstats.jar <number of cultivars> VcfParser <folder containing VCFs> <output.tsv>`

`java -jar vcf-sumstats.jar <number of cultivars> Histograms <folder containing variant TSVs> <annotatedRegions.tsv>`

`java -jar vcf-sumstats.jar <number of cultivars> Linkage <folder containing variant TSVs>`
