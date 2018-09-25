/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plagiarismdetect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import static plagiarismdetect.LanguageUtils.stopWords;

/**
 *
 * @author mussandi
 */
public class Block {
    public int start;
    public int blockSize;
    public Source source;
    public HashSet<String> vocab;
    public Block(String filePath) {
        source = LanguageUtils.files.get(filePath);
        this.start = 0;
        this.blockSize = source.size();
    }
    public Block(String filePath, int start, int blockSize) {
        source = LanguageUtils.files.get(filePath);
        this.start = start;
        this.blockSize = blockSize;
    }
    public Block(Block a, Block b) {
        source = LanguageUtils.files.get(a.source.path);
        
        this.start = a.start < b.start ? a.start : b.start;
        
        this.blockSize = a.blockSize + b.blockSize;
        //this.vocab = (HashSet)a.getVocab().clone();
        //this.vocab.addAll(b.getVocab());
    }
    
    public Block[] segment(int numBlocks) {
        
        Block[] blocks = new Block[numBlocks];
        int numWords = this.blockSize;
        int wordsPerBlock = numWords / numBlocks;
        int remaining = numWords % numBlocks;
        int start = this.start;
        for (int b = 0; b < numBlocks; b++) {
            int blockSize = wordsPerBlock;
            if (remaining > 0) {
                blockSize++;
                remaining--;
            }
            blocks[b] = new Block(this.source.path, start, blockSize);
            
            start += blockSize;
        }
        return blocks;
    }
    public String get(int n) {
        return source.get(start + n);
    }
    public int size() {
        return this.blockSize;
    }
    @Override
    public String toString() {
        return toStringSourceContext();
    }
    public String toStringSourceContext() {
        String str = "|";
        int numChars = 40;
        boolean marked = false;
        for (int i = 0; i < numChars; i++) {
            float spos = ((float)i / numChars) * source.size();
            if((spos > start || i == numChars - 1) && !marked){
                str += "#";
                marked = true;
                continue;
            }
                
            if(spos >= start && spos <= start + blockSize ) {
                str += "#";
                marked = true;
            } else
                str += "-";
            
        }
        return str + "|\n";
    }
    public String toStringComplete() { // used to print this block with extra information
        
        String str = "";
        str += "file: " + this.source.path + " start: " + getFilePosition(this.start) + " size: " + blockSize + "\n";
        
        str += toStringSourceContext();
        
        for(int i = start; i < start + blockSize; i++) {
            str += source.get(i) + " ";
            if(i - start > 100) {
                str += "...";
                break;
            }
        }
//        str += "\nvocab: ";
//        int i = 0;
//        for(String v : getVocab()) {
//            if(i > 100)  {
//                str += "...";
//                break;
//            }
//            str += v + " ";
//            i++;
//        }
        return str + "\n";
    }
    public int getFilePosition(int filteredPosition) {
        
        ArrayList<String> unfilteredWords = source.getUnfiltered();
        int filePosition = 0;
        while(filteredPosition > 0) {
            if(!stopWords.contains(unfilteredWords.get(filePosition))) {
                filteredPosition--;
            }
            filePosition++;
        }
        return filePosition;
    }
    public void trim(Block block) throws IOException {
        HashSet vocS = block.getVocab();
        while (!vocS.contains(get(0))) {
            start++;
            blockSize--;
        }
        while (!vocS.contains(get(blockSize - 1))) {
            blockSize--;
        }
    }
    public void cacheVocab() {
        vocab = getVocab();
    }
            
    public HashSet<String> getVocab() {
        if(vocab != null) return vocab;
        HashSet<String> voc = new HashSet<String>();
//        if (synTranslation) {
//            
//            for(int i = start; i < start + blockSize; i++) {
//                String word = source.get(i);
//
//                if (vocab.contains(word) || word.length() == 0) {
//                    continue;
//                }
//                String syn = LanguageUtils.getLowestSynonym(word);
//                vocab.add(syn);
//            }
//        } else {
            voc.addAll(source.filteredWords.subList(start, start + blockSize));
        return voc;
    }
}
