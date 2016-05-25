package liuyibo.utils;

import liuyibo.sparql.SparqlBuilder;
import liuyibo.sparql.SparqlLineEx;
import liuyibo.sparql.SparqlVar;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyibo on 16-4-4.
 */

/**
 * find a exist pattern of a given word
 * eg. "uncle" means "?1 father ?2. ?2 fater ?3. ?4 father ?3. ?4 sex male", ?1 uncle ?4
 */
public class PatternParser {
    private static Map<String, String> verbMap;
    private static Map<String, String> nounMap;
    private static Map<String, String> adjMap;

    private static Map<String, String> verbMap() {
        if (verbMap == null) {
            verbMap = init("data/patterns/verb.json");
        }
        return verbMap;
    }

    private static Map<String, String> nounMap() {
        if (nounMap == null) {
            nounMap = init("data/patterns/noun.json");
        }
        return nounMap;
    }

    private static Map<String, String> adjMap() {
        if (adjMap == null) {
            adjMap = init("data/patterns/adj.json");
        }
        return adjMap;
    }

    private static Map<String, String> init(String filepath) {
        try {
            JsonArray ar = Json.createReader(new FileInputStream(new File(filepath))).readArray();
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < ar.size(); i++) {
                JsonObject obj = ar.getJsonObject(i);
                String word = obj.getString("word");
                String pattern = obj.getString("pattern");
                map.put(word, pattern);
            }
            return map;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Result verb(String word, SparqlBuilder sb) {
        return parse(verbMap(), word, sb);
    }

    public static Result noun(String word, SparqlBuilder sb) {
        return parse(nounMap(), word, sb);
    }

    public static Result adj(String word, SparqlBuilder sb) {
        return parse(adjMap(), word, sb);
    }

    private static Result parse(Map<String, String> map, String word, SparqlBuilder sb) {
        if (!map.containsKey(word)) {
            return null;
        }
        Result result = new Result();
        String pattern = map.get(word);
        List<String> vars = new ArrayList<>();
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '?') {
                int j = i + 1;
                while (j < pattern.length() && Character.isLetterOrDigit(pattern.charAt(j))) {
                    j++;
                }
                vars.add(pattern.substring(i, j));
                i = j;
            }
        }
        Map<String, SparqlVar> items = new HashMap<>();
        for (int i = 0; i < vars.size(); i++) {
            String var = vars.get(i);
            if (items.containsKey(var)) {
                continue;
            }
            items.put(var, sb.newVar());
        }
        if (items.containsKey("?x")) {
            result.x = items.get("?x");
        }
        if (items.containsKey("?y")) {
            result.y = items.get("?y");
        }
        if (items.containsKey("?z")) {
            result.z = items.get("?z");
        }
        List<Object> objs = new ArrayList<>();
        int last = 0;
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '?') {
                if (i != last) {
                    objs.add(pattern.substring(last, i));
                }
                int j = i + 1;
                while (j < pattern.length() && Character.isLetterOrDigit(pattern.charAt(j))) {
                    j++;
                }
                objs.add(items.get(pattern.substring(i, j)));
                i = j;
                last = i;
            }
        }
        if (last < pattern.length()) {
            objs.add(pattern.substring(last));
        }
        sb.line(new SparqlLineEx(objs));
        return result;
    }

    public static class Result {
        public SparqlVar x;
        public SparqlVar y;
        public SparqlVar z;
    }
}
