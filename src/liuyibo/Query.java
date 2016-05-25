package liuyibo;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import liuyibo.models.Tree;
import liuyibo.models.Word;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by liuyibo on 16-3-3.
 */

/**
 * wrap NLP methods to a String
 */
public class Query {

    private int mLength;
    private CoreMap mSentence;
    private CoreLabel[] mTokens;
    private Word[] mWords;
    private SemanticGraph mDependencyGraph;

    public Query(List<Word> tokens) {
        mSentence = NLP.getSentence(
                tokens.stream().map(w -> w.word).collect(Collectors.toList()));

        init();

        mWords = new Word[mLength];
        for (int i = 0; i < mLength; i++) {
            mWords[i] = tokens.get(i);
        }
    }

    public Query(String text) {
        mSentence = NLP.getSentence(text);

        init();

        mWords = new Word[mLength];
        for (int i = 0; i < mLength; i++) {
            mWords[i] = new Word(mTokens[i].get(CoreAnnotations.TextAnnotation.class));
        }
    }

    private void init() {
        List<CoreLabel> tokens = mSentence.get(CoreAnnotations.TokensAnnotation.class);
        mLength = tokens.size();
        mTokens = new CoreLabel[mLength];
        tokens.toArray(mTokens);

        GrammaticalStructure gs = NLP.getDependencyParser().predict(mSentence);
        mDependencyGraph = new SemanticGraph(gs.allTypedDependencies());
    }


    public int length() {
        return mLength;
    }

    public Word word(int index) {
        return mWords[index];
    }

    public List<Word> words() {
        List<Word> words = new ArrayList<>();
        for (Word word : mWords) {
            words.add(word);
        }
        return words;
    }

    public String posTag(int index) {
        return mTokens[index].get(CoreAnnotations.PartOfSpeechAnnotation.class);
    }

    public String nerTag(int index) {
        return mTokens[index].get(CoreAnnotations.NamedEntityTagAnnotation.class);
    }

    public String lemma(int index) {
        return mTokens[index].get(CoreAnnotations.LemmaAnnotation.class);
    }

    public SemanticGraph dependencyGraph() {
        return mDependencyGraph;
    }

    public SemanticGraph simpleDependencyGraph() {
        return mSentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
    }

    public Tree tree() {
        Tree tree = new Tree(mSentence.get(TreeCoreAnnotations.TreeAnnotation.class));
        for (int i = 0; i < mLength; i++) {
            tree.setWord(i, mWords[i]);
        }
        return tree;
    }

    public String sentence() {
        return mSentence.toString();
    }

    public int getFocus() {
        boolean[] not = new boolean[mLength];
        for (SemanticGraphEdge edge : dependencyGraph().edgeListSorted()) {
            not[edge.getDependent().index() - 1] = true;
        }
        for (int i = 0; i < mLength; i++) {
            if (!not[i]) {
                return i;
            }
        }
        return -1;
    }
}
