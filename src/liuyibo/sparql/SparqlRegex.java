package liuyibo.sparql;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyibo on 16-3-29.
 */
public class SparqlRegex extends SparqlItem {

    public static SparqlItem plus(SparqlItem a) {
        return new Plus(a);
    }
    public static SparqlItem star(SparqlItem a) {
        return new Star(a);
    }
    public static SparqlItem or(List<? extends SparqlItem> a) {
        return new Or(a);
    }

    public static class Plus extends SparqlItem {
        SparqlItem a;
        public Plus(SparqlItem a) {
            this.a = a;
        }
        @Override
        public String toString() {
            return a + "+";
        }
    }
    public static class Star extends SparqlItem {
        SparqlItem a;
        public Star(SparqlItem a) {
            this.a = a;
        }
        @Override
        public String toString() {
            return a + "*";
        }
    }
    public static class Or extends SparqlItem {
        List<SparqlItem> a;
        public Or(List<? extends SparqlItem> a) {
            this.a = new ArrayList<>(a);
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < a.size(); i++) {
                if (i > 0) {
                    sb.append("|");
                }
                sb.append(a.get(i));
            }
            sb.append(")");
            return sb.toString();
        }
    }
}
