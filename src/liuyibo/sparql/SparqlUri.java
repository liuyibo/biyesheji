package liuyibo.sparql;

/**
 * Created by liuyibo on 16-3-22.
 */
public class SparqlUri extends SparqlItem {
    private String uri;

    public SparqlUri(String uri) {
        this.uri = uri;
    }

    public static SparqlUri newEntity(String id) {
        return new SparqlUri(Sparql.URIPREFIX_WD + id);
    }

    public static SparqlUri newProperty(String id) {
        return new SparqlUri(Sparql.URIPREFIX_WDT + id);
    }

    public static String name(String uri) {
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    @Override
    public String toString() {
        if (uri.startsWith(Sparql.URIPREFIX_WD)) {
            return "wd:" + name(uri);
        } else if (uri.startsWith(Sparql.URIPREFIX_WDT)) {
            return "wdt:" + name(uri);
        } else {
            return "<" + uri + ">";
        }
    }
}
