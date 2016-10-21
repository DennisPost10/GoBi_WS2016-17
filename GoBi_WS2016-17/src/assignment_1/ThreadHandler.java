package assignment_1;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import debugStuff.DebugMessageFactory;
import thread.CommerceRunner;
import thread.Consumable;
import thread.SimpleProducer;

public class ThreadHandler {

	private int THREAD_AMOUNT;
	private long FILELENGTH;
	private GenomeAnnotation genomeAnnotation;
	private ArrayList<Thread> threadList;
	
	public ThreadHandler(int amountThreads){
		this.genomeAnnotation = new GenomeAnnotation();
		this.threadList = new ArrayList<>();
		this.THREAD_AMOUNT = amountThreads;
	}
	
	public ThreadHandler(){
		this(Runtime.getRuntime().availableProcessors());
	}
	
	public void startThreads(String filepath){
		
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			
			int amountLines = countLines(filepath);
			
			int part = amountLines/THREAD_AMOUNT;
			
			String line = null;
			ArrayList<String> currentLines = new ArrayList<>();
			int countLines = 0;
			int countLinesGlobal = 0;
			
			while((line = br.readLine()) != null){
				
				countLines += 1;
				countLinesGlobal += 1;
				
//				printCurrentReadInProgress(countLinesGlobal, amountLines);
				
				if(countLines == part){
					
					countLines = 0;
					
					addAndStartThread(currentLines);
					
					currentLines = new ArrayList<>();
				}
				
				currentLines.add(line);
			}
			
			br.close();
			
			waitForThreads();
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	private void addAndStartThread(ArrayList<String> lines){
		this.threadList.add(new Thread(new GTF_Parser(this.threadList.size()+1+"", lines, this.genomeAnnotation)));
		this.threadList.get(this.threadList.size()-1).start();
	}
	
	private void printCurrentReadInProgress(long currentByte, long amountBytes){
		double currentProgress = (double)currentByte/amountBytes*100;
		double roundedProgress = Math.round(currentProgress*100.0)/100.0;
		
		DebugMessageFactory.printInfoDebugMessage(true, "READ IN PROGRESS: "+roundedProgress+" %");
	}
	
	public static int countLines(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
	
	public void waitForThreads(){
		for (Thread t : this.threadList){
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args) {
		
		ThreadHandler th = new ThreadHandler(1);
		
		long startTime = System.currentTimeMillis();
		
//		th.startThreads("/home/proj/biosoft/praktikum/genprakt-ws16/gtf/Saccharomyces_cerevisiae.R64-1-1.75.gtf");
//		th.startThreads("/home/proj/biosoft/praktikum/genprakt-ws16/gtf/Mus_musculus.GRCm38.75.gtf");
//		th.startThreads("/usr/local/storage/GOBI/gtf/gtf/Homo_sapiens.GRCh37.75.gtf");
		th.startThreads("/usr/local/storage/GOBI/gtf/gtf/Mus_musculus.GRCm38.75.gtf");
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("[NEEDED TIME] "+(endTime-startTime)+" milliseconds");
		
		System.out.println("Chromosomes:\t"+th.genomeAnnotation.getAmountChromsomes());
		System.out.println("Genes:\t\t"+th.genomeAnnotation.getAmountGenes());
		System.out.println("Exons:\t\t"+th.genomeAnnotation.getAmountExons());
		System.out.println("Transcripte:\t"+th.genomeAnnotation.getAmountTranscripts());
		
	}
}
