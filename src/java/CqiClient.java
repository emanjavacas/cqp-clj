/**
 * 
 * This software is copyright (c) 2015 by Enrique Manjavacas
 * This is free software. You can redistribute it and/or modify it under the
 * terms described in the GNU General Public License v3 of which you should
 * have received a copy. Otherwise you can download it from
 *
 * http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright 2015 Enrique Manjavacas
 * @copyright 2012 Seminar fuer Sprachwissenschaft (http://www.sfs.uni-tuebingen.de/)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt GNU General Public License
 * v3
 */

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;


public class CqiClient {

    private static final byte[] CQI_PADDING = {(byte) 0x00};
    private static final byte[] CQI_STATUS_OK = {(byte) 0x01, (byte) 0x01};
    private static final byte[] CQI_STATUS_CONNECT_OK = {(byte) 0x01, (byte) 0x02};
    private static final byte[] CQI_STATUS_BYE_OK = {(byte) 0x01, (byte) 0x03};
    private static final byte[] CQI_STATUS_PING_OK = {(byte) 0x01, (byte) 0x04};
    private static final byte[] CQI_DATA_BYTE = {(byte) 0x03, (byte) 0x01};
    private static final byte[] CQI_DATA_BOOL = {(byte) 0x03, (byte) 0x02};
    private static final byte[] CQI_DATA_INT = {(byte) 0x03, (byte) 0x03};
    private static final byte[] CQI_DATA_STRING = {(byte) 0x03, (byte) 0x04};
    private static final byte[] CQI_DATA_BYTE_LIST = {(byte) 0x03, (byte) 0x05};
    private static final byte[] CQI_DATA_BOOL_LIST = {(byte) 0x03, (byte) 0x06};
    private static final byte[] CQI_DATA_INT_LIST = {(byte) 0x03, (byte) 0x07};
    private static final byte[] CQI_DATA_STRING_LIST = {(byte) 0x03, (byte) 0x08};
    private static final byte[] CQI_DATA_INT_INT = {(byte) 0x03, (byte) 0x09};
    private static final byte[] CQI_DATA_INT_INT_INT_INT = {(byte) 0x03, (byte) 0x0A};
    private static final byte[] CQI_DATA_INT_TABLE = {(byte) 0x03, (byte) 0x0B};
    private static final byte[] CQI_CTRL_CONNECT = {(byte) 0x11, (byte) 0x01};
    private static final byte[] CQI_CTRL_BYE = {(byte) 0x11, (byte) 0x02};
    private static final byte[] CQI_CTRL_LAST_GENERAL_ERROR = {(byte) 0x11, (byte) 0x05};
    private static final byte[] CQI_CTRL_LAST_CQP_ERROR = {(byte) 0x11, (byte) 0x06};
    private static final byte[] CQI_CORPUS_LIST_CORPORA = {(byte) 0x13, (byte) 0x01};
    private static final byte[] CQI_CORPUS_CHARSET = {(byte) 0x13, (byte) 0x03};
    private static final byte[] CQI_CORPUS_POSITIONAL_ATTRIBUTES = {(byte) 0x13, (byte) 0x05};
    private static final byte[] CQI_CORPUS_STRUCTURAL_ATTRIBUTES = {(byte) 0x13, (byte) 0x06};
    private static final byte[] CQI_CORPUS_STRUCTURAL_ATTRIBUTE_HAS_VALUES = {(byte) 0x13, (byte) 0x07};
    private static final byte[] CQI_CORPUS_FULL_NAME = {(byte) 0x13, (byte) 0x09};
    private static final byte[] CQI_CL_CPOS2STR = {(byte) 0x14, (byte) 0x08};
    private static final byte[] CQI_CL_CPOS2STRUC = {(byte) 0x14, (byte) 0x09};
    private static final byte[] CQI_CL_CPOS2LBOUND = {(byte) 0x14, (byte) 0x20};
    private static final byte[] CQI_CL_CPOS2RBOUND = {(byte) 0x14, (byte) 0x21};
    private static final byte[] CQI_CL_STRUC2STR = {(byte) 0x14, (byte) 0x0B};
    private static final byte[] CQI_CQP_QUERY = {(byte) 0x15, (byte) 0x01};
    private static final byte[] CQI_CQP_LIST_SUBCORPORA = {(byte) 0x15, (byte) 0x02};    
    private static final byte[] CQI_CQP_SUBCORPUS_SIZE = {(byte) 0x15, (byte) 0x03};
    private static final byte[] CQI_CQP_DUMP_SUBCORPUS = {(byte) 0x15, (byte) 0x05};
    private static final byte[] CQI_CQP_DROP_SUBCORPUS = {(byte) 0x15, (byte) 0x09};
    private static final Charset DEFAULT_CHARSET = Charset.forName("ASCII");
    public static final byte CQI_CONST_FIELD_MATCH = (byte) 0x10;
    public static final byte CQI_CONST_FIELD_MATCHEND = (byte) 0x11;
    public static final byte CQI_CONST_FIELD_TARGET = (byte) 0x00;
    /**
     * Error messages
     */
    private static final String UNEXPECTED_ANSWER = "Unexpected answer";
    private static final String SERVER_NOT_FOUND = "Server not found";
    private static final String INTERNAL_ERROR = "Internal error";
    private static final String INTERNAL_CQI_ERROR = "Internal CQI error";
    private static final String INTERNAL_CQP_ERROR = "Internal CQP error";
    private static final String INTERNAL_CL_ERROR = "Internal CL error";
    private static final String OUT_OF_MEMORY_ERROR = "Out of memory";
    private static final String CORPUS_ACCESS_ERROR = "Corpus access error";
    private static final String WRONG_ATTRIBUTE_TYPE_ERROR = "Wrong attribute type";
    private static final String OUT_OF_RANGE_ERROR = "Out of range";
    private static final String REGEX_ERROR = "Regex error";
    private static final String NO_SUCH_ATTRIBUTE_ERROR = "No such attribute";
    private static final String NO_SUCH_CORPUS_CQP_ERROR = "No such corpus";
    private static final String INVALID_FIELD_CQP_ERROR = "Invalid field";
    private static final String OUT_OF_RANGE_CQP_ERROR = "Out of range";
    private static final String SYNTAX_CQP_ERROR = "CQP Syntax error";
    private static final String GENERAL_CQP_ERROR = "General CQP error";
    private static final String CONNECTION_REFUSED_ERROR = "Connection refused";
    private static final String USER_ABORT_ERROR = "User abort";
    private static final String SYNTAX_ERROR = "Syntax error";
    private static final String GENERAL_ERROR = "General error";
    private static final String INSUFFICIENT_BUFFER_SIZE = "Insufficient buffer size";
    private static final String SERVER_IO_ERROR = "IO Error while communicating to the server";
    /**
     * Constants
     */
    private static final String DEFAULT_SUBCORPUS_NAME = "Results";
    private static final int BYTE_BUFFER_SIZE = 40;
    private static final int BUFFER_SIZE = 10;
    private Socket socket;
    private SocketAddress serverAddress;
    private DataOutput streamToServer;
    private DataInput streamFromServer;
    private final int[][] buffer = new int[3][BUFFER_SIZE];
    private final byte[] byteBuffer = new byte[BYTE_BUFFER_SIZE];

    /**
     * Instantiates a new cqi client.
     *
     * @param host the host of the CQI server
     * @param port the port of the CQI server
     *
     * @throws CqiClientException the server not found exception
     */
    public CqiClient(String host, int port) throws CqiClientException {
        try {
            this.socket = new Socket();
            this.serverAddress = new InetSocketAddress(host, port);
            this.socket.connect(serverAddress);
            this.streamToServer = new DataOutputStream(this.socket.getOutputStream());
            this.streamFromServer = new DataInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            throw new CqiClientException(SERVER_NOT_FOUND, e);
        }
    }

    /**
     * Connect the client to a server
     *
     * @param username the username
     * @param password the password
     * @return true, if successful
     * @throws CqiClientException
     */
    public synchronized boolean connect(String username, String password)
            throws CqiClientException {
        try {
            this.streamToServer.write(CQI_CTRL_CONNECT);
            this.writeString(username);
            this.writeString(password);
            return (readHeaderFromServer() == CQI_STATUS_CONNECT_OK);
        } catch (IOException ex) {
            throw new CqiClientException(SERVER_IO_ERROR, ex);
        }
    }

    /**
     * Disconnect
     *
     * @return true, if successful
     *
     * @throws CqiClientException
     */
    public synchronized boolean disconnect() throws CqiClientException {
        try {
            this.streamToServer.write(CQI_CTRL_BYE);
            return (readHeaderFromServer() == CQI_STATUS_BYE_OK);
        } catch (IOException ex) {
            throw new CqiClientException(SERVER_IO_ERROR, ex);
        }
    }

    /**
     * Lists the corpora available on the server
     *
     * @return the name of the corpora
     * @throws CqiClientException
     */
    public synchronized String[] listCorpora() throws CqiClientException {
        try {
            this.streamToServer.write(CQI_CORPUS_LIST_CORPORA);
            return readStringArray(DEFAULT_CHARSET);
        } catch (IOException e) {
            throw new CqiClientException(SERVER_IO_ERROR, e);
        }
    }

    /**
     * Lists the corpora available on the server
     * @param corpus the corpus from which to extract subcorpus names
     * @return the name of the subcorpora
     * @throws CqiClientException
     */
    public synchronized String[] listSubcorpora(String corpus) throws CqiClientException {
	try {
	    this.streamToServer.write(CQI_CQP_LIST_SUBCORPORA);
	    this.writeString(corpus);
	    return readStringArray(DEFAULT_CHARSET);
	} catch (IOException e) {
	    throw new CqiClientException(SERVER_IO_ERROR, e);
	}
    }

    /**
     * Gives the corpus positional attributes.
     *
     * @param corpus the corpus id
     * @return the name of the attributes
     * @throws CqiClientException
     */
    public synchronized String[] corpusPositionalAttributes(String corpus)
            throws CqiClientException {
        return genericStringToStringArray(corpus,
                CQI_CORPUS_POSITIONAL_ATTRIBUTES);
    }

    /**
     * Gives the corpus structural attributes.
     *
     * @param corpus the corpus
     * @return the name of the attributes
     * @throws CqiClientException
     */
    public synchronized String[] corpusStructuralAttributes(String corpus)
            throws CqiClientException {
        return genericStringToStringArray(corpus,
                CQI_CORPUS_STRUCTURAL_ATTRIBUTES);
    }

    /**
     * Gives the corpus charset.
     *
     * @param corpus the corpus
     * @return the name of the charset
     * @throws CqiClientException Signals that the data read on the socket is
     * unexpected
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws CqiServerError Signals that the cqi server raised an error
     */
    public synchronized String corpusCharset(String corpus)
            throws CqiClientException, IOException {
        return genericStringToString(corpus, CQI_CORPUS_CHARSET);
    }

    /**
     * Gives the corpus full name.
     *
     * @param corpus the corpus
     * @return the full name
     * @throws CqiClientException Signals that the data read on the socket is unexpected
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized String corpusFullName(String corpus)
            throws CqiClientException, IOException {
        return genericStringToString(corpus, CQI_CORPUS_FULL_NAME);
    }

    /**
     * Gives positional attribute for a range of positions. Wraps cpos2Str.
     * @param corpus the corpus name
     * @param attribute the attribute name. Example: "word"
     * @param fromPosition starting position
     * @param toPosition end position
     * @param charset charset object specifying the charset
     * @param strCharset string specifying the charset
     */
    public synchronized String[] dumpPositionalAttributes(String corpus, String attribute, 
	int[] cpos, Charset charset) throws CqiClientException {
	return cpos2Str(String.format("%s.%s", corpus, attribute), cpos, charset);
    }

    public synchronized String[] dumpPositionalAttributes(String corpus, String attribute, 
        int[] cpos)  throws CqiClientException {
	Charset charset;
	try {
	    charset = Charset.forName(corpusCharset(corpus));
	} catch (IOException e) {
	    throw new CqiClientException(SERVER_IO_ERROR, e);
	}
	return dumpPositionalAttributes(corpus, attribute, cpos, charset);
    }

    public synchronized String[] dumpPositionalAttributes(String corpus, String attribute, 
             int fromPosition, int toPosition, Charset charset) throws CqiClientException {
	return cpos2Str(String.format("%s.%s", corpus, attribute), fromPosition, toPosition, charset); 
    }

    public synchronized String[] dumpPositionalAttributes(String corpus, String attribute, 
             int fromPosition, int toPosition) throws CqiClientException {
	Charset charset;
	try {
	    charset = Charset.forName(corpusCharset(corpus));
	} catch (IOException e) {
	    throw new CqiClientException(SERVER_IO_ERROR, e);
	} 
	return dumpPositionalAttributes(corpus, attribute, fromPosition, toPosition, charset);
    }

    public synchronized String[] dumpPositionalAttributes(String corpus, String attribute, 
             int[] cpos, String strCharset)  throws CqiClientException {
	Charset charset = Charset.forName(strCharset);
	return dumpPositionalAttributes(corpus, attribute, cpos, charset);
    }

    public synchronized String[] dumpPositionalAttributes(String corpus, String attribute, 
             int fromPosition, int toPosition, String strCharset) throws CqiClientException {
	Charset charset = Charset.forName(strCharset);
	return dumpPositionalAttributes(corpus, attribute, fromPosition, toPosition, charset);
    }

    /**
     * Gives positional attribute for a range of positions. Wraps cpos2Str.
     * @param corpus the corpus name
     * @param attribute the attribute name. Example: "word"
     * @param fromPosition starting position
     * @param toPosition end position
     * @param charset charset object specifying the charset
     * @param strCharset string specifying the charset
     */

    public synchronized String[] dumpStructuralAttributes(String corpus, String attribute, int[] cpos, Charset charset) throws CqiClientException {
	String attributeName = String.format("%s.%s", corpus, attribute);
	int[] strucs = cpos2Struc(attributeName, cpos);
	return struc2Str(attributeName, strucs, charset);
    }

    public synchronized String[] dumpStructuralAttributes(String corpus, String attribute, int[] cpos) 
	throws CqiClientException {
	Charset charset;
	try {
	    charset = Charset.forName(corpusCharset(corpus));
	} catch (IOException e) {
	    throw new CqiClientException(SERVER_IO_ERROR, e);
	}
	String attributeName = String.format("%s.%s", corpus, attribute);
	int[] strucs = cpos2Struc(attributeName, cpos);
	return struc2Str(attributeName, strucs, charset);
    }

    public synchronized String[] dumpStructuralAttributes(String corpus, String attribute, 
           int fromPosition, int toPosition, Charset charset) throws CqiClientException {
	String attributeName = String.format("%s.%s", corpus, attribute);
	int[] strucs = cpos2Struc(attributeName, fromPosition, toPosition);
	return struc2Str(attributeName, strucs, charset);
    }

    public synchronized String[] dumpStructuralAttributes(String corpus, String attribute, 
           int fromPosition, int toPosition) throws CqiClientException {
	Charset charset;
	try {
	    charset = Charset.forName(corpusCharset(corpus));
	} catch (IOException e) {
	    throw new CqiClientException(SERVER_IO_ERROR, e);
	}
	String attributeName = String.format("%s.%s", corpus, attribute);
	int[] strucs = cpos2Struc(attributeName, fromPosition, toPosition);
	return struc2Str(attributeName, strucs, charset);
    }

    public synchronized String[] dumpStructuralAttributes(String corpus, String attribute, 
	   int fromPosition, int toPosition,  String strCharset) throws CqiClientException {
	Charset charset = Charset.forName(strCharset);
	return dumpStructuralAttributes(corpus, attribute, fromPosition, toPosition, charset);
    }

    public synchronized String[] dumpStructuralAttributes(String corpus, String attribute,
	 int[] cpos, String strCharset) throws CqiClientException {
	Charset charset = Charset.forName(strCharset);
	return dumpStructuralAttributes(corpus, attribute, cpos, charset);
    }

    /**
     * Runs a default CQP query (storing results to result)
     * @param corpus the corpus name
     * @param query the query
     */ 

    public synchronized void query(String corpus, String query) throws CqiClientException {
	cqpQuery(corpus, DEFAULT_SUBCORPUS_NAME, query, DEFAULT_CHARSET);
    }

    public synchronized void query(String corpus, String query, String charset) 
	throws CqiClientException {
	Charset charsetCharset = Charset.forName(charset);
	query(corpus, query, charsetCharset);
    }

    public synchronized void query(String corpus, String query, Charset charset) 
	throws CqiClientException {
	    cqpQuery(corpus, DEFAULT_SUBCORPUS_NAME, query, charset);
    }

    public synchronized void query(String corpus, String subcorpus, String query, Charset charset)
	throws CqiClientException {
	cqpQuery(corpus, subcorpus, query, charset);
    }
    /**
     * drops query, defaulting to default subcorpus name
     *
     * @param corpus corpus name
     * @param subcorpus
     */
    public synchronized void dropQuery(String corpus, String subCorpus) 
    throws CqiClientException {
	dropSubCorpus(String.format("%s:%s", corpus, subCorpus));
    }

    public synchronized void dropQuery(String corpus) throws CqiClientException {
	dropQuery(corpus, DEFAULT_SUBCORPUS_NAME);
    }
    /**
     * returns query size with appropriate defaults
     *
     * @param corpus corpus name
     */
    public int querySize(String corpus, String subcorpus){
	try {
	    return subCorpusSize(String.format("%s:%s", corpus, subcorpus));
	} catch (CqiClientException e) {
	    return -1;
	}
    }

    public int querySize(String corpus) {
	return querySize(corpus, DEFAULT_SUBCORPUS_NAME);
    }

    /**
     * non-buffered dumpSubcorpus. Defaults to default subcorpus name
     *
     * @param corpus corpus name
     * @param fromPosition first index in subcorpus to be dumped
     * @param fromPosition last index in subcorpus to be dumped  
     */
    public synchronized int[][] dumpSubCorpus(String corpus, String subCorpus, int fromPosition, int toPosition) {
	try {
	    String[] subcorpora = listSubcorpora(corpus);
	    if (!Arrays.asList(listSubcorpora(corpus)).contains(subCorpus)) {
		return null;
	    } 	    
	    String subCorpusName = String.format("%s:%s", corpus, subCorpus);
	    int resultSize = subCorpusSize(subCorpusName);
	    toPosition = (toPosition >= resultSize) ? resultSize : toPosition;
	    int index = 0;
	    int dumpSize = toPosition - fromPosition;
	    int[][] result = new int[3][dumpSize];
	    while (fromPosition < toPosition) {
		int bufferEnd = (fromPosition + BUFFER_SIZE > toPosition) ? toPosition - 1 : fromPosition + BUFFER_SIZE - 1;
		int bufferSize = bufferEnd - fromPosition + 1;
		dumpSubCorpusBuffer(subCorpusName, CqiClient.CQI_CONST_FIELD_MATCH, fromPosition, bufferEnd, buffer[0]);
		dumpSubCorpusBuffer(subCorpusName, CqiClient.CQI_CONST_FIELD_MATCHEND, fromPosition, bufferEnd, buffer[1]);
		dumpSubCorpusBuffer(subCorpusName, CqiClient.CQI_CONST_FIELD_TARGET, fromPosition, bufferEnd, buffer[2]);
		for (int i = 0; i < bufferSize; i++) {
		    result[0][index + i] = buffer[0][i];
		    result[1][index + i] = buffer[1][i];
		    result[2][index + i] = buffer[2][i];		
		}
		index += bufferSize;
		fromPosition += bufferSize;
	    }
	    return result;
	} catch (CqiClientException e) {
	    return new int[3][0];		
	}
    }

    public synchronized int[][] dumpSubCorpus(String corpus, int fromPosition, int toPosition) {
	return dumpSubCorpus(corpus, DEFAULT_SUBCORPUS_NAME, fromPosition, toPosition);
    }

    /**
     * Write a string on the socket.
     *
     * @param string the string
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private synchronized void writeString(String string, Charset charset) throws IOException {
        byte[] bytes = string.getBytes(charset);
        this.streamToServer.writeShort(bytes.length);
        this.streamToServer.write(bytes);
    }

    private synchronized void writeString(String string) throws IOException {
        writeString(string, DEFAULT_CHARSET);
    }

    /**
     * Write int array on the socket.
     *
     * @param ints the int array
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private synchronized void writeIntArray(int[] ints) throws IOException {
        int length = ints.length;
        this.streamToServer.writeInt(length);
        for (int i = 0; i < length; i++) {
            this.streamToServer.writeInt(ints[i]);
        }
    }

    /**
     * Write int array on the socket.
     *
     * @param ints the int array
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private synchronized void writeIntArray(int[] ints, int length) throws IOException {
        this.streamToServer.writeInt(length);
        for (int i = 0; i < length; i++) {
            this.streamToServer.writeInt(ints[i]);
        }
    }

    /**
     * Write int array on the socket.
     *
     * @param ints the int array
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private synchronized void writeIntArray(int from, int to) throws IOException {
        this.streamToServer.writeInt(to - from + 1);
        for (int i = from; i <= to; i++) {
            this.streamToServer.writeInt(i);
        }
    }

    /**
     * Read a string from the socket.
     *
     * @return the string
     *
     * @throws CqiClientException Signals that the data read on the socket is
     * unexpected
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private synchronized String readString(Charset charset) throws
            CqiClientException, IOException {
        if (readHeaderFromServer() != CQI_DATA_STRING) {
            throw new CqiClientException(UNEXPECTED_ANSWER);
        }
        short length = this.streamFromServer.readShort();
        byte[] bytes = new byte[length];
        this.streamFromServer.readFully(bytes);
        String res = new String(bytes, charset);
        return res;
    }

    private synchronized String readString() throws
            CqiClientException, IOException {
        return readString(DEFAULT_CHARSET);
    }

    /**
     * Read a boolean from the socket.
     *
     * @return the boolean
     *
     * @throws CqiClientException
     */
    private synchronized boolean readBoolean() throws CqiClientException,
            IOException {
        if (readHeaderFromServer() != CQI_DATA_BOOL) {
            throw new CqiClientException(UNEXPECTED_ANSWER);
        }
        return this.streamFromServer.readByte() == 1;
    }

    /**
     * Read an int from the socket.
     *
     * @return the int
     *
     * @throws CqiClientException Signals that the data read on the socket is
     * unexpected
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private synchronized int readInt() throws CqiClientException, IOException {
        if (readHeaderFromServer() != CQI_DATA_INT) {
            throw new CqiClientException(UNEXPECTED_ANSWER);
        }
        int res = this.streamFromServer.readInt();
        return res;
    }

    /**
     * Read a string array from the socket.
     *
     * @return the string array
     *
     * @throws CqiClientException
     */
    private synchronized String[] readStringArray(Charset charset)
            throws CqiClientException {
        try {
            byte[] header = readHeaderFromServer();
            if (header != CQI_DATA_STRING_LIST) {
                throw new CqiClientException(UNEXPECTED_ANSWER);
            }
            int arrayLength = this.streamFromServer.readInt();
            String[] strings = new String[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                short stringLength = this.streamFromServer.readShort();
                byte[] bytes = new byte[stringLength];
                this.streamFromServer.readFully(bytes);
                strings[i] = new String(bytes, charset);
            }
            return strings;
        } catch (IOException e) {
            throw new CqiClientException("Error reading a string array", e);
        }
    }

    private synchronized int[] readIntArray() throws CqiClientException {
	try {
	    byte[] header = readHeaderFromServer();
	    if (header != CQI_DATA_INT_LIST) {
		throw new CqiClientException(UNEXPECTED_ANSWER);
	    }
	    int arrayLength = this.streamFromServer.readInt();
	    int bsize = arrayLength * 4;
	    int[] output = new int[arrayLength];
	    byte[] abuffer = new byte[bsize];
	    streamFromServer.readFully(abuffer, 0, bsize);
	    for (int i = 0; i + 3 < bsize; i += 4) {
		output[i >> 2] = bytesToInt(abuffer[i], abuffer[i+1], abuffer[i+2], abuffer[i+3]);
	    }
	    return output;
	} catch (IOException e) {
	    throw new CqiClientException("Error reading an int array", e);
	}
    }

    /**
     * Read an int array from the socket.
     *
     * @return the int array
     *
     * @throws CqiClientException
     * @throws IOException
     */
    private synchronized void readIntList(int[] output) throws CqiClientException,
            IOException {
        if ((readHeaderFromServer()) != CQI_DATA_INT_LIST) {
            throw new CqiClientException(UNEXPECTED_ANSWER);
        }
        int arrayLength = this.streamFromServer.readInt();
        if (output.length < arrayLength) {
            throw new CqiClientException(INSUFFICIENT_BUFFER_SIZE);
        }
        int bsize = arrayLength * 4;
        if (byteBuffer.length < bsize) {
            throw new CqiClientException(INSUFFICIENT_BUFFER_SIZE);
        }
        streamFromServer.readFully(byteBuffer, 0, bsize);
        for (int i = 0; i + 3 < bsize; i += 4) {
            output[i >> 2] = bytesToInt(byteBuffer[i], byteBuffer[i + 1], byteBuffer[i + 2], byteBuffer[i + 3]);
        }
    }

    /**
     * Bytes to int.
     *
     * @param a the a
     * @param b the b
     * @param c the c
     * @param d the d
     * @return the int
     */
    private synchronized static int bytesToInt(byte a, byte b, byte c, byte d) {
        return (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
    }

    /**
     * Read the header of the data send by the server.
     *
     * @return the header
     *
     * @throws CqiClientException\
     * @throws IOException
     */
    private synchronized byte[] readHeaderFromServer()
            throws CqiClientException, IOException {
        byte b = this.streamFromServer.readByte();
        switch (b) {
            case 0x00:// cf cqi.h:29
                return CQI_PADDING;
            case 0x01:// cf cqi.h:37
                b = this.streamFromServer.readByte();
                switch (b) {
                    case 0x01:// cf cqi.h:39
                        return CQI_STATUS_OK;
                    case 0x02:// cf cqi.h:40
                        return CQI_STATUS_CONNECT_OK;
                    case 0x03:// cf cqi.h:41
                        return CQI_STATUS_BYE_OK;
                    case 0x04:// cf cqi.h:42
                        return CQI_STATUS_PING_OK;
                }
                break;
            case 0x02:// cf cqi.h:45
                b = this.streamFromServer.readByte();
                switch (b) {
                    case 0x01:// cf cqi.h:39
                        throw new CqiClientException(GENERAL_ERROR);
                    case 0x02:// cf cqi.h:40
                        throw new CqiClientException(CONNECTION_REFUSED_ERROR);
                    case 0x03:// cf cqi.h:41
                        throw new CqiClientException(USER_ABORT_ERROR);
                    case 0x04:// cf cqi.h:42
                        throw new CqiClientException(SYNTAX_ERROR);
                    default:
                        throw new CqiClientException(INTERNAL_CQI_ERROR);
                }
            case 0x03:// cf cqi.h:53
                b = this.streamFromServer.readByte();

                switch (b) {
                    case 0x01:// cf cqi.h:39
                        return CQI_DATA_BYTE;
                    case 0x02:// cf cqi.h:40
                        return CQI_DATA_BOOL;
                    case 0x03:// cf cqi.h:41
                        return CQI_DATA_INT;
                    case 0x04:// cf cqi.h:42
                        return CQI_DATA_STRING;
                    case 0x05:// cf cqi.h:42
                        return CQI_DATA_BYTE_LIST;
                    case 0x06:// cf cqi.h:42
                        return CQI_DATA_BOOL_LIST;
                    case 0x07:// cf cqi.h:42
                        return CQI_DATA_INT_LIST;
                    case 0x08:// cf cqi.h:42
                        return CQI_DATA_STRING_LIST;
                    case 0x09:// cf cqi.h:42
                        return CQI_DATA_INT_INT;
                    case 0x0A:// cf cqi.h:42
                        return CQI_DATA_INT_INT_INT_INT;
                    case 0x0B:// cf cqi.h:42
                        return CQI_DATA_INT_TABLE;
                }
                break;
            case 0x04:// cf cqi.h:67

                b = this.streamFromServer.readByte();
                switch (b) {
                    case 0x01:// cf cqi.h:39
                        throw new CqiClientException(NO_SUCH_ATTRIBUTE_ERROR);
                    case 0x02:// cf cqi.h:40
                        throw new CqiClientException(WRONG_ATTRIBUTE_TYPE_ERROR);
                    case 0x03:// cf cqi.h:41
                        throw new CqiClientException(OUT_OF_RANGE_ERROR);
                    case 0x04:// cf cqi.h:42
                        throw new CqiClientException(REGEX_ERROR + ": " + getLastCqiError());
                    case 0x05:// cf cqi.h:42
                        throw new CqiClientException(CORPUS_ACCESS_ERROR);
                    case 0x06:// cf cqi.h:42
                        throw new CqiClientException(OUT_OF_MEMORY_ERROR);
                    case 0x07:// cf cqi.h:42
                        throw new CqiClientException(INTERNAL_ERROR);
                    default:
                        throw new CqiClientException(INTERNAL_CL_ERROR);
                }
            case 0x05:// cf cqi.h:94

                b = this.streamFromServer.readByte();
                switch (b) {
                    case 0x01:// cf cqi.h:39
                        throw new CqiClientException(GENERAL_CQP_ERROR);
                    case 0x02:// cf cqi.h:40
                        throw new CqiClientException(NO_SUCH_CORPUS_CQP_ERROR);
                    case 0x03:// cf cqi.h:41
                        throw new CqiClientException(INVALID_FIELD_CQP_ERROR);
                    case 0x04:// cf cqi.h:42
                        throw new CqiClientException(OUT_OF_RANGE_CQP_ERROR);
                    case 0x05:// cf cqi.h:44
                        throw new CqiClientException(SYNTAX_CQP_ERROR + ": " + getLastCQPError());
                    default:
                        throw new CqiClientException(INTERNAL_CQP_ERROR);
                }
        }
        return null;
    }

    /**
     * Ask the server to execute a function with a String->String signature.
     *
     * @param string the argument
     * @param function the function to be executed
     * @return the result
     * @throws CqiClientException Signals that the data read on the socket is
     * unexpected
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private synchronized String genericStringToString(String string,
            byte[] function) throws CqiClientException, IOException {
        this.streamToServer.write(function);
        this.writeString(string, DEFAULT_CHARSET);
        return readString(DEFAULT_CHARSET);
    }

    /**
     * Ask the server to execute a function with a String->StringArray
     * signature.
     *
     * @param string the argument
     * @param function the function to be executed
     * @return the result
     * @throws CqiClientException
     */
    private synchronized String[] genericStringToStringArray(String string,
            byte[] function) throws CqiClientException {
        try {
            this.streamToServer.write(function);
            this.writeString(string, DEFAULT_CHARSET);
        } catch (IOException e) {
            throw new CqiClientException(SERVER_IO_ERROR, e);
        }
        return readStringArray(DEFAULT_CHARSET);
    }

    /**
     * Ask the server to execute a function with a String x int[]->String[]
     * signature.
     *
     * @param string the string argument
     * @param ints the int[] argument
     * @param function the function to be executed
     * @return the result
     * @throws CqiClientException
     */
    private synchronized String[] genericStringXIntArraytoStringArray(
            String string, int[] ints, byte[] function, Charset charset) throws
            CqiClientException {
        try {
            this.streamToServer.write(function);
            this.writeString(string, charset);
            this.writeIntArray(ints);
            String[] res = readStringArray(charset);
            return res;
        } catch (IOException e) {
            throw new CqiClientException(SERVER_IO_ERROR, e);
        }
    }

    private synchronized String[] genericStringXIntArraytoStringArray(
            String string, int fromPosition, int toPosition, byte[] function, Charset charset) throws
            CqiClientException {
        try {
            this.streamToServer.write(function);
            this.writeString(string, charset);
            this.writeIntArray(fromPosition, toPosition);
            String[] res = readStringArray(charset);
            return res;
        } catch (IOException e) {
            throw new CqiClientException(SERVER_IO_ERROR, e);
        }

    }

    /**
     * Ask the server to execute a function with a String x int[]->int[]
     * signature.
     *
     * @param string the string argument
     * @param ints the int[] argument
     * @param function the function to be executed
     * @return the result
     * @throws CqiClientException
     */
    private synchronized int[] genericStringXIntArraytoIntArray(String string, 
            int[] ints, byte[] function) throws CqiClientException {
	try {
	    this.streamToServer.write(function);
	    this.writeString(string);
	    this.writeIntArray(ints);
	    int[] output = readIntArray();
	    return output;
	} catch (IOException e) {
	    throw new CqiClientException(SERVER_IO_ERROR, e);
	}
    }    

    private synchronized int[] genericStringXIntArraytoIntArray(String string, 
              int fromPosition, int toPosition, byte[] function) throws CqiClientException {
	try {
	    this.streamToServer.write(function);
	    this.writeString(string);
	    this.writeIntArray(fromPosition, toPosition);
	    int[] output = readIntArray();
	    return output;
	} catch (IOException e) {
	    throw new CqiClientException(SERVER_IO_ERROR, e);
	}
    }   

    private synchronized void genericStringXIntArraytoIntArray(String string,
            int[] ints, byte[] function, int[] output, int size) throws CqiClientException {
        try {
            this.streamToServer.write(function);
            this.writeString(string);
            this.writeIntArray(ints, size);
            readIntList(output);
        } catch (IOException e) {
            throw new CqiClientException(SERVER_IO_ERROR, e);
        }
    }


    /**
     * return the last CQP error.
     *
     * @return the last error
     * @throws CqiClientException Signals that the data read on the socket is
     * unexpected
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws CqiServerError Signals that the cqi server raised an error
     */
    private synchronized String getLastCqiError() throws CqiClientException, IOException {
        this.streamToServer.write(CQI_CTRL_LAST_GENERAL_ERROR);

        try {
            String ret = readString();
            return ret;
        } catch (CqiClientException e) {
            return "getLastCQiError: " + e;
        }
    }

    /**
     * return the last CQP error.
     *
     * @return the last error
     * @throws CqiClientException Signals that the data read on the socket is
     * unexpected
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws CqiServerError Signals that the cqi server raised an error
     */
    private synchronized String getLastCQPError() throws CqiClientException, IOException {

        this.streamToServer.write(CQI_CTRL_LAST_CQP_ERROR);

        try {
            String ret = readString();
            return ret;
        } catch (CqiClientException e) {
            return "getLastCQPError: " + e;
        }
    }

    /**
     * Converts positions to their value given an attribute.
     *
     * @param attribute the attribute
     * @param cpos the cpos
     * @return array of positional attributes
     * @throws CqiClientException
     */
    synchronized String[] cpos2Str(String attribute, int[] cpos, Charset charset) 
	throws CqiClientException {
	return genericStringXIntArraytoStringArray(attribute, cpos, CQI_CL_CPOS2STR, charset);
    }

    synchronized String[] cpos2Str(String attribute, int fromPosition, int toPosition, Charset charset)
	throws CqiClientException {
        return genericStringXIntArraytoStringArray(attribute, fromPosition, toPosition,
                CQI_CL_CPOS2STR, charset);
    }

    /**
     * Computes for each position of an array the Id of the enclosing structural
     * attribute.
     *
     * @param attribute the attribute
     * @param cpos the positions
     * @return the IDs
     * @throws CqiClientException
     */
    synchronized int[] cpos2Struc(String attribute, int[] cpos)
	throws CqiClientException {
        return genericStringXIntArraytoIntArray(attribute, cpos, CQI_CL_CPOS2STRUC);
    }

    synchronized int[] cpos2Struc(String attribute, int fromPosition, int toPosition)
	throws CqiClientException {
        return genericStringXIntArraytoIntArray(attribute, fromPosition, toPosition, CQI_CL_CPOS2STRUC);
    }

    synchronized void cpos2Struc(String attribute, int[] cpos, int[] output, int size)
	throws CqiClientException {
        genericStringXIntArraytoIntArray(attribute, cpos, CQI_CL_CPOS2STRUC, output, size);
    }


    /**
     * Computes for each position of an array the position of the left boundary
     * of the enclosing structural attribute.
     *
     * @param attribute the attribute
     * @param cpos the cpos
     * @return the positions of the left boundaries
     * @throws CqiClientException
     */
    synchronized int[] cpos2LBound(String attribute, int[] cpos)
	throws CqiClientException {
        return genericStringXIntArraytoIntArray(attribute, cpos, CQI_CL_CPOS2LBOUND);
    }

    synchronized int[] cpos2LBound(String attribute, int fromPosition, int toPosition) 
	throws CqiClientException {
	return genericStringXIntArraytoIntArray(attribute, fromPosition, toPosition, CQI_CL_CPOS2LBOUND);
    }

    synchronized void cpos2LBound(String attribute, int[] cpos, int[] output, int size)
	throws CqiClientException {
        genericStringXIntArraytoIntArray(attribute, cpos, CQI_CL_CPOS2LBOUND, output, size);
    }

    /**
     * Computes for each position of an array the position of the right boundary
     * of the enclosing structural attribute.
     *
     * @param attribute the attribute
     * @param cpos the cpos
     * @return the positions of the right boundaries
     * @throws CqiClientException
     */
    synchronized int[] cpos2RBound(String attribute, int[] cpos)
	throws CqiClientException {
        return genericStringXIntArraytoIntArray(attribute, cpos, CQI_CL_CPOS2RBOUND);
    }

    synchronized int[] cpos2RBound(String attribute, int fromPosition, int toPosition) 
	throws CqiClientException {
	return genericStringXIntArraytoIntArray(attribute, fromPosition, toPosition, CQI_CL_CPOS2RBOUND);
    }

    synchronized void cpos2RBound(String attribute, int[] cpos, int[] output, int size)
	throws CqiClientException {
        genericStringXIntArraytoIntArray(attribute, cpos, CQI_CL_CPOS2RBOUND, output, size);
    }
    /**
     * Retrieves annotated string values of structure regions in <strucs>; "" if
     * out of range.
     *
     * @param attribute the attribute
     * @param strucs the strucs
     * @return the string[]
     * @throws CqiClientException
     */
    synchronized String[] struc2Str(String attribute, int[] strucs, Charset charset)
            throws CqiClientException {
        return genericStringXIntArraytoStringArray(attribute, strucs, CQI_CL_STRUC2STR, charset);
    }

    /**
     * Runs a CQP query.
     *
     * @param corpus the corpus
     * @param subcorpus the subcorpus
     * @param query the query
     * @param charset the charset
     * @throws CqiClientException
     */
    private synchronized void cqpQuery(String corpus, String subcorpus,
            String query, Charset charset) throws CqiClientException {
        try {
            this.streamToServer.write(CQI_CQP_QUERY);
            this.writeString(corpus);
            this.writeString(subcorpus);
            this.writeString(query, charset);
            this.readHeaderFromServer();
        } catch (IOException e) {
            throw new CqiClientException(SERVER_IO_ERROR, e);
        }
    }

    /**
     * Gives the size of a subcorpus .
     *
     * @param subcorpus the subcorpus
     *
     * @return the size
     *
     * @throws CqiClientException
     */
    private synchronized int subCorpusSize(String subcorpus) throws
            CqiClientException {
        try {
            this.streamToServer.write(CQI_CQP_SUBCORPUS_SIZE);
            this.writeString(subcorpus);
            return this.readInt();
        } catch (IOException e) {
            throw new CqiClientException(SERVER_IO_ERROR, e);
        }
    }

    /**
     * Dumps the values of <field> for match ranges <first> .. <last> in
     * <subcorpus>. <field> is one of the CQI_CONST_FIELD_* constants.
     *
     * @param subcorpus the subcorpus
     * @param field the field
     * @param first the first
     * @param last the last
     *
     * @return the values
     *
     * @throws CqiClientException
     */
    synchronized void dumpSubCorpusBuffer(String subcorpus, byte field,
            int first, int last, int[] output) throws CqiClientException {
        try {
            this.streamToServer.write(CQI_CQP_DUMP_SUBCORPUS);
            this.writeString(subcorpus);
            this.streamToServer.writeByte(field);
            this.streamToServer.writeInt(first);
            this.streamToServer.writeInt(last);
            this.readIntList(output);
        } catch (IOException e) {
            throw new CqiClientException(SERVER_IO_ERROR, e);
        }
    }

    /**
     * Drops a subcorpus.
     *
     * @param subcorpus the subcorpus
     *
     * @throws CqiClientException
     */
    synchronized void dropSubCorpus(String subcorpus) throws
            CqiClientException {
        try {
            this.streamToServer.write(CQI_CQP_DROP_SUBCORPUS);
            this.writeString(subcorpus);
            this.readHeaderFromServer();
        } catch (IOException e) {
            throw new CqiClientException(SERVER_IO_ERROR, e);
        }
    }
}
