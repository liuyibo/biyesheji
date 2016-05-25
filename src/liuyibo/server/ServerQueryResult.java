package liuyibo.server;

import liuyibo.ParseException;
import liuyibo.SparqlQueryResult;
import org.json.JSONObject;

import javax.json.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyibo on 16-5-9.
 */
public class ServerQueryResult {
    public static class Line {
        String uri;
        String label;
    }
    List<Line> lines = new ArrayList<>();
    public ServerQueryResult(SparqlQueryResult sparqlResult) {
        if (sparqlResult == null || sparqlResult.getColumnCount() == 0 || sparqlResult.getColumnCount() > 2) {
            throw new ParseException();
        }
        String name = sparqlResult.getVarName(0);
        for (int i = 0; i < sparqlResult.getResultCount(); i++) {
            Line line = new Line();
            SparqlQueryResult.Item item = sparqlResult.getResult(i, 0);
            if (item.type.equals("uri")) {
                line.uri = item.value;
            }
            if (sparqlResult.getColumnCount() == 2) {
                String name1 = sparqlResult.getVarName(1);
                if (name1.equals(name + "Label")) {
                    line.label = sparqlResult.getResult(i, 1).value;
                }
            }
            lines.add(line);
        }
    }

    public JsonArray toJson() {
        JsonArrayBuilder ab = Json.createArrayBuilder();
        for (Line line : lines) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            if (line.label != null) {
                ob.add("label", line.label);
            }
            if (line.uri != null) {
                ob.add("uri", line.uri);
            }
            ab.add(ob);
        }
        return ab.build();
    }
}
