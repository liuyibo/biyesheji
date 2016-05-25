package liuyibo.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liuyibo on 16-3-4.
 */
public class DataGenerator {

    private static final List<String> SENTENCE_PARSER = new ArrayList<>(Arrays.asList(
            "Actors were born in Germany",
            "The Airedale Terrier come from country",
            "Birds are there in the United States",
            "All European Capitals",
            "Tom Hanks married to XXX",
            "Capitals in Europe were host cities of games"
    ));

    private static final List<String> SENTENCE_QALD3_PART = new ArrayList<>(Arrays.asList(
            "Who was the successor of John F. Kennedy?",
            "Who is the mayor of Berlin?",
            "Give me all members of Prodigy?",
            "Give me all cars that are produced in Germany ?",
            "Give me all people that were born in Vienna and died in Berlin ?",
            "How tall is Michael Jordan ?",
            "What is the capital of Canada ?",
            "Who is the governor of Wyoming ?",
            "Who was the father of Queen Elizabeth II?",
            "Sean Parnell is the governor of which U.S. state ?",
            "Give me all movies directed by Francis Ford Coppola.",
            "What is the birth name of Angela Merkel ?",
            "Who developed Minecraft ?",
            "Give me all companies in Munich.",
            "Who founded Intel?",
            "Who is the husband of Amanda Palmer ?",
            "Which cities does the Weser flow through ?",
            "Which countries are connected by the Rhine ?",
            "What are the nicknames of San Francisco ?",
            "What is the time zone of Salt Lake City ?",
            "Give me all Argentine films.",
            "Is Michelle Obama the wife of Barack Obama ?",
            "When did Michael Jackson die ?",
            "List the children of Margaret Thatcher.",
            "Who was called Scarface?",
            "Which books by Kerouac were published by Viking Press ?",
            "How high is the Mount Everest ?",
            "Who created the comic Captain America ?",
            "What is the largest city in Australia ?",
            "In which city was the former Dutch queen Juliana buried ?",
            "Which country does the creator of Miffy come from ?",
            "Who produces Orangina ?"
    ));
    public static List<String> ARRAY1 = new ArrayList<>(Arrays.asList(
            "actors were born in Germany ? "
    ));

    private static List<String> parseXml(String filename) {
        try {
            File file = new File(filename);
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("question");
            List<String> result = new ArrayList<>();
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                String query = nNode.getFirstChild().getNextSibling().getTextContent();
                query = query.trim();
                result.add(query);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<String> QALD1() {
        return parseXml("data/1.xml");
    }

    public static List<String> QALD3t() {
        return parseXml("data/3t.xml");
    }

    public static List<String> QALD3() {
        return parseXml("data/3.xml");
    }

    public static List<String> one() {
        return Arrays.asList("In which city was Queen Juliana buried ?");
    }

    public static List<String> QALD3_part() {
        return SENTENCE_QALD3_PART;
    }

    public static List<String> get1() {
        return ARRAY1;
    }

    public static List<String> RemovePunctuation() {
        return ARRAY1;
    }

    public static List<String> SentenceParser() {
        return SENTENCE_PARSER;
    }
}
