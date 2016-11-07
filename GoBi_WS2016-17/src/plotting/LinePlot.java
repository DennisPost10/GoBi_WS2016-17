package plotting;

import java.io.File;
import java.util.*;

import debugStuff.DebugMessageFactory;
import io.AllroundFileWriter;
import io.ConfigReader;
import io.TemporaryFile;
import javafx.util.Pair;

public class LinePlot extends Plot{

	Vector<Vector<Object>> x;
	Vector<Vector<Object>> y;
	
	Vector<Object> legendLabels;
	
	int maxX, maxY;
	
	public LinePlot(Pair<Vector<Vector<Object>>,Vector<Vector<Object>>> pair, String title, String xLab, String yLab, int maxX, int maxY){
		
		this.x = pair.getKey();
		this.y = pair.getValue();
		
		this.maxX = maxX;
		this.maxY = maxY;
		
		setTitle(title);
		setXLab(xLab);
		setYLab(yLab);
		
		legendLabels = new Vector<>();
		
		for (int i = 0; i < x.size(); i++) {
			legendLabels.add(String.valueOf(i));
		}
	}
	
	public void plot(){
		RExecutor r = new RExecutor(generateCommand(ConfigReader.readConfig().get("output_directory")+this.title+".png"));
		
		Thread t = new Thread(r);
		t.start();
		
		try {
			DebugMessageFactory.printNormalDebugMessage(true, "Wait for R to plot..");
			t.join();
			DebugMessageFactory.printNormalDebugMessage(true, "R thread terminated.");
			
		} catch (InterruptedException e) {
			throw new RuntimeException("R did not exit properly!");
		}
		
	}
	
	public void addLegendVector(Vector<Object> vector){
		this.legendLabels = vector;
	}
	
	@Override
	public String generateCommand(String filename) {
		
		File tmp = TemporaryFile.createTempFile();
		
		AllroundFileWriter.writeVector(x.get(0), tmp);
		AllroundFileWriter.writeVector(y.get(0), tmp, true);
		
		for (int i = 1; i < x.size(); i++) {
			AllroundFileWriter.writeVector(x.get(i), tmp, true);
			AllroundFileWriter.writeVector(y.get(i), tmp, true);
		}
		
		AllroundFileWriter.writeVector(this.legendLabels, tmp, true);
		
		String command = "";
		command += String.format("png(\"%s\",width=3.25,height=3.25,units=\"in\",res=400,pointsize=4);", filename);
		command += String.format("x<-scan(\"%s\",nlines=1,skip=0);", tmp);
		command += String.format("y<-scan(\"%s\",nlines=1,skip=1);", tmp);
		command += String.format("plot(x,y,ann=F,type=\"l\",xlim=range(0:"+maxX+"),ylim=range(0:"+maxY+"),col=1);");
		
		int counter = 2;
		
		for (int i = 1; i < x.size(); i++) {
			command += String.format("x"+i+"<-scan(\"%s\",nlines=1,skip="+(counter)+");", tmp);
			counter += 1;
			command += String.format("y"+i+"<-scan(\"%s\",nlines=1,skip="+(counter)+");", tmp);
			counter += 1;
			command += String.format("lines(x"+i+",y"+i+",type=\"l\",col="+(i+1)+");", tmp);
			
		}
		
		command += String.format("ll<-scan(\"%s\",nlines=1,skip="+(counter)+",what=character());", tmp);
		command += String.format("legend(\"bottomright\", legend=ll, col=1:"+x.size()+", lty=c(1:1));");
		command += String.format("title(main=\"%s\", xlab=\"%s\", ylab=\"%s\");", super.title, super.xLab, super.yLab);
		command += "dev.off();";
		
		return command;
		
	}
}