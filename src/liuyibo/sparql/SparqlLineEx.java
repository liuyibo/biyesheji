package liuyibo.sparql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyibo on 16-4-4.
 */

/**
 * a sparql string
 * usually items can be a string, or a SparqlVar
 */
public class SparqlLineEx extends SparqlLine {

    List<Object> items = new ArrayList<>();

    public SparqlLineEx(List<Object> objs) {
        for (Object obj : objs) {
            items.add(obj);
        }
    }

    @Override
    public boolean applyEquals(Map<SparqlItem, SparqlItem> equals) {
        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            if (!(item instanceof SparqlItem)) {
                continue;
            }
            SparqlItem sitem = (SparqlItem) item;
            if (equals.containsKey(sitem)) {
                items.set(i, equals.get(sitem));
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i));
        }
        return sb.toString();
    }
}
