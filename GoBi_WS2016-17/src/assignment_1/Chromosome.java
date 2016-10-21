package assignment_1;

import java.util.HashMap;
import java.util.Iterator;

import augmentedTree.IntervalTree;

public class Chromosome implements GenomicRegion{

	private String id;
	
	private HashMap<String, Gene> genes;
	
	IntervalTree<Gene> geneTree;
	
	public Chromosome(String id) {
		this.id = id;
		this.genes = new HashMap<String, Gene>();
		this.geneTree = new IntervalTree<Gene>();
	}
	
	@Override
	public int getStart() {
		return -1;
	}

	@Override
	public int getStop() {
		return -1;
	}

	@Override
	public Iterator<GenomicRegion> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(GenomicRegion o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isOnForwardStrand() {
		return false;
	}

	@Override
	public String getChromosomeID() {
		return this.id;
	}

	@Override
	public GenomicRegionType getType() {
		return GenomicRegionType.CHROMOSOME;
	}

	public HashMap<String, Gene> getGenes() {
		return genes;
	}

	public Chromosome addGene(Gene g) {
		this.genes.put(g.getID(), g);
		this.geneTree.add(g);
		return this;
	}

	public IntervalTree<Gene> getGeneIntervalTree(){
		return this.geneTree;
	}

}
