package liuyibo;

import com.google.common.io.Files;
import liuyibo.models.Entity;
import org.apache.jena.atlas.json.JSON;
import sun.net.SocksProxy;

import javax.json.*;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by liuyibo on 16-3-1.
 */
public class WikiJsonQueryHelper {
    // wikidata json endpoint
    // manual page: https://www.wikidata.org/w/api.php?action=help&modules=main
    private static final String URL = "https://www.wikidata.org/w/api.php";

    /**
     * query with entity name
     * @param search
     * @return
     */
    public static JsonObject query(String search) {
        return query(search, "item");
    }

    /**
     * query with name and type
     * @param search
     * @param type "item" or "property"
     * @return
     */
    public static JsonObject query(String search, String type) {
        return query("wbsearchentities", search, type);
    }

    /**
     * query with action, name, and type
     * @param action
     * @param search
     * @param type
     * @return
     */
    private static JsonObject query(String action, String search, String type) {
        try {
            String params = new URLBuilder()
                    .param("action", action)
                    .param("format", "json")
                    .param("search", search)
                    .param("language", "en")
                    .param("type", type)
                    .buildParams();

            return queryWithParams(params);
       } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * query with param string
     * @param query
     * @return
     * @throws Exception
     */
    private static JsonObject queryWithParams(String query) throws Exception {
        JsonObject json = HttpGetter.getJson(URL + "?" + query);
        return json;
    }

    /**
     * query with id, action is "getentities"
     * @param id
     * @return
     */
    public static JsonObject queryWithId(String id) {
        String params = new URLBuilder()
                .param("action", "wbgetentities")
                .param("languages", "en")
                .param("format", "json")
                .param("props", "labels", "aliases")
                .param("ids", id)
                .buildParams();
        JsonObject json = null;
        try {
            json = queryWithParams(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * query labels of given ids
     * @param key
     * @return
     */
    public static HashMap<String, List<String>> queryLabel(List<String> key) {
        List<String> queryId = key;

        String params = new URLBuilder()
                .param("action", "wbgetentities")
                .param("languages", "en")
                .param("format", "json")
                .param("props", "labels", "aliases")
                .param("ids", queryId.toArray(new String[queryId.size()]))
                .buildParams();
        JsonObject json = null;
        try {
            json = queryWithParams(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getLabelsWithJson(json);
    }

    /**
     * parse json result to label map
     * @param json
     * @return
     */
    private static HashMap<String, List<String>> getLabelsWithJson(JsonObject json) {
        if (json == null) {
            return null;
        }
        JsonObject entities = json.getJsonObject("entities");
        if (entities == null) {
            return null;
        }

        HashMap<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, JsonValue> entry : entities.entrySet()) {
            String key = entry.getKey();
            try {
                JsonObject obj = (JsonObject) entry.getValue();
                List<String> labels = new ArrayList<>();
                labels.add(obj.getJsonObject("labels").getJsonObject("en").getString("value"));
                JsonArray aliases = obj.getJsonObject("aliases").getJsonArray("en");
                for (int i = 0; i < aliases.size(); i++) {
                    labels.add(aliases.getJsonObject(i).getString("value"));
                }
                result.put(key, labels);
            } catch (Exception e) {
                continue;
            }
        }
        return result;
    }

    /**
     * query entities with word
     * @param token
     * @return
     */
    public static List<Entity> queryEntities(String token) {
        return queryEntities(token, false);
    }

    /**
     * query properties with word
     * @param token
     * @return
     */
    public static List<Entity> queryProperties(String token) {
        return queryEntities(token, true);
    }

    /**
     * query with word and type
     * @param token
     * @param prop
     * @return
     */
    public static List<Entity> queryEntities(String token, boolean prop) {
        List<Entity> list = new ArrayList<>();
        JsonObject json = query(token, prop ? "property" : "item");
        if (json == null) {
            System.out.println("JSON error!");
            return null;
        }
        JsonArray ar = json.getJsonArray("search");
        if (ar == null) {
            System.out.println("ARRAY error!");
            return null;
        }
        if (ar.size() == 0) {
            return list;
        }
        for (int it = 0; it < ar.size(); it++) {
            JsonObject obj = ar.getJsonObject(it);
            Entity entity = new Entity();
            entity.id = obj.getString("id", null);
            entity.uri = obj.getString("concepturi", null);
            entity.label = obj.getString("label", null);
            entity.description = obj.getString("description", null);
            JsonArray aliases = obj.getJsonArray("aliases");
            if (aliases != null) {
                List<String> a = new ArrayList<>();
                for (int k = 0; k < aliases.size(); k++) {
                    String s = aliases.getString(k);
                    a.add(s);
                }
                entity.aliases = a;
            }
            list.add(entity);
        }
        return list;
    }

}
