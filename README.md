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

A sample one liner would be:
`(head -n 2 <file> && tail -n +3 <file> | sort -k1,1 -k2,2n) > <sorted_file>`

In this example, column 1 contains scaffold names (sorted alphabetically) and column 2. The header is
excluded from sorting. If it's a variant file where the scaffold name and position are in one column,
one could do this:

`(head -n 2 <file> && tail -n +3 <file> | sort -k1,1 -k1,1n) > <sorted_file>`
