package liuyibo;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by liuyibo on 16-3-7.
 */
public class TokenizerTest {
    @Test
    public void test() {
        String query = "Which countries have more than two official languages?";
        new Tokenizer(query).process();
    }
}