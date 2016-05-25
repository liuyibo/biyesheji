package liuyibo.sparql;

import java.util.Map;

/**
 * Created by liuyibo on 16-4-4.
 */
public abstract class SparqlLine {
    public abstract boolean applyEquals(Map<SparqlItem, SparqlItem> equals);
}
