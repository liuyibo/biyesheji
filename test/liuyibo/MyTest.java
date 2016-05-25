package liuyibo;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.data.WordnetFile;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.WordNetConnection;
import org.apache.jena.reasoner.rulesys.builtins.Print;
import org.junit.Test;

import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.Assert.*;

/**
 * Created by liuyibo on 16-3-7.
 */
public class MyTest {
    static String path = "/home/liuyibo/WordNet-3.0/dict";
    static IDictionary dict = new Dictionary(new File(path));

    @Test
    public void test() throws IOException {
        dict.open();
        IIndexWord idxWord = dict.getIndexWord("father", POS.NOUN);
        for (IWordID wordID : idxWord.getWordIDs()) {
            IWord word = dict.getWord(wordID);
            gogogo(word, 0);
            gogogo(word.getSynset(), 0);
        }
    }

    private void gogogo(IWord word, int d) {
        if (d > 2) return;
        for (int i = 0; i < d; i++) System.out.print("  ");
        System.out.println(word.getLemma());

        for (Map.Entry<IPointer, List<IWordID>> entry : word.getRelatedMap().entrySet()) {
            for (int i = 0; i < d; i++) System.out.print("  ");
            System.out.println("---" + entry.getKey());
            for (IWordID id : entry.getValue()) {
                gogogo(dict.getWord(id), d + 1);
            }
        }
    }

    private void gogogo(ISynset synnet, int d) {
        if (d > 2) return;
        for (IWord word : synnet.getWords()) {
            for (int i = 0; i < d; i++) System.out.print("  ");
            System.out.println(word);
        }
        for (Map.Entry<IPointer, List<ISynsetID>> entry : synnet.getRelatedMap().entrySet()) {
            for (int i = 0; i < d; i++) System.out.print("  ");
            System.out.println("---" + entry.getKey());
            for (ISynsetID id : entry.getValue()) {
                gogogo(dict.getSynset(id), d + 1);
            }
        }
    }

}
