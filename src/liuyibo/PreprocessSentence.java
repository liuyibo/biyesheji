package liuyibo;

/**
 * Created by liuyibo on 16-5-8.
 */
public abstract class PreprocessSentence {
    public static String process(String text) {
        int l = 0;
        while (l < text.length() && !Character.isLetterOrDigit(text.charAt(l))) {
            l++;
        }
        int r = text.length() - 1;
        while (r >= 0 && !Character.isLetterOrDigit(text.charAt(r))) {
            r--;
        }
        if (r - l < 5) {
            throw new ParseException();
        }
        text = text.substring(l, r + 1);
        text = String.join(" ", text.split(" +"));
        return text;
    }

}
