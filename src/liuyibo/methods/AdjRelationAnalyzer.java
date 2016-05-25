package liuyibo.methods;

import liuyibo.sparql.SparqlBuilder;
import liuyibo.sparql.SparqlVar;
import liuyibo.utils.PatternParser;

/**
 * Created by liuyibo on 16-3-29.
 */
public class AdjRelationAnalyzer {

    public static SparqlVar analyze(String adj, SparqlBuilder sb) {
        PatternParser.Result patternResult = PatternParser.adj(adj, sb);
        if (patternResult != null) {
            return patternResult.x;
        }
        return null;
    }

}
