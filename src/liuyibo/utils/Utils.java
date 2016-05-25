package liuyibo.utils;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.util.Index;
import liuyibo.NLP;
import liuyibo.Query;
import liuyibo.models.Word;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyibo on 16-3-4.
 */
public class Utils {

    public static String firstLetterUpperCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String firstLetterLowerCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static Word firstLetterLowerCase(Word word) {
        word.word = firstLetterLowerCase(word.word);
        return word;
    }
}
