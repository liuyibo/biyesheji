package liuyibo;

import liuyibo.models.Entity;
import liuyibo.models.Word;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by liuyibo on 16-3-7.
 */

/**
 * give a string, tokenize it into a token array
 * eg. Who is Barack Obama? -> Who, is, Barack Obama
 * strategy: for each word, we find longest token which is a entity label in Wikidata
 */
public class Tokenizer {
    private Query mSentence;
    private Result result;

    public static class Result {
        List<Word> words = new ArrayList<>();
    }

    public Tokenizer(String text) {
        mSentence = new Query(text);
        result = new Result();
    }

    public Result process() {

        int n = mSentence.length();

        List<Entity> list = new ArrayList<>();
        Set<String> phrases = new TreeSet<>();

        for (int i = 0; i < n; i++) {
            for (int j = i + 2; j <= n; j++) {
                String token = substrWithTokenIndex(i, j);
                List<Entity> result = WikiJsonQueryHelper.queryEntities(token);
                if (result == null) {
                    System.out.println("QUERY ERROR!");
                    continue;
                }
                if (result.isEmpty()) {
                    break;
                }
                for (Entity entity : result) {
                    boolean match = false;
                    if (token.equals(entity.label)) {
                        match = true;
                    } else if (entity.aliases != null) {
                        for (String alias : entity.aliases) {
                            if (token.equals(alias)) {
                                match = true;
                            }
                        }
                    }
                    if (match) {
                        list.add(entity);
                        phrases.add(token);
                    }
                }
            }
        }

        for (int i = 0; i < n; i++) {
            boolean ok = false;
            for (int j = n; j >= i + 2; j--) {
                String token = substrWithTokenIndex(i, j);
                if (canBeToken(i, j) && phrases.contains(token)) {
                    ok = true;

                    List<String> phrase = new ArrayList<>();
                    for (int k = i; k < j; k++) {
                        phrase.add(mSentence.word(k).word);
                    }
                    String str = String.join(" ", phrase);
                    Word word = new Word(str);
                    word.regword = token;
                    result.words.add(word);

                    i = j - 1;
                    break;
                }
            }
            if (!ok) {
                Word word = mSentence.word(i);
                String str = word.word;
                if (str.equals(str.toLowerCase()) && mSentence.posTag(i).startsWith("N")) {
                    word.regword = mSentence.lemma(i);
                }
                result.words.add(word);
            }
        }

        return result;
    }

    private boolean canBeToken(int start, int end) {
        boolean uppercase = false;
        boolean lowercase = false;
        for (int i = start; i < end; i++) {
            if (mSentence.posTag(i).equals("DT") || mSentence.posTag(i).equals("IN")) {
                continue;
            }
            if (Character.isUpperCase(mSentence.word(i).word.charAt(0))) {
                uppercase = true;
            }
            if (Character.isLowerCase(mSentence.word(i).word.charAt(0))) {
                lowercase = true;
            }
        }
        if (lowercase && uppercase) {
            return false;
        }
        return true;
    }

    private String substrWithTokenIndex(int start, int end) {
        List<String> list = new ArrayList<>();
        for (int i = start; i < end; i++) {
            if (Character.isUpperCase(mSentence.word(i).word.charAt(0))) {
                list.add(mSentence.word(i).word);
            } else if (mSentence.posTag(i).equals("NNS")) {
                list.add(mSentence.lemma(i));
            } else {
                list.add(mSentence.word(i).word);
            }
        }
        return String.join(" ", list);
    }

}
