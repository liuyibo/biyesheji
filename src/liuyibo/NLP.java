package liuyibo;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import liuyibo.utils.DataGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by liuyibo on 16-2-29.
 */
public class NLP {

    private static StanfordCoreNLP sPipeline;
    private static DependencyParser sDependencyParser;

    public static StanfordCoreNLP getPipeline() {
        if (sPipeline == null) {
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
            sPipeline = new StanfordCoreNLP(props);
        }
        return sPipeline;
    }

    public static DependencyParser getDependencyParser() {
        if (sDependencyParser == null) {
            sDependencyParser = DependencyParser.loadFromModelFile(DependencyParser.DEFAULT_MODEL);
        }
        return sDependencyParser;
    }

    public static CoreMap getSentence(String text) {
        Annotation document = new Annotation(text);
        getPipeline().annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        return sentences.get(0);
    }

    public static CoreMap getSentence(List<String> tokens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(tokens.get(i).replace(' ', 'ߝ'));
        }
        return getSentence(sb.toString());
    }

    public static void init() {
        analyze("Who is the father of Obama?");
    }

    public static SparqlQueryResult analyze(String sentence) {
        String text = sentence;
        System.out.println();
        System.out.println();
        System.out.println(text);
        try {
            String result0 = PreprocessSentence.process(text);
            String result1 = MoveHeadPreposition.process(result0);
            Tokenizer.Result result2 = new Tokenizer(result1).process();
            SentenceFormTranslator.Result result3 = new SentenceFormTranslator(result2).process();
            SentenceParser.Result result4 = new SentenceParser(result3).process();
            return result4.sparqlResult;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return null;
        }
    }
}
