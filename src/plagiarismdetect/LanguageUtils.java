/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plagiarismdetect;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

/**
 *
 * @author mussandi
 */
public class LanguageUtils {

    static String similarity;
    static IDictionary dict;
    static String cacheDir = "/Users/mussandi/Documents/SourcePAN12/.cache";
    static HashSet<String> stopWords;
    
    public static Hashtable<String, Source> files = new Hashtable<String, Source>();

    static {
        try {
            String path = "/Users/mussandi" + File.separator + "dict";// the path of dictionary extrated folder.
            URL url = new URL("file", null, path);

            dict = new Dictionary(url);
            dict.open();

            String stopWordsFile = "/Users/mussandi/stopwords/stopwords_en.txt";

            LanguageUtils.stopWords = arrayToVocab(null, new ArrayList<String>(Arrays.asList(fileToArray(stopWordsFile))), false, false);
        } catch (MalformedURLException ex) {
            Logger.getLogger(LanguageUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LanguageUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void loadFiles(String dir, int maxFiles, String ignore) throws IOException {

        System.out.println("Loading sources at dir " + dir);
        final File folder = new File(dir);
        for (final File fileEntry : folder.listFiles()) {
            if(maxFiles == files.size()) break;
            if (fileEntry.getName().charAt(0) == '.') {
                continue;
            }
            if (fileEntry.isDirectory()) {
                loadFiles(fileEntry.getPath(), maxFiles, ignore);
            } else {
                if (fileEntry.getPath().equals(ignore)) {
                    continue;
                }
                new Source(fileEntry.getPath());
                
            }
        }
        System.out.println("Done loading sources at dir " + dir);
    }
    
    
    public static String getLowestSynonym(String str) {
        String result = str;
        for (IWord word : getSynonymousAux(str)) {
            ISynset synset = word.getSynset();
            for (IWord isyn : synset.getWords()) {
                String syn = isyn.getLemma();
                if (result.compareTo(syn) > 0) {
                    result = syn;
                }
            }
        }
        return result;
    }

    public static ArrayList<String> getSynonymous(String str) {
        ArrayList<String> syns = new ArrayList<String>();
        for (IWord iword : getSynonymousAux(str)) {
            ISynset synset = iword.getSynset();
            for (IWord syn : synset.getWords()) {
                syns.add(syn.getLemma());
            }
        }
        return syns;
    }

    public static boolean isSynonymousOrEqual(String str1, String str2) {
        if (str1.equals(str2)) {
            return true;
        }
        for (IWord word : getSynonymousAux(str1)) {
            ISynset synset = word.getSynset();
            for (IWord syn : synset.getWords()) {
                if (str2.equals(syn.getLemma())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String blockToString(ArrayList<String> block) {
        String str = "";
        for (String word : block) {
            if (word.length() > 0) {
                str += (word + " ");
            }
        }
        str += ("\n");
        return str;
    }

    public static ArrayList<IWord> getSynonymousAux(String str) {
        ArrayList<IWord> words = new ArrayList<IWord>();
        POS[] types = {POS.NOUN, POS.ADJECTIVE, POS.VERB, POS.ADVERB};
        for (POS pos : types) {
            IIndexWord idxWord = dict.getIndexWord(str, pos);
            if (idxWord != null) {
                for (IWordID wordID : idxWord.getWordIDs()) {
                    words.add(dict.getWord(wordID));
                }
            }
        }
        return words;
    }

    public static String cacheName(String name) {
        String[] path = name.split("/");
        return cacheDir + "/" + path[path.length - 1];
    }

    public static HashSet<String> getIntersection0(HashSet<String> a, HashSet<String> b) {
        HashSet inter = (HashSet) a.clone();
        inter.retainAll(b);
        return inter;
    }

    public static HashSet<String> getIntersection(HashSet<String> a, HashSet<String> b) {

        HashSet<String> inter = new HashSet<String>();
        HashSet<String> menor;
        HashSet<String> maior;
        if (a.size() < b.size()) {
            menor = a;
            maior = b;
        } else {
            menor = b;
            maior = a;
        }

        for (String word : menor) {
            if (maior.contains(word)) {
                inter.add(word);
            }
        }
        return inter;
    }

    public static ArrayList<Pair<Integer, String[]>> fileToSentecesME(String fileName) throws FileNotFoundException, IOException {
        ArrayList<Pair<Integer, String[]>> result = new ArrayList<Pair<Integer, String[]>>();
        String file = readFile(fileName);
        InputStream inputStream = new FileInputStream("/Users/mussandi/library-java/en-sent.bin");
        SentenceModel model = new SentenceModel(inputStream);

        SentenceDetectorME detector = new SentenceDetectorME(model);
        Span sentences[] = detector.sentPosDetect(file);
        for (Span sent : sentences) {
            String[] words = file.substring(sent.getStart(), sent.getEnd()).split("[: ,.!;…\n\r()]+");
            result.add(new Pair<Integer, String[]>(sent.getStart(), words));
        }

        return result;
    }
    public static ArrayList<String> fileToList(String suspFile, boolean filterSW) {
        ArrayList<String> wordsList = new ArrayList<String>(Arrays.asList(fileToArray(suspFile)));
        if (filterSW) {
            wordsList = filterStopWords(wordsList);
        }
        return wordsList;
    }

    public static double metricSimilarity(HashSet<String> vocabSuspeito, HashSet<String> vocabFonte) {

        return (double) getIntersection(vocabSuspeito, vocabFonte).size() / (double) vocabSuspeito.size();

    }

    public static double jaccardSimilarity(HashSet<String> suspeito, HashSet<String> fonte) {
        HashSet vocabI = getIntersection0(suspeito, fonte);
        HashSet vocabU = fonte;
        vocabU.addAll(suspeito);

        return (double) vocabI.size() / (double) vocabU.size();
    }


    public static String[] readCache(String cache) {
        String content = readFile(cache);
        if (content == null) {
            return null;
        }
        return content.split(" ");
    }

    public static HashSet arrayToVocab(String outputName, ArrayList<String> array, boolean synTranslation, boolean filterStopWords) throws IOException {
        File fcacheDir;
        FileWriter fw;
        BufferedWriter bw = null;
        HashSet voc = new HashSet();
        if (synTranslation) {
            if (outputName != null) {
                String[] cache = readCache(cacheName(outputName));
                if (cache != null) {
                    voc.addAll(Arrays.asList(cache));
                    return voc;
                }
                fcacheDir = new File(cacheDir);
                if (!fcacheDir.exists()) {
                    fcacheDir.mkdir();
                }

                fw = new FileWriter(cacheName(outputName));
                bw = new BufferedWriter(fw);
            }
            for (String word : array) {
                if (word.length() == 0) {
                    continue;
                }
                if (voc.contains(word) || (filterStopWords && LanguageUtils.stopWords.contains(word))) {
                    continue;
                }
                String syn = LanguageUtils.getLowestSynonym(word);
                voc.add(syn);
                if (outputName != null) {
                    bw.write(" " + syn);
                }
            }
            if (outputName != null) {
                bw.close();
            }
        } else {
            voc.addAll(array);
            if (filterStopWords) {
                voc.removeAll(LanguageUtils.stopWords);
            }
        }
        return voc;
    }

    public static String readFile(String caminho) {

        //Scanner text = new Scanner(System.in);
        String readfile = null;
        Path path = Paths.get(caminho);
        try {
            byte[] textread = Files.readAllBytes(path);
            readfile = new String(textread);
            //System.out.println(readfile);
        } catch (Exception e) {
            System.out.println("file not found! " + caminho);
        }

        return readfile;
    }

    public static ArrayList<String> filterStopWords(ArrayList<String> words) {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            if (word.length() > 0 && !LanguageUtils.stopWords.contains(word)) {
                result.add(word);
            }
        }
        return result;
    }

    public static String[] fileToArray(String file) {
        return readFile(file).toLowerCase().split("[ [0-9]?!:,.!;…\n\r()]+");
    }
}
