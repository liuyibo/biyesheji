package liuyibo.methods;

import liuyibo.ParseException;
import liuyibo.WikiHelper;
import liuyibo.sparql.*;

/**
 * Created by liuyibo on 16-3-29.
 */
public class NounRelationAnalyzer {

    /**
     * if text is a type, return item with type "text"
     * if text is not a type, just return the item "text"
     * @param text
     * @param sb
     * @return
     */
    public static SparqlItem analyze(String text, SparqlBuilder sb) {

        String entity = WikiHelper.getEntity(text);
        if (entity == null) {
            throw new ParseException();
        }
        SparqlUri uri = new SparqlUri(entity);
        SparqlItem v;
        if (WikiHelper.isType(uri)) {
            v = sb.newVar();
            SparqlItem res = v;
            SparqlVar v2 = sb.newVar();
            sb.line(new SparqlLineTriple(v, Sparql.URI_INSTANCEOF, v2));
            sb.line(new SparqlLineTriple(v2, SparqlRegex.star(Sparql.URI_SUBCLASS), uri));
            return res;
        } else {
            return uri;
        }

    }
}
