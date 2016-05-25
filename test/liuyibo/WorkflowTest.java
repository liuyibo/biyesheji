package liuyibo;

import liuyibo.utils.DataGenerator;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by liuyibo on 16-3-8.
 */
public class WorkflowTest {
    @Test
    public void test() throws Exception {
        for (String text : DataGenerator.QALD3()) {
            System.out.println();System.out.println();
            System.out.println(text);
            SparqlQueryResult result = NLP.analyze(text);
            if (result != null) {
                System.out.println(result);
            }
        }
    }
}
