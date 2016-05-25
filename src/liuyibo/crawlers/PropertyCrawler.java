package liuyibo.crawlers;

import liuyibo.WikiJsonQueryHelper;

import javax.json.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyibo on 16-3-28.
 */
public class PropertyCrawler {
    public static void main(String[] args) {
        try {
            new PropertyCrawler().crawl();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void crawl() throws IOException {
        JsonArrayBuilder doc = Json.createArrayBuilder();
        for (int i = 0; i < 3000; i++) {
            String id = "P" + i;
            System.out.println("Property: " + id);
            JsonObject json = WikiJsonQueryHelper.queryWithId(id);
            if (json == null) {
                i--;
                continue;
            }
            if (json.get("success") == null) {
                continue;
            }
            json = json.getJsonObject("entities").getJsonObject(id);
            if (json.get("missing") != null) {
                continue;
            }

            String label = json.getJsonObject("labels").getJsonObject("en").getString("value");
            List<String> aliases = new ArrayList<>();
            JsonArray aliasar = json.getJsonObject("aliases").getJsonArray("en");
            if (aliasar != null) {
                for (int it = 0; it < aliasar.size(); it++) {
                    aliases.add(aliasar.getJsonObject(it).getString("value"));
                }
            }

            JsonObjectBuilder item = Json.createObjectBuilder();
            item.add("id", id);
            item.add("label", label);
            JsonArrayBuilder ab = Json.createArrayBuilder();
            for (String alias : aliases) {
                ab.add(alias);
            }
            item.add("aliases", ab);

            doc.add(item);
        }
        File file = new File("data/properties.json");
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write(doc.build().toString());
        writer.close();
    }
}
