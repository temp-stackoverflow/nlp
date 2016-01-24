import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class EvalTextSegmentation {
	
	public static Map<Integer, Double> foldTokensTP = new HashMap<>();
	public static Map<Integer, Double> foldTokensFP = new HashMap<>();
	public static Map<Integer, Double> foldTokensTotal = new HashMap<>();
	
	public static Map<Integer, Double> foldSentenceTP = new HashMap<>();
	public static Map<Integer, Double> foldSentenceFP = new HashMap<>();
	public static Map<Integer, Double> foldSentenceTotal = new HashMap<>();
	
	public static void init() {
		foldTokensTP = new HashMap<>();
		foldTokensFP = new HashMap<>();
		foldTokensTotal = new HashMap<>();
		
		foldSentenceTP = new HashMap<>();
		foldSentenceFP = new HashMap<>();
		foldSentenceTotal = new HashMap<>();
	}
	
	public static void computeFscore(Map<Integer, Double> foldTP, Map<Integer, Double> foldFP, Map<Integer, Double> foldTotal) {
		double recall = 0.0;
		double precision = 0.0;
		for (int i = 1; i <= 10; i++) {
			recall += (foldTP.get(i)/foldTotal.get(i));
			precision += (foldTP.get(i)/(foldTP.get(i)+foldFP.get(i)));
		}
		recall = Math.round(((recall/10.0)*100.0)*10.0)/10.0d;
		precision = Math.round(((precision/10.0)*100.0)*10.0)/10.0d;
		double fscore = Math.round(((2.0*recall*precision)/(recall+precision))*10.0)/10.0d;
		
		System.out.println("P: "+precision+" R: "+recall+" F: "+fscore);
	}
	
	public static void computeTotals(String goldFile, String resultFile, int fold) throws IOException {
		
		String[] goldLines = new String(Files.readAllBytes(Paths.get(goldFile))).split("\\n");
		String[] resultLines = new String(Files.readAllBytes(Paths.get(resultFile))).split("\\n");
		
		double tokensTP = 0;
		double tokensFP = 0;
		double tokensTotal = 0;
		
		double sentenceTP = 0;
		double sentenceFP = 0;
		double sentenceTotal = 0;
		
		for (int i = 0; i < goldLines.length; i++) {
						
			String[] goldTokens = goldLines[i].trim().split("\\s+");
			String goldSentenceTag = goldTokens[goldTokens.length-1];
			String result = resultLines[i].trim();
			
			if (goldSentenceTag.matches(".*?/")) {
				sentenceTotal++;
				if (result.matches(".*?/")) {
					sentenceTP++;
				}
			}
			else if (result.matches(".*?/")) {
				sentenceFP++;
			}				
			
			if (goldLines[i].trim().equals("")) {
				continue;
			}			
			
			if (goldSentenceTag.matches("B.*")) {
				tokensTotal++;
				boolean resultMatch = result.matches("B.*");
				i++;
				if (i >= goldLines.length || goldLines[i].trim().equals("")) {
					if (resultMatch) {
						tokensTP++;
					}
					else {
						tokensFP++;
					}
					continue;
				}
				
				goldTokens = goldLines[i].trim().split("\\s+");				
				goldSentenceTag = goldTokens[goldTokens.length-1];
				result = resultLines[i].trim();
								
				while (goldSentenceTag.matches("I.*")) {
					if (result.matches("B.*") || result.matches("O.*")) {
						tokensFP++;
						resultMatch = false;
					}
					i++;
					if (i >= goldLines.length || goldLines[i].trim().equals("")) {
						break;
					}
					
					goldTokens = goldLines[i].trim().split("\\s+");					
					goldSentenceTag = goldTokens[goldTokens.length-1];
					result = resultLines[i].trim();					
				}
				
				if (resultMatch) {
					tokensTP++;
				}
			}
			else if (goldSentenceTag.matches("O.*")) {
				tokensTotal++;
				if (result.matches("O.*")) {
					tokensTP++;
				}
				else if (result.matches("B.*")) {
					tokensFP++;
				}
			}
			else if (result.matches("B.*")) {
				tokensFP++;
			}
		}
				
		foldTokensTP.put(fold, tokensTP);
		foldTokensFP.put(fold, tokensFP);
		foldTokensTotal.put(fold, tokensTotal);
		
		foldSentenceTP.put(fold, sentenceTP);
		foldSentenceFP.put(fold, sentenceFP);
		foldSentenceTotal.put(fold, sentenceTotal);		
	}	
	
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Usage: data-dir");
			System.exit(1);			
		}
		
		for (int i = 1; i <= 10; i++) {
			computeTotals(args[0]+"\\test"+i+".txt", args[0]+"\\result"+i+".txt", i);
		}
		
		System.out.println("Tokenization Results: ");
		computeFscore(foldTokensTP, foldTokensFP, foldTokensTotal);
		System.out.println("\nSentence Splitting Results: ");
		computeFscore(foldSentenceTP, foldSentenceFP, foldSentenceTotal);
		
		init();

		for (int i = 1; i <= 10; i++) {
			computeTotals(args[0]+"\\test"+i+".txt", args[0]+"\\result-meta"+i+".txt", i);			
		}		

		System.out.println("\nSentence Splitting Results (+code tags): ");
		computeFscore(foldSentenceTP, foldSentenceFP, foldSentenceTotal);		
		
	}		

}