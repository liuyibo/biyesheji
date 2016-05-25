package liuyibo.sparql;

import java.util.Map;

/**
 * Created by liuyibo on 16-3-22.
 */
public class SparqlLineTriple extends SparqlLine {
    SparqlItem a;
    SparqlItem b;
    SparqlItem c;

    public SparqlLineTriple(SparqlItem a, SparqlItem b, SparqlItem c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public String toString() {
        return a + " " + b + " " + c + " .\n";
    }

    @Override
    public boolean applyEquals(Map<SparqlItem, SparqlItem> equals) {
        if (equals.containsKey(a)) {
            a = equals.get(a);
            return true;
        }
        if (equals.containsKey(c)) {
            c = equals.get(c);
            return true;
        }
        return false;
    }
}
