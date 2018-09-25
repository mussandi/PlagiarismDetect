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
public class DetectBlocksTernary {

    //static boolean DEBUG= false;

    private static String analyzeBlock(Block blockF, Block blockS, int depth) throws IOException {
        String log = "";
        //log += blockF;
        
        //String suspSentence = LanguageTool.fileToParagraphs(suspeitoFile, true); //melhorar
        if ((blockF.size()) / 3 < blockS.size() && depth > 0) {
            //String res = LinearComparetor.checkSequentialFonte(blockS, blockF, 4);
            // if(res.length() > 0) System.out.println("Check sequential result: " + res);

            //System.out.println(suspSentence); //para imprimir cada frase suspeita e o result do bloco.
            blockF.trim(blockS);
            log += "fonte:";
            log += blockF.toStringComplete();
        } else {
            Block[] blocks = blockF.segment(3);
            
            Block blockAB = new Block(blocks[0], blocks[1]);

            Block blockBC = new Block(blocks[1], blocks[2]);
            //log += "" + blocks[0] + blocks[1] + blocks[2];

//            double resultAB = LanguageUtils.metricSimilarity(blockS.getVocab(), blockAB.getVocab());
            double resultAB = LanguageUtils.metricSimilarity(blockS.getVocab(), blockAB.getVocab());
            double resultBC = LanguageUtils.metricSimilarity(blockS.getVocab(), blockBC.getVocab());
            if (resultAB >= resultBC) {
                if (resultAB > 0.7) {
                    log += analyzeBlock(blockAB, blockS, depth + 1);
                }
            }
            else {
                if (resultBC > 0.7) {

                    log += analyzeBlock(blockBC, blockS, depth + 1);
                }
            }
        }
        return log;
    }

    public static String metricsFiles(Block blockS) throws IOException {
        String log = ("\nSuspeito: ");
        log += blockS.toStringComplete() + "\n";

        for (String path : LanguageUtils.files.keySet()) {
            if(path.equals(blockS.source.path)) continue;
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
        
//        Source s = new Source("test", "testwordA testwordb testwordc testwordd\ntestworde\ntestwordF");
//        Block block = new Block("test", 1, 3);
//        System.out.println(block);
//        System.out.println(s.paragraphBlocks.get(0));
//        System.out.println(s.paragraphBlocks.get(1));
//        System.out.println(s.paragraphBlocks.get(2));
//        
//        Block[] segments = s.paragraphBlocks.get(0).segment(2);
//        System.out.println(segments[0]);
//        System.out.println(segments[1]);
//        if(true) return;
//        String suspeitoFile = "/Users/mussandi/Documents/SourcePAN12/DezBlocosPAN12.txt";
//        LanguageUtils.loadFiles("/Users/mussandi/Documents/SourcePAN12/", -1, suspeitoFile);

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
        DetectTernaryThread[] thrs = new DetectTernaryThread[paragraphs.size()];
        for (int i = 0; i < paragraphs.size(); i++) {
            Block block = paragraphs.get(i);
            block.cacheVocab();
            thrs[i] = new DetectTernaryThread(block);
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
