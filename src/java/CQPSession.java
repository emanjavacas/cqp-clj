import java.nio.charset.Charset;

public class CQPSession {
    private static final int BUFFER_SIZE = 10;
    private static final int MATCH_START = 0;
    private static final int MATCH_END = 1;
    private static final int TARGET = 2;
    private static final int CONTEXT_START = 3;
    private static final int CONTEXT_END = 4;
    private final CqiClient client;
    private static int port;    
    private static String host;
    private static String user;
    private static String pass;
    private String corpus;
    private String subcorpus;
    private Charset charset;
    
    CQPSession(int port, String host, String user, String pass) {
	this.port = port;
	this.host = host;
	this.user = user;
	this.pass = pass;
	try {
	    this.client = new CqiClient(host, port);
	    client.connect(user, pass);
	} catch (CqiClientException e) {
	    throw new IllegalArgumentException("Could not connet to server");
	}
    }
}
