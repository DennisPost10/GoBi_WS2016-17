package tasks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

import crawling.EnsemblCrawler;
import debugStuff.DebugMessageFactory;
import gtf.Gene;
import gtf.GenomeAnnotation;
import gtf.ThreadHandler;
import io.AllroundFileReader;
import io.AllroundFileWriter;
import io.ConfigHelper;
import io.ConfigReader;
import javafx.util.Pair;
import plotting.BarPlot;
import plotting.LinePlot;

public class Assignment1 {

	public void task_1() {
		long start = System.currentTimeMillis();

		ConfigHelper ch = new ConfigHelper();

		HashMap<String, String> fileMap = ConfigReader.readFilepathConfig(ch.getDefaultConfigPath("gtf-paths.txt"), "\t", new String[] {"#"});

		HashMap<String, HashMap<String, Integer>> biotypesOrgansimnCount = new HashMap<>();
		StupidComparator sc = new StupidComparator(biotypesOrgansimnCount);
		TreeMap<String, HashMap<String, Integer>> biotypesOrgansimnCountSorted = new TreeMap<>(sc);

		ThreadHandler th;
		GenomeAnnotation ga;

		for (Entry<String, String> entry : fileMap.entrySet()) {

			th = new ThreadHandler();
			th.startThreads(entry.getValue());
			ga = th.getGenomeAnnotation();

			for (Entry<String, Integer> entry2 : ga.getAmountGenesPerBiotype().entrySet()) {
				if (!biotypesOrgansimnCount.containsKey(entry2.getKey())) {
					biotypesOrgansimnCount.put(entry2.getKey(), new HashMap<String, Integer>());
				}
				biotypesOrgansimnCount.get(entry2.getKey()).put(entry.getKey(), entry2.getValue());	
			}

		}

		biotypesOrgansimnCountSorted.putAll(biotypesOrgansimnCount);

		AllroundFileWriter.writeXMLForTask1(ch.getDefaultOutputPath() + "biotypes_genes_organisms.xml", biotypesOrgansimnCountSorted);
		
		long end = System.currentTimeMillis();

		DebugMessageFactory.printInfoDebugMessage(true, "TASK 1 TOOK " + (end - start) + " MILLISECONDS.");

	}

	class StupidComparator implements Comparator<String> {
		HashMap<String, HashMap<String, Integer>> base;

		public StupidComparator(HashMap<String, HashMap<String, Integer>> base) {
			this.base = base;
		}

		public int getTotalCount(HashMap<String, Integer> map) {
			int out = 0;
			for (Integer i : map.values()) {
				out += i;
			}
			return out;
		}

		@Override
		public int compare(String o1, String o2) {
			if (getTotalCount(base.get(o1)) >= getTotalCount(base.get(o2))) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	class StupidComparator2 implements Comparator<String> {

		HashMap<String, HashMap<Integer, Integer>> base;

		public StupidComparator2(HashMap<String, HashMap<Integer, Integer>> base) {
			this.base = base;
		}

		public int getTotalCount(HashMap<Integer, Integer> map) {
			int out = 0;
			for (Integer i : map.keySet()) {
				out += 1;
			}
			return out;
		}

		@Override
		public int compare(String o1, String o2) {
			if (getTotalCount(base.get(o1)) >= getTotalCount(base.get(o2))) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	/**
	 * Create a barplot for every biotype containing all given gtf files as x-axis and the number of genes as y-axis
	 */
	public void task_2() {

		AllroundFileReader fr = new AllroundFileReader();
		ConfigHelper ch = new ConfigHelper();

		HashMap<String, HashMap<String, Integer>> s = fr.readXMLForTask1(ch.getDefaultOutputPath() + "biotypes_genes_organisms.xml");
		StupidComparator sc = new StupidComparator(s);
		TreeMap<String, HashMap<String, Integer>> sorted = new TreeMap<>(sc);

		sorted.putAll(s);

		TreeMap<String, String> annotMap = AllroundFileReader.readAnnotation(ch.getDefaultConfigPath("annot.map"));
		ArrayList<String> pathList = new ArrayList<>();
		
		int counter = 1;

		for (Entry<String, HashMap<String, Integer>> entryMain : sorted.entrySet()) {

			Vector<Object> values = new Vector<>(entryMain.getValue().values());
			Vector<Object> descr = new Vector<>();

			for (Entry<String, Integer> entrySub : entryMain.getValue().entrySet()) {
				descr.add(annotMap.get(entrySub.getKey()));
			}
			
			/* add a bar for every file which does not contain any genes for this biotype */
			for(String string : annotMap.values()){
				if(!descr.contains(string)){
					descr.add(string);
					values.add(0);
				}
			}

			Pair<Vector<Object>, Vector<Object>> tmpVector = new Pair<>(values, descr);

			BarPlot bp = new BarPlot(tmpVector, entryMain.getKey(), "", "");
			bp.plot();

			pathList.add(ch.getDefaultOutputPath() + "" + entryMain.getKey());

			DebugMessageFactory.printInfoDebugMessage(true, "PLOTTED " + counter + " / " + s.entrySet().size());
			counter++;
		}

		AllroundFileWriter.createHTMLforPlots(ch.getDefaultOutputPath() + "genetypes.html", pathList, null);

	}

	
	class TreeMapByValueSorter implements Comparator<String> {
		HashMap<String, Integer> base;

		public TreeMapByValueSorter(HashMap<String, Integer> base) {
			this.base = base;
		}

		@Override
		public int compare(String o1, String o2) {
			if (base.get(o1) >= base.get(o2)) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	public void task_3() {

		ConfigHelper ch = new ConfigHelper();
		EnsemblCrawler crawler = new EnsemblCrawler();

		HashMap<String, String> fileMap = ConfigReader.readFilepathConfig(ch.getDefaultConfigPath("gtf-paths.txt"),
				"\t", new String[] { "#" });

		HashMap<String, TreeMap<String, TreeMap<Integer, Integer>>> biotypesGeneTransCount = new HashMap<>();

		HashMap<String, HashMap<String, Integer>> amountMap = new HashMap<>();

		ThreadHandler th;
		GenomeAnnotation ga;

		for (Entry<String, String> entry : fileMap.entrySet()) {

			th = new ThreadHandler();
			th.startThreads(entry.getValue());
			ga = th.getGenomeAnnotation();

			HashMap<String, HashSet<Gene>> genesPerBiotype = ga.getGenesForEachBiotype();

			for (Entry<String, HashSet<Gene>> biotypeGeneEntry : genesPerBiotype.entrySet()) {

				for (Gene g : biotypeGeneEntry.getValue()) {

					int amountTranscripts = g.getTranscripts().size();

					/* if biotype exists */
					if (biotypesGeneTransCount.containsKey(biotypeGeneEntry.getKey())) {

						TreeMap<String, TreeMap<Integer, Integer>> tmp = biotypesGeneTransCount
								.get(biotypeGeneEntry.getKey());

						/* if file exists */
						if (tmp.containsKey(entry.getKey())) {
							TreeMap<Integer, Integer> tmp2 = tmp.get(entry.getKey());

							if (tmp2.containsKey(amountTranscripts)) {
								tmp2.put(amountTranscripts, tmp2.get(amountTranscripts) + 1);
							} else {
								tmp2.put(amountTranscripts, 1);
							}
						}
						/* if file does not exist */
						else {

							TreeMap<Integer, Integer> tmpMap = new TreeMap<>();
							tmpMap.put(amountTranscripts, 1);

							tmp.put(entry.getKey(), tmpMap);
						}
					}
					/* if biotype does not exist create new entry */
					else {
						TreeMap<Integer, Integer> tmpMap = new TreeMap<>();
						tmpMap.put(amountTranscripts, 1);

						TreeMap<String, TreeMap<Integer, Integer>> tmpMap2 = new TreeMap<>();
						tmpMap2.put(entry.getKey(), tmpMap);

						biotypesGeneTransCount.put(biotypeGeneEntry.getKey(), tmpMap2);
					}

					/* add do amount list */
					if (amountMap.containsKey(biotypeGeneEntry.getKey())) {

						amountMap.get(biotypeGeneEntry.getKey()).put(g.getId(), g.getTranscripts().size());

					} else {

						HashMap<String, Integer> tmp = new HashMap<>();
						tmp.put(g.getId(), g.getTranscripts().size());
						amountMap.put(biotypeGeneEntry.getKey(), tmp);

					}
				}
			}
		}

		TreeMap<String, TreeMap<Integer, Integer>> treeMap;

		TreeMap<String, String> annotMap = AllroundFileReader.readAnnotation(ch.getDefaultConfigPath("annot.map"));

		ArrayList<String> pathList = new ArrayList<>();
		ArrayList<ArrayList<String[]>> infoList = new ArrayList<>();

		for (Entry<String, TreeMap<String, TreeMap<Integer, Integer>>> s : biotypesGeneTransCount.entrySet()) {

			treeMap = new TreeMap<>();
			treeMap.putAll(s.getValue());

			int maxX = 0;
			int maxY = 0;

			 if(!s.getKey().equals("protein_coding")){
				 continue;
			 }

			Vector<Vector<Object>> vectorOfVectors1 = new Vector<>();
			Vector<Vector<Object>> vectorOfVectors2 = new Vector<>();

			Vector<Object> legendLabels = new Vector<>();

			for (Entry<String, String> annot : annotMap.entrySet()) {

				TreeMap<Integer, Integer> values = treeMap.get(annot.getKey());

				int counter = 0;

				Vector<Object> vTMP1 = new Vector<>();
				Vector<Object> vTMP2 = new Vector<>();

				if (values == null) {

					vTMP1.add(0);
					vTMP2.add(0);

					legendLabels.add(annot.getValue());

					vectorOfVectors1.add(vTMP1);
					vectorOfVectors2.add(vTMP2);

					continue;
				}

				for (Entry<Integer, Integer> s2 : values.entrySet()) {

					if (s2.getKey() <= 1) {
						continue;
					}

					maxX = Math.max(maxX, s2.getKey());

					vTMP1.add(s2.getKey());

					counter += s2.getValue();

					maxY = Math.max(maxY, counter);

					vTMP2.add(counter);

				}

				System.out.println(annot.getKey() + " : " + counter);

				legendLabels.add(annot.getValue());

				vectorOfVectors1.add(vTMP1);
				vectorOfVectors2.add(vTMP2);
			}

			LinePlot lp = new LinePlot(
					new Pair<Vector<Vector<Object>>, Vector<Vector<Object>>>(vectorOfVectors1, vectorOfVectors2),
					s.getKey(), "num tr/genes", "num genes", maxX, maxY);

			ArrayList<String[]> tmp = new ArrayList<>();

			int cnt = 0;

			TreeMapByValueSorter tvs = new TreeMapByValueSorter(amountMap.get(s.getKey()));
			
			TreeMap<String, Integer> tmpMap = new TreeMap<>(tvs);
			
			tmpMap.putAll(amountMap.get(s.getKey()));

			for (Entry<String, Integer> e : tmpMap.entrySet()) {

				if (cnt == 11) {
					break;
				}

				tmp.add(crawler.getGeneInfo(e.getKey()));

				cnt++;
			}

			pathList.add(ch.getDefaultOutputPath() + "" + s.getKey());
			infoList.add(tmp);

			lp.addLegendVector(legendLabels);

			lp.plot();

		}

		AllroundFileWriter.createHTMLforPlots(ch.getDefaultOutputPath() + "NumTransPerBiotype.html", pathList,infoList);

	}

	public static void main(String[] args) {

		Assignment1 as1 = new Assignment1();

//		as1.task_1();
		as1.task_2();
//		as1.task_3();

	}

}
