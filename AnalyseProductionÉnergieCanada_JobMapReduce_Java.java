// Analyse de la production moyenne des énergies renouvelables et non renouvelables au Canada entre 2011-2015 

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.*;
import org.apache.log4j.Logger;

public class MoyProduction extends Configured implements Tool {

  private static final Logger LOG = Logger.getLogger(MoyProduction.class);

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new MoyProduction(), args);
    System.exit(res);
  }

  public int run(String[] args) throws Exception {
    Job job = Job.getInstance(getConf(), "MoyProduction");
    job.setJarByClass(this.getClass());
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    job.setMapperClass(Map.class);
    job.setReducerClass(Reduce.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(DoubleWritable.class);
    return job.waitForCompletion(true) ? 0 : 1;
  }

  public static class Map extends Mapper<LongWritable, Text, Text, DoubleWritable> {
	private DoubleWritable Donnee = new DoubleWritable();
    private int Annee;
    private Text Sortie ;   
    

    public void map(LongWritable offset, Text lineText, Context context) throws IOException, InterruptedException {
    	System.out.println ("entree mapper " + offset);
    	String line = lineText.toString();
	    String [] prod = line.split(",");

        
    	if (lineText.toString().contains("Region") || lineText.toString().contains(",,,,")){
            return;
    	}
    	
        System.out.println (prod[0]+""+ prod[1]+""+ prod[2]+""+ prod[3]);   	

	    Sortie = new Text (prod[0]+" "+ prod[1]);
	    Annee = Integer.parseInt(prod[2]);
	    Donnee = new DoubleWritable(new Double(prod[3]));
	    
	    if (Annee >=2011 &&  Annee <=2015){
		    context.write(Sortie,Donnee);
	    }
	    System.out.println ("sortie mapper");
    }
  }

    public static class Reduce extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
	  
     public void reduce(Text cle, Iterable<DoubleWritable> counts, Context context)   throws IOException, InterruptedException {
    	 System.out.println ("entree reducer");
    	int nombre = 0;
    	double sum = 0;
    	double moy = 0;
      for (DoubleWritable count : counts) {
        System.out.println (cle);
      	System.out.println (count.get());
        sum += count.get();
        nombre ++;
      }
      moy=sum/nombre;
      context.write(cle, new DoubleWritable(moy));
      
      System.out.println ("sortie reducer");
    }
  }

}
