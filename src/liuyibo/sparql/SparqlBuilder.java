package liuyibo.sparql;

import liuyibo.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyibo on 16-3-22.
 */
public class SparqlBuilder {
    private static final String LABEL_SERVICE_LINE = "SERVICE wikibase:label {bd:serviceParam wikibase:language \"en\" .}";
    private StringBuilder sb = new StringBuilder();
    private int limit = 100;
    private List<SparqlItem> select = null;
    private List<SparqlLine> lines = new ArrayList<>();
    private int varCount = 0;
    private Map<SparqlItem, SparqlItem> equals = new HashMap<>();

    public SparqlBuilder() {

    }

    public SparqlVar newVar() {
        return new SparqlVar(++varCount);
    }

    public SparqlBuilder line(SparqlLine line) {
        lines.add(line);
        return this;
    }

    public SparqlBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public SparqlBuilder select(SparqlVar select) {
        if (this.select == null) {
            this.select = new ArrayList<>();
        }
        this.select.add(select);
        return this;
    }

    private void applyEqualsRelation() {
        if (select != null) {
            for (int i = 0; i < select.size(); i++) {
                SparqlItem var = select.get(i);
                if (equals.containsKey(var)) {
                    select.set(i, equals.get(var));
                }
            }
        }
        boolean ok;
        do {
            ok = true;
            for (SparqlLine line : lines) {
                if (line.applyEquals(equals)) {
                    ok = false;
                }
           }
        } while (!ok);
    }

    public String build() {
        applyEqualsRelation();

        sb = new StringBuilder();
        sb.append("select distinct ");
        if (select == null || select.size() != 1 || !(select.get(0) instanceof SparqlVar)) {
            throw new ParseException();
        } else {
            SparqlVar var = (SparqlVar) select.get(0);
            sb.append(var);
            sb.append(" ");
            sb.append("?" + var.var + "Label ");
        }
        sb.append(" {\n");
        for (SparqlLine line : lines) {
            sb.append("\t");
            sb.append(line);
        }
        sb.append(LABEL_SERVICE_LINE);
        sb.append("}");
        if (limit != -1) {
            sb.append(" limit ").append(limit);
        }
        return sb.toString();
    }

    public boolean empty() {
        return lines.isEmpty();
    }

    public SparqlBuilder equalVar(SparqlItem v1, SparqlItem v3) {
        SparqlItem a = v1;
        SparqlItem b = v3;
        if (a instanceof SparqlVar || b instanceof SparqlVar) {
            if (!(a instanceof SparqlVar)) {
                SparqlItem c = a;
                a = b;
                b = c;
            }
            equals.put(a, b);
        } else {
            throw new ParseException();
        }
        return this;
    }

}
