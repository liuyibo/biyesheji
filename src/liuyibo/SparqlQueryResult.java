package liuyibo;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyibo on 16-3-2.
 */
public class SparqlQueryResult {

    public class Item {
        public String type;
        public String value;
    }

    private String mString;
    private int mColumnCount;
    private int mResultCount;
    private List<String> mVarName = new ArrayList<>();
    private List<List<Item>> mResult = new ArrayList<>();

    public int getColumnCount() {
        return mColumnCount;
    }

    public String getVarName(int index) {
        return mVarName.get(index);
    }

    public int getResultCount() {
        return mResultCount;
    }

    public Item getResult(int index, int column) {
        return mResult.get(index).get(column);
    }


    private boolean initFromWikiJson(JsonObject json) {
        if (json == null) {
            return false;
        }
        mString = json.toString();
        JsonObject head = json.getJsonObject("head");
        JsonObject results = json.getJsonObject("results");
        if (head == null || results == null) {
            return false;
        }
        JsonArray vars = head.getJsonArray("vars");
        if (vars == null) {
            return false;
        }
        mColumnCount = vars.size();
        for (int i = 0; i < mColumnCount; i++) {
            mVarName.add(vars.getString(i));
        }
        JsonArray ar = results.getJsonArray("bindings");
        if (ar == null) {
            return false;
        }
        mResultCount = ar.size();
        for (int i = 0; i < mResultCount; i++) {
            JsonObject obj = ar.getJsonObject(i);
            List<Item> result = new ArrayList<>();
            for (int j = 0; j < mColumnCount; j++) {
                String var = mVarName.get(j);
                JsonObject data = obj.getJsonObject(var);
                Item item = new Item();
                if (data == null) {
                    result.add(item);
                    continue;
                }
                item.type = data.getString("type");
                item.value = data.getString("value");
                result.add(item);
            }
            mResult.add(result);
        }
        return true;
    }

    @Override
    public String toString() {
        if (mResultCount == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        StringBuilder sep = new StringBuilder();
        for (int i = 0; i < 50 * mColumnCount; i++) {
            sep.append('-');
        }
        sep.append("\n");
        sb.append(sep);
        for (int i = 0; i < mColumnCount; i++) {
            sb.append(String.format("%-50s", mVarName.get(i)));
        }
        sb.append("\n");
        sb.append(sep);
        for (int i = 0; i < mResultCount; i++) {
            for (int j = 0; j < mColumnCount; j++) {
                sb.append(String.format("%-50s", mResult.get(i).get(j).value));
            }
            sb.append("\n");
        }
        sb.append(sep);
        return sb.toString();
    }

    public String toJsonString() {
        return mString;
    }

    public static SparqlQueryResult createFromWikiJson(JsonObject json) {
        SparqlQueryResult result = new SparqlQueryResult();
        if (!result.initFromWikiJson(json)) {
            return null;
        }
        return result;
    }
}
