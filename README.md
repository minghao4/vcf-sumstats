# vcf-sumstats

Usage:

`java -jar vcf-sumstats.jar <number of cultivars> <command> [files]`

Available commands:

* VcfParser
* Histograms
* Linkage

Formats for each:

``` java
java -jar vcf-sumstats.jar <number of cultivars> VcfParser <folder containing VCFs> <output.tsv>

java -jar vcf-sumstats.jar <number of cultivars> Histograms <folder containing sorted variant TSVs> <annotatedRegions.tsv>

java -jar vcf-sumstats.jar <number of cultivars> Linkage <folder containing sorted SNP TSV>
```

## Please use TSV files sorted alphabetically by scaffold and ascending by position!! This is very important!!!
