package liuyibo;

import sun.net.SocksProxy;

import javax.json.Json;
import javax.json.JsonObject;
import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;

/**
 * Created by liuyibo on 16-3-2.
 */
public class WikiSparqlQueryHelper {
    // wikidata sparql endpoint
    private static final String URL = "https://query.wikidata.org/sparql";

    /**
     * query Wikidata with sparql
     * related web endpoint: https://query.wikidata.org/
     * @param query
     * @return
     */
    public static JsonObject query(String query) {
        String url = new URLBuilder()
                .param("query", query)
                .param("format", "json")
                .build(URL);
        JsonObject json = HttpGetter.getJson(url);
        return json;
    }
}
