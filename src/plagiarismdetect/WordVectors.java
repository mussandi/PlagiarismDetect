
package plagiarismdetect;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;
import javafx.util.Pair;


/**
 *
 * @author mussandi
 */
public class WordVectors {
    public class Vec {
        public float[] coordinates;
        public int numWords;
        
        // load vec from plain text file
        public Vec(Scanner src) {
            numWords = 1;
            coordinates = new float[300];
            for(int i = 0; i < 300; i++)
                coordinates[i] = src.nextFloat(); 
        }
        
        // load vec from binary file
        public Vec(DataInputStream src) throws IOException {
            numWords = 1;
            coordinates = new float[300];
            for(int i = 0; i < 300; i++)
                coordinates[i] = src.readFloat();
        }
        public Vec() {
            numWords = 0;
            coordinates = new float[300];
        }
        
        public float length() {
            float len = 0.0f;
            for(float c : coordinates)
                len += c * c;
            return (float)Math.sqrt(len); 
        }
        public String toString() {
            String str = "Vec(";
            for(float c : coordinates)
                str = str + c + ", ";
            return str.substring(0, str.length() - 2) + ")";
        }
        public Vec normalize() {
            Vec norm = new Vec();
            float len_inv = 1.0f / length();
            for(int i = 0; i < 300; i++) {
                norm.coordinates[i] = coordinates[i] * len_inv;
            }
            norm.numWords = this.numWords;
            return norm;
        }
        public Vec add(Vec b) {
            Vec sum = new Vec();
            for(int i = 0; i < 300; i++) {
                sum.coordinates[i] = this.coordinates[i] + b.coordinates[i];
            }
            sum.numWords = this.numWords + b.numWords;
            return sum;
        }
        public Vec sub(Vec b) {
            Vec sum = new Vec();
            for(int i = 0; i < 300; i++) {
                sum.coordinates[i] = this.coordinates[i] - b.coordinates[i];
            }
            sum.numWords = this.numWords - b.numWords;
            return sum;
        }
        public Vec subAndAdd(Vec s, Vec a) {
            Vec sum = new Vec();
            for(int i = 0; i < 300; i++) {
                sum.coordinates[i] = this.coordinates[i] - s.coordinates[i] + a.coordinates[i];
            }
            sum.numWords = this.numWords - s.numWords + a.numWords;
            return sum;
        }
        public float dot(Vec b) {
            float prod = 0.0f;
            for(int i = 0; i < 300; i++) {
                prod += this.coordinates[i] * b.coordinates[i];
            }
            return prod;
        }
        
    }
    
    private Hashtable<String, Vec> vectors;
    
    public WordVectors(String fileName) throws FileNotFoundException, IOException {
        System.out.println("Loading word vectors.");
        String extension = "";
        vectors = new Hashtable<>();
        int i = fileName.lastIndexOf('.');
        extension = fileName.substring(i + 1);
        if (extension.equals("bin"))
             loadBinary(fileName);
        else if (extension.equals("txt") || extension.equals("ascii"))
             loadPlainText(fileName);
        System.out.println("Done loading word vectors.");
             
    }
    
    public void loadBinary(String fileName) throws FileNotFoundException, IOException {
        FileInputStream fr = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fr);
        DataInputStream dis = new DataInputStream(bis);
        while (true) {
            StringBuilder word = new StringBuilder();
            byte c;
            try {
                while((c = dis.readByte()) != '\0') {
                    word.append((char)c);
                }
            } catch(java.io.EOFException e) {
                break;
            }
            Vec vec = new Vec(dis);
            if(Float.isInfinite(vec.length()))
                System.out.println(word+ ": " + vec);
            else
                vectors.put(word.toString(), vec);
        }
        dis.close();
        bis.close();
        fr.close();
    
    }

    public void loadPlainText(String fileName) throws FileNotFoundException, IOException {
        FileReader fin = new FileReader(fileName);
        Scanner src = new Scanner(fin);
        while (src.hasNext()) {
            String word = src.next();
            vectors.put(word, new Vec(src));
        }
        fin.close();
    }
    public float distance(String a, String b) {
        Vec A = vectors.get(a);
        Vec B = vectors.get(b);
        if(A == null) {
            System.out.println(a + " not found.");
            return 0.0f;
        }
        if(B == null) {
            System.out.println(b + " not found.");
            return 0.0f;
        }
        return A.normalize().dot(B.normalize());
    }
    public static int getMin(ArrayList<Pair<String, Float>> v) {
        float minDist = Float.POSITIVE_INFINITY;
        int minDistId = -1;
        for(int i = 0; i < v.size(); i++) {
            float dist = v.get(i).getValue();
            if(dist < minDist) {
                minDist = dist;
                minDistId = i;
            }
        }
        return minDistId;
    }
    public class ResultComparator implements Comparator<Pair<String, Float>> {
        @Override
        public int compare(Pair<String, Float> o1, Pair<String, Float> o2) {
            return (o2.getValue().compareTo(o1.getValue()));
        }
    }
    public Vec phraseToVec(ArrayList<String> phrase, int maxWords, boolean normalize, boolean debug) {
        Vec A = new Vec();
        for(String word : phrase) {
            if(maxWords == A.numWords) break;
            
            Vec W = vectors.get(word);
            if(W == null) {
                if(debug) System.out.println(word + " not in dictionary.");
            } else {
                A = A.add(W);
            }
        }
        if(normalize) A = A.normalize();
        return A;
    }
    public ArrayList<Pair<String, Float>> distance(String a) {
        String[] words = a.split(" ");
        ArrayList<String> wordsList = new ArrayList<String>(Arrays.asList(words));
        Vec A = phraseToVec(wordsList, -1, true, true);
        
        ArrayList<Pair<String, Float>> result = new ArrayList();
        Set<String> keys = vectors.keySet();
        for(String key: keys){
            boolean isInput = false;
            for(String in : words) {
                if(key.equals(in)) {
                    isInput = true;
                    break;
                }
            }
            if(isInput) continue;
            
            Vec V = vectors.get(key).normalize();
            float dist = A.dot(V);
            Pair<String, Float> pair = new Pair(key, dist);
            if(result.size() < 40) {
                result.add(pair);
            }
            else
            {
                int min = getMin(result);
                if(result.get(min).getValue() < dist)
                    result.set(min, pair);
            }
        }
        result.sort(new ResultComparator());
        return result;
    }

    int incIndex(int index, ArrayList<String>words)
    {
        index++;
        for(; index < words.size(); index++)
            if(vectors.get(words.get(index)) != null)
                return index;
        return index;
    }
    public ArrayList<Float> comparePhrase(ArrayList<String> suspect, ArrayList<String> source, float min) {
        ArrayList<Float> distances = new ArrayList<Float>();
        
        Vec sVecNorm = phraseToVec(suspect, -1, true, true);
        Vec fVec = new Vec();
        int firstWordIndex = -1;
        int lastWordIndex = -1;
        for(int i = 0; fVec.numWords < sVecNorm.numWords; i++) {
            String word = source.get(i);
            Vec W = vectors.get(word);
            if(W != null) {
                if(firstWordIndex == -1) {
                    firstWordIndex = i;
                }
                lastWordIndex = i;
                //System.out.println("'"+word+"' len " + W.length());
                fVec = fVec.add(W);
            }
        }
        int nextWordIndex = incIndex(lastWordIndex, source);
        
        // COMPARACAO
        float similarity = sVecNorm.dot(fVec.normalize());
        distances.add(similarity);// save result on list to drawn after // guardar os resultados na lista para depois desenhar
        float maxSim = similarity;
        
        while(nextWordIndex < source.size())
        {
            // ATUALIZACAO do vetor
            fVec = fVec.subAndAdd(
                    vectors.get(source.get(firstWordIndex)),
                    vectors.get(source.get(nextWordIndex)));
            
            // COMPARACAO
            similarity = sVecNorm.dot(fVec.normalize());
            distances.add(similarity);// para guardar os resultados na lista para depois desenhar
            if(similarity > maxSim) maxSim = similarity;

            // INCREMENTACAO
            firstWordIndex = incIndex(firstWordIndex, source);
            nextWordIndex = incIndex(nextWordIndex, source);
        }
        
        if(maxSim < min) return null;
        return distances;
    }
    
    
    public static void convertToBinary(String input, String output) throws IOException{
        
        FileOutputStream fw = new FileOutputStream(output);
        BufferedOutputStream bos = new BufferedOutputStream(fw);
        DataOutputStream dos = new DataOutputStream(bos);

        FileReader fin = new FileReader(input);
        int n = 0;
        Scanner src = new Scanner(fin);
        while (src.hasNext()) {
            String word = src.next().toLowerCase();
            dos.writeBytes(word);
            dos.writeByte('\0');  //
            try {
                if (n % 500 == 0) {
                    System.out.println(n);
                }
                for (int i = 0; i < 300; i++) {
                    dos.writeFloat(src.nextFloat());
                }
            } catch (Exception e) {
                System.out.println("error in word "+ word + " " + n);
                System.exit(1);
            }
            n++;
        }
        fin.close();
        System.out.println("finished writing");
        dos.close();
        bos.close();
        fw.close();
    }
    public static long timeCalculator(long start) {
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Execution time: " + elapsed + " Millis");
        return elapsed;
    }
    public static void main(String args[]) throws IOException {
        //convertToBinary("/Users/mussandi/Word2Vec/GNewsW2Vsingle.ascii", "/Users/mussandi/Word2Vec/GNewsW2Vsingle.bin");
        //System.exit(0);
        long start = System.currentTimeMillis();
        //Scanner keyboard = new Scanner (System.in);
        WordVectors wv = new WordVectors("/Users/mussandi/Word2Vec/GNewsW2Vsingle.bin");
        
//        
//        String suspeitoFile = "/Users/mussandi/Documents/SourcePAN12Time/DezBlocosPAN12.txt";
//        LanguageUtils.loadFiles("/Users/mussandi/Documents/SourcePAN12Time/", -1, suspeitoFile);
//        ArrayList<Pair<Integer, ArrayList<String>>> paragraphs = LanguageUtils.fileToParagraphs(suspeitoFile, true);

        String s1 = "/Users/mussandi/Documents/testes/teste/suspeito.txt";
        String s2 = "/Users/mussandi/Documents/testes/teste/suspeito2.txt";
        String s3 = "/Users/mussandi/Documents/testes/teste/suspeito3.txt";
        String s4 = "/Users/mussandi/Documents/testes/teste/suspeito4.txt";
        String f = "/Users/mussandi/Documents/testes/teste/fonte.txt";
        ArrayList<String> S1 = LanguageUtils.fileToList(s1, true);
        ArrayList<String> S2 = LanguageUtils.fileToList(s2, true);
        ArrayList<String> S3 = LanguageUtils.fileToList(s3, true);
        ArrayList<String> S4 = LanguageUtils.fileToList(s4, true);
        ArrayList<String> F = LanguageUtils.fileToList(f, true);
        
        
        final Plot plot = new Plot("Distances");
        ArrayList<Float> distances = wv.comparePhrase(S1, F, -1.0f);
        if(distances != null) plot.addSeries("s1", distances);
        distances = wv.comparePhrase(S2, F, -1.0f);
        if(distances != null) plot.addSeries("s2", distances);
        distances = wv.comparePhrase(S3, F, -1.0f);
        if(distances != null) plot.addSeries("s3", distances);
        distances = wv.comparePhrase(S4, F, -1.0f);
        if(distances != null) plot.addSeries("s4", distances);       
        
        
        
//        String s1 = "/Users/mussandi/Documents/testes/teste/parpha.txt";
//        String f = "/Users/mussandi/Documents/testes/teste/fonte.txt";
//        ArrayList<String> S1 = LanguageUtils.fileToList(s1, true);
//        ArrayList<String> F = LanguageUtils.fileToList(f, true);
//        
//        final Plot plot = new Plot("Distances");
//        ArrayList<Float> distances = wv.comparePhrase(S1, F, -1.0f);
//        if(distances != null) plot.addSeries("s1", distances);
        
        
//        for (int i = 0; i < paragraphs.size(); i++) {
//            ArrayList<String> paragraph = paragraphs.get(i).getValue();
//             System.out.println(paragraph.size());
//            for (Pair<String, ArrayList<String>> file : LanguageUtils.files) {
//                ArrayList<Float> distances = wv.comparePhrase(paragraph, file.getValue(), .8f);
//                if(distances != null) plot.addSeries(file.getKey(), distances);
//            }
//            break;
//        }
        plot.end();
        
        
//        //convertToBinary("/Users/mussandi/Word2Vec/GNewsW2Vsingle.txt", "/Users/mussandi/Word2Vec/GNewsW2Vsingle.bin");
//        System.out.println("insert a word or sentence:");
//        String w = keyboard.next().toLowerCase();
//        ///System.out.println(wv.distance("king", "queen"));
//        //ArrayList<Pair<String, Float>> l = wv.distance("king was with queen"); // no insert using keyboard sentence.
//        ArrayList<Pair<String, Float>> l = wv.distance(w);
//        if(l != null) {
//            System.out.println("---------------------------------------------");
//            for (Pair<String, Float> pair : l) {
//                System.out.println(pair.getKey() + "\t" + pair.getValue());
//            }
//            System.out.println("---------------------------------------------");
//            System.out.println("");
//        }
         timeCalculator(start);
    }

}
