/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plagiarismdetect;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 *
 * @author mussandi
 */
public class DetectBlocksBinary {

    //static boolean DEBUG= false;
    static ArrayList<Pair<String, ArrayList<String>>> files = new ArrayList<Pair<String, ArrayList<String>>>();

    private static String analyzeBlock(Block blockF, Block blockS, int depth) throws IOException {
        String log = "";
        //log += blockF;
        int ctr = 0;

        //String suspSentence = LanguageTool.fileToParagraphs(suspeitoFile, true); //melhorar
        if ((blockF.size()) / 2 < blockS.size() && depth > 0) {
            //String res = LinearComparetor.checkSequentialFonte(blockS, blockF, 4);
            // if(res.length() > 0) System.out.println("Check sequential result: " + res);

            //System.out.println(suspSentence); //para imprimir cada frase suspeita e o result do bloco.
            blockF.trim(blockS);
            log += "fonte:";

            log += blockF.toStringComplete();
        } else {
            Block[] blocks = blockF.segment(2);
            //log += "" + blocks[0] + blocks[1];

            double resultAB = LanguageUtils.jaccardSimilarity(blockS.getVocab(), blocks[0].getVocab());
            double resultBC = LanguageUtils.jaccardSimilarity(blockS.getVocab(), blocks[1].getVocab());
            if (resultAB >= resultBC) {
                if (resultAB > 0.03) {
                    log += analyzeBlock(blocks[0], blockS, depth + 1);
                    
                }
            } else {
                if (resultBC > 0.03) {

                    log += analyzeBlock(blocks[1], blockS, depth + 1);
                }
            }
        }
        return log;
    }

    public static String metricsFiles(Block blockS) throws IOException {
        String log = ("\nSuspeito: ");
        log += blockS.toStringComplete() + "\n";

        for (String path : LanguageUtils.files.keySet()) {
            if (path.equals(blockS.source.path)) {
                continue;
            }
            log += analyzeBlock(new Block(path), blockS, 0);
        }
        return log;
    }

    public static long timeCalculator(long start) {

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Execution time: " + elapsed + " Millis");

        return elapsed;

    }

    public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException {
        int ctrCases = 0;
//        String suspeitoFile = "/Users/mussandi/Documents/testes/SourcePAN12Time/DezBlocosPAN12.txt";// for one file on the directory
//        LanguageUtils.loadFiles("/Users/mussandi/Documents/testes/SourcePAN12Time", -1, null); // for one file on the directory

        String suspeitoFile = "/Users/mussandi/Documents/testes/SourcePAN12/DezBlocosPAN12.txt";
        LanguageUtils.loadFiles("/Users/mussandi/Documents/testes/SourcePAN12", -1, null);
        System.out.println(suspeitoFile);

        //metricsFiles(suspeito);
        ArrayList<Block> paragraphs = LanguageUtils.files.get(suspeitoFile).paragraphBlocks;

        long start = System.currentTimeMillis();

//        String log = "";
//        for (Pair<Integer, String[]> paragraph : paragraphs) {
//            log += metricsFiles(new ArrayList<String>(Arrays.asList(paragraph.getValue())));
//        }
        DetectBinaryThread[] thrs = new DetectBinaryThread[paragraphs.size()];
        for (int i = 0; i < paragraphs.size(); i++) {
            Block block = paragraphs.get(i);
            block.cacheVocab();
            thrs[i] = new DetectBinaryThread(block);
            thrs[i].start();
        }
        for (int i = 0; i < paragraphs.size(); i++) {
            thrs[i].join();
        }
        timeCalculator(start);

        //if (DEBUG)  System.out.println(log);
        //System.out.println(log);
        for (int i = 0; i < paragraphs.size(); i++) {
            thrs[i].printLog();
            ctrCases += 1;

        }
        System.out.println("---------------------------------------- ");
        System.out.println("There are " + ctrCases + " suspecius cases ");
        System.out.println("---------------------------------------- ");
    }

}
