import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Eval {
	
	public static Map<Integer, Double> foldTP = new HashMap<>();
	public static Map<Integer, Double> foldTotal = new HashMap<>();
	public static Map<Integer, Double> foldTotalPredicted = new HashMap<>();	
	
	public static double tp = 0;
	public static double total = 0;
	public static double totalPredicted = 0;	
	
	public static void computeFscore() {
		double recall = 0.0;
		double precision = 0.0;
		for (int i = 1; i <= 10; i++) {
			recall += (foldTP.get(i)/foldTotal.get(i));
			precision += (foldTP.get(i)/foldTotalPredicted.get(i));
		}
		recall = Math.round(((recall/10.0)*100.0)*10.0)/10.0d;
		precision = Math.round(((precision/10.0)*100.0)*10.0)/10.0d;
		double fscore = Math.round(((2.0*recall*precision)/(recall+precision))*10.0)/10.0d;

		System.out.println("P: "+precision+" R: "+recall+" F: "+fscore);
	}	
	
	public static String normalizeWord(String word) {
		String normalizedWord = "";
		char[] wordCharArr = word.toCharArray();
		
		int i = 0;
		
		while (i < wordCharArr.length) {
			if (wordCharArr[i] < 0x00 || wordCharArr[i] > 0x7f) {
				normalizedWord += "x";
			}
			else {
				normalizedWord += wordCharArr[i];
			}
			i++;
		}
		
		return normalizedWord;
	}
	
	public static int[] getAlignedIndexes(String[] gold, int i, String[] system, int j) throws IOException {
		int[] indexes = new int[2];

		String gString = "";
		String sString = "";
		
		while (i < gold.length && j < system.length) {
			
			gold[i] = gold[i].trim();
			String[] gTok = gold[i].split("\t");			
			String gPos = gTok[1];			
			String gWord = normalizeWord(gTok[0]);
			
			system[j] = system[j].trim();
			String[] sTok = system[j].split("\t");
			String sPos = sTok[1];
			String sWord = normalizeWord(sTok[0]);
			
			if (sString.equals("") && gString.equals("")) {				
				sString = sWord;
				gString = gWord;
				
				if (sPos.equals(gPos)) {
					tp++;
				}				
				i++;
				j++;
				continue;
			}			
			else if (sString.equals(gString)) {
				--i;
				indexes[0] = i;
				--j;
				indexes[1] = j;	
				return indexes;
			}
		
			if (sString.equals(gString+gWord)) {
				i++;
				gString += gWord;
			}
			else if (sString.length() > gString.length()+gWord.length() && sString.substring(0, gString.length()+gWord.length()).equals(gString+gWord)) {
				gString += gWord;
				i++;
			}
			else if (gString.equals(sString+sWord)) {				
				j++;
				sString += sWord;			
				
				if (sPos.equals(gPos)) {
					tp++;
				}
				
			}
			else if (gString.length() > sString.length()+sWord.length() && gString.substring(0, sString.length()+sWord.length()).equals(sString+sWord)) {		
				sString += sWord;
				j++;			
				
				if (sPos.equals(gPos)) {
					tp++;
				}				
			}
			else {				
				gString += gWord;
				sString += sWord;
				i++;
				j++;	
				
				if (sPos.equals(gPos)) {
					tp++;
				}				
			}
						
		}
		
		indexes[0] = i;
		indexes[1] = j;
		
		return indexes;
	}	
	
	public static void computeTP(String[] gold, String[] system, int fold) throws IOException {
		int i = 0;
		int j = 0;
		
		tp = 0;
		
		while (i < gold.length && j < system.length) {						
			gold[i] = gold[i].trim();
			if (gold[i].equals("")) {
				i++;
				continue;
			}
			String[] gTok = gold[i].split("\t");
			String gWord = normalizeWord(gTok[0]);				
			String gPos = gTok[1];

			system[j] = system[j].trim();
			if (system[j].equals("")) {				
				j++;
				continue;
			}
			String[] sTok = system[j].split("\t");
			String sWord = normalizeWord(sTok[0]);
			String sPos = sTok[1];
			
			if (gWord.equals(sWord) || ((gWord.charAt(0) == '\'' || gWord.charAt(0) == '\"') && gWord.contains("_") && gWord.replaceAll("_", "").equals(sWord))) {
				if (gPos.equals(sPos)) {
					tp++;
				}
			}
			else {
				int[] alignedIndexes = getAlignedIndexes(gold, i, system, j);
				i = alignedIndexes[0];
				j = alignedIndexes[1];
			}
			
			i++;
			j++;			
		}	
		
		foldTP.put(fold, tp);	
	}	
	
	public static void computeTotals(String goldFile, String resultFile, int fold) throws IOException {		
		total = 0;
		totalPredicted = 0;		
		
		String[] goldLines = new String(Files.readAllBytes(Paths.get(goldFile))).split("\\n");
		total += goldLines.length;
		String[] resultLines = new String(Files.readAllBytes(Paths.get(resultFile))).split("\\n");
		totalPredicted += resultLines.length;	
		
		computeTP(goldLines, resultLines, fold);
		foldTotal.put(fold, total);
		foldTotalPredicted.put(fold, totalPredicted);		
	}		

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Usage: data-dir extension");
			System.exit(1);
		}
		
		for (int i = 1; i <= 10; i++) {
			computeTotals(args[0]+"\\test-sst"+i+".txt", args[0]+"\\result-sst-"+args[1]+i+".txt", i);
		}
		
		computeFscore();
	}
	
}