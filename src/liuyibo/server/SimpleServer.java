package liuyibo.server;

import liuyibo.NLP;
import liuyibo.ParseException;
import liuyibo.SparqlQueryResult;

import javax.json.Json;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.ByteBuffer;


public class SimpleServer extends Thread {

    private int port = 5000;

    private byte[] read(String filepath) {
        try {
            InputStream in = new FileInputStream("data/server" + filepath);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            byte[] data = out.toByteArray();
            return data;
        } catch (IOException e) {
            return null;
        }
    }

    public SimpleServer() throws IOException {
    }

    private byte[] getHeader(byte[] content) throws UnsupportedEncodingException {
        String header = "HTTP/1.0 200 OK\r\n" +
                "Content-length: " + content.length + "\r\n\r\n";
        return header.getBytes("ASCII");
    }

    public void run() {
        try {
            ServerSocket server = new ServerSocket(this.port);
            System.out.println("Accepting connections on port " + server.getLocalPort());

            while (true) {
                Socket connection = null;
                try {
                    connection = server.accept();
                    OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String req = in.readLine();
                    if (req.startsWith("POST /query ")) {
                        String line;
                        int contentLength = 0;
                        while (true) {
                            line = in.readLine();
                            if (line == null || line.isEmpty()) {
                                break;
                            }
                            final String contentHeader = "Content-Length: ";
                            if (line.startsWith(contentHeader)) {
                                contentLength = Integer.parseInt(line.substring(contentHeader.length()));
                            }
                        }

                        char[] buf = new char[contentLength];
                        in.read(buf);
                        String query = new String(buf);

                        SparqlQueryResult result = NLP.analyze(query);
                        if (result != null) {
                            byte[] data = Json.createObjectBuilder()
                                    .add("result", "1")
                                    .add("data", new ServerQueryResult(result).toJson())
                                    .build()
                                    .toString()
                                    .getBytes();
                            out.write(getHeader(data));
                            out.write(data);
                            out.flush();
                        } else {
                            byte[] data = Json.createObjectBuilder()
                                    .add("result", 0)
                                    .build().toString().getBytes();
                            out.write(getHeader(data));
                            out.write(data);
                            out.flush();
                        }
                    } else if (req.startsWith("GET")) {
                        String filepath = req.split(" ")[1];
                        if (filepath.equals("/")) {
                            filepath = "/index.html";
                        }
                        byte[] data = read(filepath);
                        if (data != null) {
                            out.write(getHeader(data));
                            out.write(data);
                            out.flush();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Could not start server.");
        }
    }

    public static void main(String[] args) {
        System.out.println("Initializing System...");
        NLP.init();
        System.out.println("System is Ready.");

        try {
            Thread t = new SimpleServer();
            t.start();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}

