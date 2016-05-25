package liuyibo;

import edu.stanford.nlp.io.StringOutputStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.zookeeper.server.ByteBufferInputStream;
import sun.net.SocksProxy;

import javax.json.Json;
import javax.json.JsonObject;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by liuyibo on 16-3-20.
 */

/**
 * http helper
 * notice:
 * 1. cache is used, data is stored in data/cache
 * 2. due to the poor stability of Wikidata online APIs, proxy is used. if you want to make it work on your machine,
 *    you need to provide socks5 proxy on localhost:8081 or modify the code in function "connect"
 */
public class HttpGetter {

    public static InputStream get(String url) {
        try {
            return connect(url);
        } catch (IOException e) {
            return null;
        }
    }

    public static JsonObject getJson(String url) {
        try {
            InputStream input = connect(url);
            JsonObject json = Json.createReader(input).readObject();
            input.close();
            return json;
        } catch (IOException e) {
            return null;
        }
    }

    private static InputStream connect(String url) throws IOException {
        String filename = DigestUtils.sha256Hex(url);
        File file = new File("data/cache/" + filename.substring(0, 2) + "/" + filename + ".txt");
        if (file.exists()) {
            return new FileInputStream(file);
        }

        HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection(SocksProxy.create(new InetSocketAddress("localhost", 1081), 5));

        conn.setRequestMethod("GET");

        InputStream input = conn.getInputStream();
        file.createNewFile();
        if (file.canWrite()) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            int c;
            while ((c = input.read()) != -1) {
                bytes.write(c);
            }
            input.close();

            FileOutputStream output = new FileOutputStream(file);
            output.write(bytes.toByteArray());
            output.close();

            return new ByteArrayInputStream(bytes.toByteArray());
        }
        return input;
    }
}
