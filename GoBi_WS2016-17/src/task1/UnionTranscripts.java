package task1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import genomeAnnotation.Gene;
import genomeAnnotation.GenomeAnnotation;
import util.UnionTranscript;

public class UnionTranscripts {

	private GenomeAnnotation ga;
	private File outputDirectory;

	public UnionTranscripts(GenomeAnnotation ga, String outputDirectory) {
		this.ga = ga;
		this.outputDirectory = new File(outputDirectory);
		if (!this.outputDirectory.exists()) {
			System.out.println("directory does not exist! dir: " + this.outputDirectory.getAbsolutePath());
			System.exit(1);
		}
		if (!this.outputDirectory.isFile()) {
			System.out.println("dir is a file not a directory! file: " + this.outputDirectory.getAbsolutePath());
			System.exit(1);
		}

	}

	public HashMap<Double, Integer> calculateUnionTranscriptDistribution() {
		HashMap<Double, Integer> occurencesOfProportion = new HashMap<>();

		Iterator<Gene> allGenesInGa = ga.iterator();
		Gene g = null;
		UnionTranscript unionTr = null;
		Double d;
		Integer occurences;
		while ((g = allGenesInGa.next()) != null) {
			unionTr = new UnionTranscript(g);
			// prozentual auf eine nachkommastelle --> max size = 1000
			d = ((double) g.getLongestTranscriptLength() / unionTr.getExonicLength()) * 1000;
			d = d.intValue() / 10d;
			occurences = occurencesOfProportion.get(d);
			if (occurences == null)
				occurencesOfProportion.put(d, 1);
			else
				occurencesOfProportion.put(d, occurences + 1);
		}
		return occurencesOfProportion;
	}

	public void writeOccurencesToFile(HashMap<Double, Integer> occurenceDistribution) {
		TreeMap<Double, Integer> sortedDistribution = new TreeMap<>();
		sortedDistribution.putAll(occurenceDistribution);
		try {
			BufferedWriter bw = new BufferedWriter(
					new FileWriter(outputDirectory + "/" + ga.getName() + "_unionTrDist.tsv"));
			for (Entry<Double, Integer> e : sortedDistribution.entrySet())
				bw.write(e.getKey() + "\t");
			bw.write("\n");
			for (Entry<Double, Integer> e : sortedDistribution.entrySet())
				bw.write(e.getValue() + "\t");
			bw.close();
		} catch (Exception e) {
			System.out.println("Error while writing unionTranscript distributionFile");
			e.printStackTrace();
			System.exit(1);
		}
	}

}
