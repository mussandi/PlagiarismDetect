/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plagiarismdetect;

import java.util.ArrayList;
import java.util.Arrays;
import static plagiarismdetect.LanguageUtils.readFile;

/**
 *
 * @author mussandi
 */
public class Source {
    String path;
    ArrayList<String> filteredWords;
    ArrayList<Block> paragraphBlocks;
    ArrayList<String> unfilteredWords;
    public Source(String path, String text) {
        unfilteredWords = new ArrayList<String>();
        this.path = path;
        LanguageUtils.files.put(path, this);
        load(text);
    }
    public Source(String path) {
        this.path = path;
        LanguageUtils.files.put(path, this);
        load(readFile(path));
    }
    public void load(String text) {
        text = text.toLowerCase();
        filteredWords = new ArrayList<String>();
        paragraphBlocks = new ArrayList<Block>();
        
        String[] paragraphs = text.split("[\n\r]+");
        
        int start = 0;
        for(String paragraph : paragraphs) {
            String[] words = paragraph.split("[ [0-9]?!:,.!;…\n\r()\"'\\*-]+");
            ArrayList<String> pwords = new ArrayList<String>(Arrays.asList(words));
            
            if(unfilteredWords != null) unfilteredWords.addAll(pwords);
            
            pwords = LanguageUtils.filterStopWords(pwords);
            paragraphBlocks.add(new Block(path, start, pwords.size()));
            start += pwords.size();
            filteredWords.addAll(pwords);
        }
    }
    public int size() {
        return filteredWords.size();
    }
    public String get(int n) {
        return filteredWords.get(n);
    }
    public ArrayList getUnfiltered() {
         return unfilteredWords != null ? unfilteredWords :
                 LanguageUtils.fileToList(path, false);
    }
}
