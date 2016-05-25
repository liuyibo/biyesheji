package liuyibo.sparql;

/**
 * Created by liuyibo on 16-3-22.
 */
public class SparqlVar extends SparqlItem {
    int var;

    SparqlVar(int var) {
        this.var = var;
    }

    @Override
    public String toString() {
        return "?" + var;
    }
}
