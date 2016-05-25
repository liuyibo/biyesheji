package liuyibo;

import liuyibo.models.Word;
import liuyibo.utils.DataGenerator;
import liuyibo.utils.Utils;

import javax.rmi.CORBA.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liuyibo on 16-3-4.
 */

/**
 * move the head proposition to the end
 * eg. In which city does Obama live -> Which city does Obama live in
 */
public abstract class MoveHeadPreposition {

    public static String process(String text) {
        Query sentence = new Query(text);
        if (sentence.posTag(0).equals("IN")) {
            List<String> tokens = new ArrayList<>();
            int first = 0;
            while (sentence.posTag(first).equals("IN")) {
                first++;
            }
            int last = sentence.length();
            while (sentence.posTag(last - 1).equals(".")) {
                last--;
            }
            for (int i = first; i < last; i++) {
                String word = sentence.word(i).word;
                if (i == first) {
                    word = Utils.firstLetterUpperCase(word);
                }
                tokens.add(word);
            }
            for (int i = 0; i < first; i++) {
                String word = sentence.word(i).word;
                word = Utils.firstLetterLowerCase(word);
                tokens.add(word);
            }
            for (int i = last; i < sentence.length(); i++) {
                tokens.add(sentence.word(i).word);
            }
            return String.join(" ", tokens);
        } else if (sentence.lemma(0).equals("how") && sentence.lemma(1).matches("tall|high") && sentence.lemma(2).equals("be")) {
            StringBuilder sb = new StringBuilder("What is the height of");
            for (int i = 3; i < sentence.length(); i++) {
                sb.append(" ");
                sb.append(sentence.word(i).word);
            }
            return sb.toString();
        } else {
            return text;
        }
    }
}
