package liuyibo.models;

import liuyibo.sparql.SparqlItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyibo on 16-3-22.
 */

/**
 * wrapper of NLP.Tree
 */
public class Tree {
    private Tree root;
    private String value;
    private Word word;
    private List<Tree> leaves;
    private List<Tree> children;
    private Tree parent;
    public NodeData data;

    protected Tree() {

    }

    public Tree(edu.stanford.nlp.trees.Tree tree) {
        root = this;
        init(tree);
    }

    private void init(edu.stanford.nlp.trees.Tree tree0) {
        data = new NodeData();
        value = tree0.value();
        leaves = new ArrayList<>();
        children = new ArrayList<>();
        if (tree0.isLeaf()) {
            leaves.add(this);
        }
        for (edu.stanford.nlp.trees.Tree child0 : tree0.children()) {
            Tree child = new Tree();
            child.root = root;
            child.parent = this;
            child.init(child0);
            children.add(child);
            leaves.addAll(child.leaves);
        }
    }

    public boolean isPhrase(String type) {
        return isPhrase() && value.startsWith(type);
    }

    public boolean isPhrase() {
        return value.matches("^NP|PP|VP|ADVP|ADJP$");
    }

    public List<Tree> getLeaves() {
        List<Tree> list = new ArrayList<>(leaves);
        return list;
    }

    public Tree firstLeaf() {
        return leaves.get(0);
    }

    public int firstLeafIndex() {
        return root.leaves.indexOf(leaves.get(0));
    }

    public Tree lastLeaf() {
        return leaves.get(leaves.size() - 1);
    }

    public int lastLeafIndex() {
        return root.leaves.indexOf(leaves.get(leaves.size() - 1));
    }

    public Tree leaf(int index) {
        return leaves.get(index);
    }

    public Tree parent() {
        return parent;
    }

    public int height() {
        int h = 0;
        Tree tree = this;
        while (tree != root) {
            tree = tree.parent;
            h++;
        }
        return h;
    }

    public Tree largestTreeBackward(int index) {
        Tree p = leaves.get(index);
        while (true) {
            if (p == root) {
                return p;
            }
            if (p.parent().firstLeafIndex() < index) {
                return p;
            }
            p = p.parent();
        }
    }

    public Tree largestPhraseBackward(int index, String type) {
        if (firstLeafIndex() == index && value.startsWith(type)) {
            return this;
        }
        for (Tree child : children) {
            if (child.firstLeafIndex() <= index && child.lastLeafIndex() >= index) {
                return child.largestPhraseBackward(index, type);
            }
        }
        return null;
    }

    public int numChildren() {
        return children.size();
    }

    public Tree getChild(int i) {
        return children.get(i);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public String value() {
        return value;
    }

    public Tree[] children() {
        Tree[] ar = new Tree[children.size()];
        children.toArray(ar);
        return ar;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leaves.size(); i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(leaves.get(i).value);
        }
        return sb.toString();
    }

    public String toWordString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leaves.size(); i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(leaves.get(i).word.regword);
        }
        return sb.toString();
    }

    public String toPennString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(value);
        sb.append(" ");
        for (Tree child : children) {
            sb.append(child.toPennString());
        }
        sb.append(")");
        return sb.toString();
    }

    public void setWord(int i, Word word) {
        leaves.get(i).word = word;
    }

    public Tree firstVerb() {
        for (Tree child : children) {
            if (child.value.startsWith("V")) {
                return child;
            }
            if (child.isPhrase()) {
                continue;
            }
            Tree tree = child.firstVerb();
            if (tree != null) {
                return tree;
            }
        }
        return null;
    }


    public static class NodeData {

        public static class VP {
            public boolean rev;
            public String verb;
            public Tree obj;
            public VP(String verb, Tree obj, boolean rev) {
                this.verb = verb;
                this.obj = obj;
                this.rev = rev;
            }
        }
        public String prop;
        public Tree noun;
        public Tree core;
        public String verb;
        public List<VP> vp = new ArrayList<>();
        public SparqlItem out;

        public NodeData() {

        }
        public NodeData(NodeData a) {
            this.prop = a.prop;
            this.noun = a.noun;
            this.verb = a.verb;
            this.vp.addAll(a.vp);
            this.out = a.out;
        }


        public boolean nothing() {
            if (prop != null || noun != null || verb != null || !vp.isEmpty()) {
                return false;
            }
            return true;
        }
    }

}
