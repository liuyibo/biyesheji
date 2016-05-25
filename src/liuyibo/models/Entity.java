package liuyibo.models;

import java.util.List;

/**
 * Created by liuyibo on 16-3-7.
 */
public class Entity {
    public String id;
    public String uri;
    public String label;
    public String description;
    public List<String> aliases;

    public boolean match(String word) {
        if (label == null || id == null || uri == null || description == null) {
            return false;
        }
        if (label.equals(word)) {
            return true;
        }
        if (aliases != null) {
            for (String str : aliases) {
                if (str.equals(word)) {
                    return true;
                }
            }
        }
        return false;
    }
}
