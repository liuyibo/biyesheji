package liuyibo;

import com.github.jsonldjava.utils.Obj;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import liuyibo.methods.AdjRelationAnalyzer;
import liuyibo.methods.NounRelationAnalyzer;
import liuyibo.methods.VerbRelationAnalyzer;
import liuyibo.models.Tree;
import liuyibo.models.Tree.NodeData;
import liuyibo.models.Tree.NodeData.VP;
import liuyibo.sparql.*;
import org.apache.jena.sparql.resultset.SPARQLResult;

import java.util.*;

/**
 * Created by liuyibo on 16-3-6.
 */

/**
 * analyse the relations of the sentence
 */
public class SentenceParser {

    private Tree tree;
    private SemanticGraph graph;
    private SparqlBuilder sb;
    private List<Statement> stmts;
    private Result result;
    private SentenceFormTranslator.Result result0;

    public static class Result {
        SparqlQueryResult sparqlResult;
    }

    public SentenceParser(SentenceFormTranslator.Result result0) {
        Query mSentence = new Query(result0.words);
        this.result0 = result0;
        tree = mSentence.tree();
        graph = mSentence.simpleDependencyGraph();
        sb = new SparqlBuilder();
        stmts = new ArrayList<>();
        result = new Result();
    }

    public Result process() {
        process(tree);

        for (int i = 0; i < stmts.size(); i++) {
            Statement stmt = stmts.get(i);
            stmt.sparql();
            System.out.println("STAT : [" + stmt.v1 + "]  [" + stmt.v2 + "]  [" + stmt.v3 + "]");
        }
        if (sb.empty()) {
            throw new ParseException();
        }
        String query = sb.build();
        System.out.println(query);


        SparqlQueryResult sparqlResult = SparqlQueryResult.createFromWikiJson(WikiSparqlQueryHelper.query(query));
        if (sparqlResult == null) {
            throw new ParseException();
        }

        result.sparqlResult = sparqlResult;

        return result;
    }

    private void process(Tree tree) {

        if (tree.isLeaf()) {
            return;
        }

        for (Tree child : tree.children()) {
            process(child);
        }

        List<Edge> edges = getEdges(tree);
        if (edges.size() != tree.numChildren() - 1) {
            error();
        }
        sortEdges(edges);

        Tree core = null;
        for (Tree child : tree.children()) {
            boolean ok = true;
            for (Edge e : edges) {
                if (e.dep == child) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                core = child;
                break;
            }
        }

        NodeData data = tree.data;

        String v = tree.value();
        if (v.equals("NP")) {
            // :noun
            data.noun = tree;
            data.core = core;
            for (Edge e : edges) {
                if (e.rel.equals("nmod") && "of".equals(e.relspec)) {
                    assertType(e, "NP", "PP");
                    data.out = newVar();
                    new Statement(e.dep.data.noun, e.gov.data.noun, tree);
                } else if (e.rel.equals("nmod")) {
                    assertType(e, "NP", "PP");
                    new Statement(e.gov.data.noun, e.dep.data.prop, e.dep.data.noun);
                } else if (e.rel.startsWith("acl")) {
                    for (VP vp : e.dep.data.vp) {
                        new Statement(e.gov.data.noun, vp.verb, vp.obj, vp.rev);
                    }
                } else if (e.rel.equals("amod") || e.rel.equals("compound")) {
                    String adj = e.dep.toString();
                    SparqlVar out = AdjRelationAnalyzer.analyze(adj, sb);
                    if (out != null) {
                        new Statement(out, "EQUALS", e.gov.data.noun);
                    }
                }
            }
        } else if (v.equals("VP")) {
            // :vp
            for (Edge e : edges) {
                if (e.rel.equals("nmod")) {
                    if (equalsType(e, "VB", "PP")) {
                        data.vp.add(new VP(
                                e.gov.data.verb + " " + e.dep.data.prop,
                                e.dep.data.noun, false));
                    } else if (equalsType(e, "NP", "PP")) {
                        new Statement(e.gov, e.dep.data.prop, e.dep.data.noun);
                    } else {
                        error();
                    }
                } else if (e.rel.equals("auxpass")) {
                    assertType(e, "VP", "VB");
                    String v0 = e.dep.data.verb;
                    for (VP vp : e.gov.data.vp) {
                        data.vp.add(new VP(v0 + " " + vp.verb, vp.obj, false));
                    }
                } else if (e.rel.equals("cop")) {
                    if (equalsType(e, "NP", "VB")) {
                        data.vp.add(new VP("EQUALS", e.gov.data.noun, false));
                    } else if (equalsType(e, "ADJP", "VB")) {

                    } else {
                        error();
                    }
                } else if (e.rel.equals("dobj")) {
                    assertType(e, "VB", "NP");
                    data.vp.add(new VP(e.gov.data.verb, e.dep.data.noun, true));
                }
            }
        } else if (v.equals("PP")) {
            // :noun :prop
            for (Edge e : edges) {
                if (e.rel.equals("case")) {
                    data.noun = e.gov.data.noun;
                    data.prop = e.dep.toString();
                } else {
                    print(e);
                }
            }
        } else if (v.equals("S")) {
            for (Edge e : edges) {
                if (e.rel.equals("nsubj") || e.rel.equals("nsubjpass")) {
                    assertType(e, "VP", "NP");
                    for (VP vp : e.gov.data.vp) {
                        new Statement(e.dep.data.noun, vp.verb, vp.obj, vp.rev);
                    }
                }
            }
        } else if (tree.numChildren() == 1) {
            if (v.startsWith("V")) {
                data.verb = tree.getChild(0).value();
            } else if (v.equals("IN") || v.equals("TO")) {
                data.prop = tree.getChild(0).value();
            } else if (v.startsWith("N")) {
                data.noun = tree;
            }
        }
        if (data.nothing()) {
            tree.data = new NodeData(core.data);
        }
    }

    private void sortEdges(List<Edge> edges) {
        int n = edges.size();
        List<Edge> result = new ArrayList<>();
        edges.sort((e1, e2) -> e1.dep.firstLeafIndex() - e2.dep.firstLeafIndex());
        for (int i = 0; i < n; i++) {
            for (Edge e : edges) {
                boolean ok = true;
                for (Edge e2 : edges) {
                    if (e.dep == e2.gov) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    result.add(e);
                    edges.remove(e);
                    break;
                }
            }
        }
        edges.clear();
        edges.addAll(result);
    }

    private SparqlVar newVar() {
        return sb.newVar();
    }

    private boolean equalsType(Edge e, String type1, String type2) {
        return e.gov.value().startsWith(type1) && e.dep.value().startsWith(type2);
    }

    private void assertType(Edge e, String type1, String type2) {
        assertType(e.gov, type1);
        assertType(e.dep, type2);
    }

    private void assertType(Tree tree, String type) {
        if (!tree.value().startsWith(type)) {
            error();
        }
    }

    private void error() {
        throw new ParseException();
    }

    private void print(Edge e) {
        System.out.println("NO REL: " + e.gov.toString() + "   " + e.dep.toString());
    }

    private List<Edge> getEdges(Tree tree) {
        int n = tree.numChildren();
        int start[] = new int[n];
        int end[] = new int[n];
        for (int i = 0; i < n; i++) {
            Tree child = tree.getChild(i);
            start[i] = child.firstLeafIndex();
            end[i] = child.lastLeafIndex();
        }
        List<Edge> edges = new ArrayList<>();
        for (SemanticGraphEdge e : graph.edgeListSorted()) {
            int x = e.getGovernor().index() - 1;
            int y = e.getDependent().index() - 1;
            int xi = -1;
            int yi = -1;

            for (int i = 0; i < n; i++) {
                if (start[i] <= x && x <= end[i]) {
                    xi = i;
                }
                if (start[i] <= y && y <= end[i]) {
                    yi = i;
                }
            }
            if (xi != -1 && yi != -1 && xi != yi) {
                Edge edge = new Edge();
                edge.rel = e.getRelation().getShortName();
                edge.relspec = e.getRelation().getSpecific();
                edge.gov = tree.getChild(xi);
                edge.dep = tree.getChild(yi);
                edges.add(edge);
            }
        }
        return edges;
    }

    private static class Edge {
        Tree gov;
        Tree dep;
        String rel;
        String relspec;
    }

    private class Statement {
        // String or Tree
        Object v1;
        Object v2;
        Object v3;
        Statement(Object v1, Object v2, Object v3) {
            this(v1, v2, v3, false);
        }
        Statement(Object v1, Object v2, Object v3, boolean rev) {
            if (rev) {
                init(v3, v2, v1);
            } else {
                init(v1, v2, v3);
            }
        }
        private void init(Object v1, Object v2, Object v3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            stmts.add(this);
        }

        public void sparql() {
            v1 = toSparql(v1);
            v3 = toSparql(v3);
            addline();
        }

        private void addline() {
            Object v = v2;
            if (v instanceof SparqlItem) {
            } else {
                String str = null;
                if (v instanceof Tree) {
                    Tree t = ((Tree) v);
                    while (t.data.out == null && t.data.core != null) {
                        t = t.data.core;
                    }
                    str = t.toWordString();
                } else {
                    str = v.toString();
                }
                if (str.equals("EQUALS")) {
                    sb.equalVar((SparqlItem) v1, (SparqlItem) v3);
                    return;
                }
                VerbRelationAnalyzer.AnalyzeResult result = VerbRelationAnalyzer.analyze(
                        str, sb, (SparqlItem) v1, (SparqlItem) v3);
                sb.equalVar(result.a, (SparqlItem) v1);
                sb.equalVar(result.c, (SparqlItem) v3);
            }
        }

        private SparqlItem toSparql(Object v) {
            if (v instanceof SparqlItem) {
                return (SparqlItem) v;
            } else if (v instanceof Tree) {
                Tree t = ((Tree) v);
                while (t.data.out == null && t.data.core != null) {
                    t = t.data.core;
                }
                if (t.data.out == null) {
                    if (result0.focusWord != -1 && result0.isPlaceHolder &&
                            t.lastLeafIndex() == result0.focusWord) {
                        SparqlVar var = newVar();
                        t.data.out = var;
                        sb.select(var);
                        if (result0.focusType == SentenceFormTranslator.Result.FocusType.TIME) {
                            sb.line(new SparqlLineEx(Arrays.asList("FILTER(datatype(", var, ")=xsd:dateTime).")));
                        } else if (result0.focusType == SentenceFormTranslator.Result.FocusType.PERSON) {
                            SparqlVar ins = sb.newVar();
                            SparqlVar ins2 = sb.newVar();
                            sb.line(new SparqlLineTriple(var, Sparql.URI_INSTANCEOF, ins));
                            sb.line(new SparqlLineTriple(ins, SparqlRegex.star(Sparql.URI_SUBCLASS), ins2));
                            sb.line(new SparqlLineTriple(ins2, SparqlRegex.star(Sparql.URI_HASPART), Sparql.URI_HUMAN));
                        } else if (result0.focusType == SentenceFormTranslator.Result.FocusType.LOCATION) {
                            SparqlVar ins = sb.newVar();
                            sb.line(new SparqlLineTriple(var, Sparql.URI_INSTANCEOF, ins));
                            sb.line(new SparqlLineTriple(ins, SparqlRegex.star(Sparql.URI_SUBCLASS), Sparql.URI_GEOLOCATION));
                        }
                        return var;
                    }
                    String text = t.toWordString();

                    t.data.out = NounRelationAnalyzer.analyze(text, sb);
                }
                return t.data.out;
            } else {
                throw new ParseException();
            }
        }
    }
}
