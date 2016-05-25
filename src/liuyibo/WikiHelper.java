package liuyibo;

import liuyibo.models.Entity;
import liuyibo.sparql.*;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liuyibo on 16-3-2.
 */
public class WikiHelper {

    /**
     * query the uri of the entity with name
     * @param name
     * @return
     */
    public static String getEntity(String name) {
        return getEntityOrProp(name, false);
    }

    /**
     * query the uri of the property with name
     * @param name
     * @return
     */
    public static String getProp(String name) {
        return getEntityOrProp(name, true);
    }

    /**
     * compare the two id, for selecting a item in a item list
     * @param id1
     * @param id2
     * @return
     */
    private static boolean idLessThan(String id1, String id2) {
        return Integer.valueOf(id1.substring(1)) < Integer.valueOf(id2.substring(1));
    }

    /**
     * query the uri of the entity or property with name
     * if more than 1 items is found, we now simply select the one with smallest id
     * @param name
     * @param prop
     * @return
     */
    private static String getEntityOrProp(String name, boolean prop) {
        List<Entity> entities = WikiJsonQueryHelper.queryEntities(name, prop);
        if (entities == null || entities.isEmpty()) {
            return null;
        }
        Entity result = null;
        for (Entity entity : entities) {
            if (entity.match(name)) {
                if (result == null || idLessThan(entity.id, result.id)) {
                    result = entity;
                }
            }
        }
        if (result == null) {
            return null;
        }
        return result.uri;
    }

    /**
     * check weather a uri is a "type", eg: "fruit" is a type, while "apple" is not a type
     * the strategy is if more than 50 entities is instanse of A, A is a type
     * @param uri
     * @return
     */
    public static boolean isType(SparqlUri uri) {
        StringBuilder query = new StringBuilder();
        query.append("select (count(?a) as ?count) { ?a ")
                .append(Sparql.URI_INSTANCEOF)
                .append(" ")
                .append(uri)
                .append(". }");
        JsonObject json = WikiSparqlQueryHelper.query(query.toString());
        SparqlQueryResult result = SparqlQueryResult.createFromWikiJson(json);
        return Integer.parseInt(result.getResult(0, 0).value) > 50;
    }

    /**
     * check whether uri is a subclass of instance
     * @param uri
     * @param instance
     * @return
     */
    public static boolean isSubclass(SparqlUri uri, SparqlUri instance) {
        StringBuilder query = new StringBuilder();
        query.append("select ?a {")
                .append(uri)
                .append(" ")
                .append(SparqlRegex.star(Sparql.URI_SUBCLASS))
                .append(" ?a . FILTER(sameTerm(?a,")
                .append(instance)
                .append(")).}");
        JsonObject json = WikiSparqlQueryHelper.query(query.toString());
        SparqlQueryResult result = SparqlQueryResult.createFromWikiJson(json);
        return result.getResultCount() > 0;
    }

    /**
     * check if uri is a subproperty of property
     * @param uri
     * @param property
     * @return
     */
    public static boolean isSubproperty(SparqlUri uri, SparqlUri property) {
        StringBuilder query = new StringBuilder();
        query.append("select ?a {")
                .append(uri)
                .append(" ")
                .append(SparqlRegex.star(Sparql.URI_SUBPROPERTY))
                .append(" ?a . FILTER(sameTerm(?a,")
                .append(property)
                .append(")).}");
        JsonObject json = WikiSparqlQueryHelper.query(query.toString());
        SparqlQueryResult result = SparqlQueryResult.createFromWikiJson(json);
        return result.getResultCount() > 0;
    }

    /**
     * find all avaliable property with a subject and a object
     * @param var1
     * @param var3
     * @return
     */
    public static List<String> getAvaliableIds(SparqlItem var1, SparqlItem var3) {
        if (!(var1 instanceof SparqlUri) && !(var3 instanceof SparqlUri)) {
            return null;
        }
        StringBuilder query = new StringBuilder();
        query.append("select distinct ?b {");
        if (var1 instanceof SparqlUri) {
            query.append(var1);
        } else {
            query.append("?a");
        }
        query.append(" ?b ");
        if (var3 instanceof SparqlUri) {
            query.append(var3);
        } else {
            query.append("?c");
        }
        query.append(" . filter(strstarts(str(?b), str(wdt:))) .")
                .append("} limit 1000");
        JsonObject json = WikiSparqlQueryHelper.query(query.toString());
        SparqlQueryResult result = SparqlQueryResult.createFromWikiJson(json);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < result.getResultCount(); i++) {
            String id = result.getResult(i, 0).value;
            list.add(SparqlUri.name(id));
        }
        return list;
    }

}
