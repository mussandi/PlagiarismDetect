/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plagiarismdetect;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import javafx.util.Pair;

/**
 *
 * @author mussandi
 */
public class LinearComparetor {

    static ArrayList<Pair<String, ArrayList<String>>> files = new ArrayList<Pair<String, ArrayList<String>>>();


    public static String checkSequentialFonte(Block fonte, Block suspeito, int min) {
              
        String result = "";
        for (int si = 0; si < suspeito.size(); si++) {
            int count = 0;
            for (int fi = 0; fi < fonte.size(); fi++) {
                if (suspeito.get(si).equals(fonte.get(fi))) {
                    for (count = 1; fi + count < fonte.size()
                            && si + count < suspeito.size(); count++) {
                        if (!suspeito.get(si + count).equals(fonte.get(fi + count))) {
                            break;
                        }
                    }
                    if (count >= min) {
                        result += new Block(fonte.source.path, fi, count).toStringComplete();
                        si += count - 1;
                        break;
                    }

                }

            }

        }
        return result;
    }

    public static String checkSequential(Block blockS, int min) throws IOException {
        String log = ("\nSuspeito: ");
        log += blockS.toStringComplete() + "\n";

        for (String path : LanguageUtils.files.keySet()) {
            if (path.equals(blockS.source.path)) {
                continue;
            }
            log += checkSequentialFonte(new Block(path), blockS, min);
        }
        return log;
    }

    public static void main(String[] args) throws IOException {
        
        String suspeitoFile = "/Users/mussandi/Documents/testes/SourcePAN12/DezBlocosPAN12.txt";
        
        LanguageUtils.loadFiles("/Users/mussandi/Documents/testes/SourcePAN12", -1, null);
        //loadFiles("/Users/mussandi/Documents/SourcePAN12", suspeitoFile);
        
        System.out.println("");
        System.out.println("Starting sentenc alignment...");
        Block suspeito = new Block(suspeitoFile);
        long start = System.currentTimeMillis();

        String result = checkSequential(suspeito, 5);

        long elapsed = System.currentTimeMillis() - start;

        System.out.println(result);
        System.out.println("doned!");
        System.out.println("Execution time: " + elapsed + " Millis");

    }
}
