package liuyibo.models;

/**
 * Created by liuyibo on 16-3-21.
 */
public class Word {
    public String word;
    public String regword;

    public Word(String word) {
        this.word = word;
        this.regword = word;
    }

    public String toString() {
        return word;
    }
}
