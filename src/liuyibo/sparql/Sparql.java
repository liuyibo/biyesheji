package liuyibo.sparql;

/**
 * Created by liuyibo on 16-3-29.
 */
public abstract class Sparql {
    public static final SparqlUri URI_INSTANCEOF = SparqlUri.newProperty("P31");
    public static final SparqlUri URI_SUBCLASS = SparqlUri.newProperty("P279");
    public static final SparqlUri URI_SUBPROPERTY = SparqlUri.newProperty("P1647");
    public static final SparqlUri URI_HASPART = SparqlUri.newProperty("P527");

    public static final String URIPREFIX_WD = "http://www.wikidata.org/entity/";
    public static final String URIPREFIX_WDT = "http://www.wikidata.org/prop/direct/";
    public static final String PREFIX_RDFS = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
    public static final String PREFIX_SCHEMA = "PREFIX schema: <http://schema.org/>\n";

    public static final SparqlUri URI_HUMAN = SparqlUri.newEntity("Q5");
    public static final SparqlUri URI_GEOLOCATION = SparqlUri.newEntity("Q2221906");
}
