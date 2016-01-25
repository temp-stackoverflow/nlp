import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Eval {
	
	public static double computeResults(String goldFile, String toolFile) throws IOException {
		String[] gold_data = new String(Files.readAllBytes(Paths.get(goldFile))).split("\\n");
		String[] tool_data = new String(Files.readAllBytes(Paths.get(toolFile))).split("\\n");
		
		int total = 0;
		int tp = 0;
		
		int i = 0;
		int j = 0;
		
		while (i < gold_data.length && j < tool_data.length) {
			gold_data[i] = gold_data[i].trim();
			tool_data[j] = tool_data[j].trim();
			
			String[] gold_tokens = gold_data[i].split("\\s+");
			String[] tool_tokens = tool_data[j].split("\\s+");
			
			int ii = 0;
			int jj = 0;
			
			while (ii < gold_tokens.length && jj < tool_tokens.length) {
				
				String[] goldWordTag = gold_tokens[ii].split("/");
				String goldPosTag = goldWordTag[goldWordTag.length-1];				
				
				String[] toolWordTag = tool_tokens[jj].split("/");
				String toolPosTag = toolWordTag[toolWordTag.length-1];			
				
				total++;
	
				if (goldPosTag.equals(toolPosTag)) {
					tp++;					
				}
				
				ii++;
				jj++;
			}
			
			
			i++;
			j++;
		}
	
		double accuracy = (double)tp/(double)total;
		return accuracy;		
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Usage: data-dir <stanford|stanford-lex|clear|clear-lex>");
			System.exit(1);
		}
		
		Map<Integer, Double> foldAccuracyMap = new HashMap<>();
		
		double overall_accuracy = 0.0;
		double total = 0.0;
		for (int i = 1; i <= 10; i++) {						
			total++;
			double accuracy = computeResults(args[0]+"\\test"+i+".txt", args[0]+"\\result-"+args[1]+i+".txt");
			foldAccuracyMap.put(i, accuracy);
			overall_accuracy += accuracy;
		}
		overall_accuracy = Math.round(((overall_accuracy/total)*100.0)*10.0)/10.0d;
		System.out.println("POS Tagging Accuracy: "+overall_accuracy);
		
	}	

}