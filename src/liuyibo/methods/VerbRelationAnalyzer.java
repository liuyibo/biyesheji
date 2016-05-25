package liuyibo.methods;

import com.google.common.collect.TreeMultimap;
import liuyibo.*;
import liuyibo.sparql.*;
import liuyibo.utils.PatternParser;
import org.apache.lucene.search.similarities.Similarity;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by liuyibo on 16-3-28.
 */
public class VerbRelationAnalyzer {
    public static List<Item> items = new ArrayList<>();
    private static Map<String, Item> id2item = new HashMap<>();
    static {
        JsonArray json = null;
        try {
            json = Json.createReader(new FileInputStream("data/properties.json")).readArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < json.size(); i++) {
            JsonObject obj = json.getJsonObject(i);
            Item item = new Item();
            item.id = obj.getString("id");
            item.label = obj.getString("label");
            JsonArray ar = obj.getJsonArray("aliases");
            for (int j = 0; j < ar.size(); j++) {
                item.aliases.add(ar.getString(j));
            }
            items.add(item);
        }
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            item.normlabel = norm(item.label);
            for (String alias : item.aliases) {
                item.normaliases.add(norm(alias));
            }
            id2item.put(items.get(i).id, items.get(i));
        }
    }

    private static String norm(String str) {
        Query q = new Query(str);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < q.length(); i++) {
            if (q.posTag(i).startsWith("N") || q.posTag(i).startsWith("V")) {
                if (!q.lemma(i).equals("be")) {
                    if (sb.length() != 0) {
                        sb.append(" ");
                    }
                    if (q.posTag(i).startsWith("N")) {
                        sb.append(q.lemma(i));
                    } else {
                        sb.append(q.word(i));
                    }
                }
            }
        }
        return sb.toString();
    }

    public static int getWeight(WordNetHelper.WordHandler wordHandler, String prop) {
        Query q = new Query(prop);
        int tot = 0;
        if (q.lemma(q.length() - 1).equals("of")) {
            return 0;
        }
        for (int i = 0; i < q.length(); i++) {
            if (q.posTag(i).startsWith("N") || q.posTag(i).startsWith("V")) {
                if (!q.posTag(i).equals("VBZ")) {
                    tot += wordHandler.relationTo(q.lemma(i), q.posTag(i).substring(0, 1));
                }
            }
        }
        return tot;
    }

    public static AnalyzeResult analyze(String relation, SparqlBuilder sb, SparqlItem var1, SparqlItem var3) {
        Query q = new Query(relation);

        List<String> avaliableIds = WikiHelper.getAvaliableIds(var1, var3);
        List<Item> avaliableItems = new ArrayList<>();
        if (avaliableIds == null) {
            avaliableItems.addAll(items);
        } else {
            for (String id : avaliableIds) {
                if (id2item.containsKey(id)) {
                    avaliableItems.add(id2item.get(id));
                }
            }
        }
        Collection<Item> selectedItems = new HashSet<>();
        if (q.length() > 0) {
            // check if is the same
            String word = WordNetHelper.findStem(relation);
            for (Item item : avaliableItems) {
                if (WordNetHelper.findStem(item.label).equals(word)) {
                    selectedItems.add(item);
                }
                for (String alias : item.aliases) {
                    if (WordNetHelper.findStem(alias).equals(word)) {
                        selectedItems.add(item);
                    }
                }
            }
        }

        List<String> list = new ArrayList<>();
        for (int i = 0; i < q.length(); i++) {
            if (q.posTag(i).startsWith("N") || q.posTag(i).startsWith("V")) {
                if (!q.lemma(i).equals("do") && !q.lemma(i).equals("be")) {
                    list.add(q.lemma(i));
                }
            }
        }

        for (Item item : avaliableItems) {
            boolean ok = true;
            for (String word : list) {
                WordNetHelper.WordHandler wordHandler = new WordNetHelper.WordHandler(word);
                int totvalue = 0;
                totvalue += getWeight(wordHandler, item.label);
                for (String alias : item.aliases) {
                    totvalue += getWeight(wordHandler, alias);
                }
                if (totvalue == 0) {
                    ok = false;
                    break;
                }
            }
            if (!list.isEmpty() && ok) {
                selectedItems.add(item);
            }
        }
        if (selectedItems.size() != 0) {
            return constructResult(selectedItems, sb);
        }
        throw new ParseException();
    }

    private static AnalyzeResult constructResult(Collection<Item> selectedItems, SparqlBuilder sb) {
        if (selectedItems.size() == 1) {
            AnalyzeResult result = new AnalyzeResult();
            result.a = sb.newVar();
            result.c = sb.newVar();
            sb.line(new SparqlLineTriple(result.a, SparqlUri.newProperty(selectedItems.iterator().next().id), result.c));
            return result;
        } else if (selectedItems.size() > 1) {
            AnalyzeResult result = new AnalyzeResult();
            result.a = sb.newVar();
            result.c = sb.newVar();
            List<SparqlUri> uris = new ArrayList<>();
            for (Item item : selectedItems) {
                uris.add(SparqlUri.newProperty(item.id));
            }
            sb.line(new SparqlLineTriple(result.a, SparqlRegex.or(uris), result.c));
            return result;
        }
        return null;
    }


    public static class AnalyzeResult {
        public SparqlVar a;
        public SparqlVar c;
    }

    private static class Item {
        String id;
        String label;
        List<String> aliases = new ArrayList<>();
        String normlabel;
        List<String> normaliases = new ArrayList<>();

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Item)) {
                return false;
            }
            Item i = (Item) obj;
            return i.id.equals(id);
        }
    }

}
