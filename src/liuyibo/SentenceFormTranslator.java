package liuyibo;

import edu.stanford.nlp.semgraph.SemanticGraph;
import liuyibo.models.Tree;
import liuyibo.models.Word;
import liuyibo.utils.ListBuilder;
import liuyibo.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyibo on 16-3-5.
 */

/**
 * parse a sentence to a affirmative sentence
 */
public class SentenceFormTranslator {

    // place holders
    private static final Word NOUN = new Word("song");
    private static final Word NOUN_S = new Word("songs");
    private static final Word DATE = new Word("2037");
    private static final Word LOCATION = new Word("Beijing");
    private static final Word ARE = new Word("are");
    private static final Word IS = new Word("is");
    private static final Word IN = new Word("in");

    public static class Result {
        List<Word> words = new ArrayList<>();
        boolean isPlaceHolder;
        int focusWord = -1;
        enum FocusType {
            LOCATION,
            PERSON,
            TIME
        }
        FocusType focusType;
        boolean isFocusInstanceType;
    }

    private Query mSentence;
    private Tree tree;
    private List<Word> tokens;
    private List<Word> resultTokens;
    private int focus;
    private Result result;

    public SentenceFormTranslator(Tokenizer.Result result) {
        mSentence = new Query(result.words);
        tree = mSentence.tree();
        tokens = new ArrayList<>();
        this.result = new Result();
        for (int i = 0; i < mSentence.length(); i++) {
            tokens.add(mSentence.word(i));
        }
    }

    public Result process() {

        resultTokens = new ArrayList<>();
        focus = -1;

        analyse();

        if (resultTokens != null && !resultTokens.isEmpty()) {
            result.words.addAll(resultTokens);
        } else {
            throw new ParseException();
        }
        return result;
    }

    private void analyseSentence(int start, int end, int placeholderType) {
        boolean hasPlaceholder = result.isPlaceHolder = placeholderType >= 0;
        int endIndex = end;
        int beIndex = -1;
        for (int i = start; i < mSentence.length(); i++) {
            String lemma = mSentence.lemma(i);
            if (lemma.equals("do") || lemma.equals("be")) {
                beIndex = i;
                break;
            }
        }


        boolean rev = true;
        if (beIndex == -1) {
            rev = false;
        } else {
            String pos = mSentence.posTag(endIndex - 1);
            if (mSentence.lemma(beIndex).equals("be") && !pos.equals("IN") && !pos.equals("TO")) {
                rev = false;
            }
        }
        if (!rev) {
            if (!hasPlaceholder) {
                resultTokens = new ListBuilder<>(tokens)
                        .append(start)
                        .build();
                focus = 1 + new Query(resultTokens).getFocus();
                result.focusWord = focus;
            } else {
                if (placeholderType == 3) {
                    boolean multi = mSentence.posTag(new Query(new ListBuilder<>(tokens).append(start).build())
                            .getFocus() + start).equals("NNS");
                    resultTokens = new ListBuilder<>(tokens)
                            .append(multi ? NOUN_S : NOUN)
                            .append(multi ? ARE : IS)
                            .append(start)
                            .build();
                } else {
                    int verbIndex = -1;
                    for (int i = start; i < mSentence.length(); i++) {
                        if (mSentence.posTag(i).startsWith("V")) {
                            verbIndex = i;
                            break;
                        }
                    }
                    boolean sing = mSentence.posTag(verbIndex).equals("VBZ")
                            || mSentence.word(verbIndex).regword.equals("was");
                    resultTokens = new ListBuilder<>(tokens)
                            .append(sing ? NOUN : NOUN_S)
                            .append(start)
                            .build();
                }
                focus = 0;
                result.focusWord = focus;
            }
        } else {
            if (beIndex == -1) {
                throw new ParseException();
            }
            int verbIndex = -1;
            for (int i = beIndex + 1; i < end; i++) {
                if (mSentence.posTag(i).startsWith("V")) {
                    verbIndex = i;
                    break;
                }
            }

            if (verbIndex == -1) {
                throw new ParseException();
            }
            if (!hasPlaceholder) {
                resultTokens = new ListBuilder<>(tokens)
                        .append(beIndex + 1, verbIndex)
                        .append(beIndex, beIndex + 1)
                        .append(verbIndex, endIndex)
                        .append(start, beIndex)
                        .append(endIndex)
                        .build();
                focus = endIndex - beIndex
                        + new Query(new ListBuilder<>(tokens).append(start, beIndex).build()).getFocus();
                result.focusWord = focus;
            } else {
                ListBuilder<Word> lb = new ListBuilder<>(tokens)
                        .append(beIndex + 1, verbIndex)
                        .append(beIndex, beIndex + 1)
                        .append(verbIndex, endIndex);
                if (placeholderType == 0) {
                    lb.append(NOUN_S);
                    focus = endIndex - beIndex;
                } else if (placeholderType == 1) {
                    lb.append(IN);
                    lb.append(DATE);
                    focus = endIndex - beIndex + 1;
                } else if (placeholderType == 2) {
                    lb.append(IN);
                    lb.append(LOCATION);
                    focus = endIndex - beIndex + 1;
                }
                resultTokens = lb.append(endIndex)
                        .build();
                result.focusWord = focus;
            }
        }
    }

    private void analyse() {
        Tree tree = this.tree;

        int endIndex = mSentence.length();

        if (startWith("WDT") || startWith("WP")) {
            if (startWith("WP")) {
                if (startWithLemma("who")) {
                    result.focusType = Result.FocusType.PERSON;
                }
            } else if (startWith("WDT")) {
                result.isFocusInstanceType = true;
            }
            if (startWith("WDT")) {
                analyseSentence(1, endIndex, -1);
            } else {
                analyseSentence(1, endIndex, 0);
            }
        } else if (startWithLemma("list") || startWithLemma("give", "i")) {
            result.isPlaceHolder = true;
            int startIndex = 0;
            if (startWithLemma("list")) {
                startIndex = 1;
            } else if (startWithLemma("give", "i")) {
                startIndex = 2;
            }
            analyseSentence(startIndex, endIndex, 3);
        } else if (startWithLemma("be") || startWithLemma("do")) {
            Tree t1 = tree.firstLeaf().parent().parent();
            if (t1.numChildren() >= 3 && t1.getChild(1).isPhrase() && t1.getChild(2).isPhrase()) {
                // Is Egypts largest city also its capital?
                Tree c1 = t1.getChild(1);
                Tree c2 = t1.getChild(2);
                int idx1 = c1.firstLeafIndex();
                int idx2 = c2.firstLeafIndex();
                resultTokens = new ListBuilder<>(tokens)
                        .append(idx1, idx2)
                        .append(Utils.firstLetterLowerCase(tokens.get(0)))
                        .append(idx2)
                        .build();
            } else {
                analyseSentence(0, endIndex, -1);
            }

        } else if (startWith("WRB")) {
            if (startWithLemma("when")) {
                result.focusType = Result.FocusType.TIME;
                analyseSentence(1, endIndex, 1);
            } else if (startWithLemma("where")) {
                result.focusType = Result.FocusType.LOCATION;
                analyseSentence(1, endIndex, 2);
            }
        }

    }


    private boolean startWith(String... pos) {
        if (mSentence.length() <  pos.length) {
            return false;
        }
        for (int i = 0; i < pos.length; i++) {
            if (!mSentence.posTag(i).equalsIgnoreCase(pos[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean startWithLemma(String... words) {
        if (mSentence.length() < words.length) {
            return false;
        }
        for (int i = 0; i < words.length; i++) {
            if (!mSentence.lemma(i).equalsIgnoreCase(words[i])) {
                return false;
            }
        }
        return true;
    }

}
