/**
 * 
 * This software is copyright (c) 2015 by Enrique Manjavacas
 * This is free software. You can redistribute it and/or modify it under the
 * terms described in the GNU General Public License v3 of which you should
 * have received a copy. Otherwise you can download it from
 *
 * http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Enrique Manjavacas
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt GNU General Public License
 * v3
 */

import java.nio.charset.Charset;

public class CqiDump {
    private static final int BUFFER_SIZE = 10;
    private static final int MATCH_START = 0;
    private static final int MATCH_END = 1;
    private static final int TARGET = 2;
    private static final int CONTEXT_START = 3;
    private static final int CONTEXT_END = 4;
    private final int[] metadataBuffer = new int[BUFFER_SIZE];
    private final int[][] resultsBuffer = new int[5][BUFFER_SIZE];
    private final CqiClient client;
    private final String corpusName;
    private final String subCorpusName;
    private final Charset charset;
    private final int resultSize;
    private int bufferStart;    

    CqiDump(CqiClient client, String corpusName, String subCorpusName, Charset charset, int resultSize) {
	this.client = client;
	this.corpusName = corpusName;
        this.subCorpusName = subCorpusName;
        this.resultSize = resultSize;
        this.charset = charset;
    }

    public int[][] dumpRange(int start, int end) throws CqiClientException {
	end = (end >= resultSize) ? resultSize : end;
	int resultIndex = 0;
	int dumpSize = end - start;
	int[][] result = new int[3][dumpSize];
	while (start < end) {
	    // read to buffer
	    int bufferEnd = (start + BUFFER_SIZE > end) ? end - 1 : start + BUFFER_SIZE - 1;
	    int bufferSize = bufferEnd - start + 1;
	    client.dumpSubCorpus(subCorpusName, CqiClient.CQI_CONST_FIELD_MATCH, start, bufferEnd, resultsBuffer[MATCH_START]);
            client.dumpSubCorpus(subCorpusName, CqiClient.CQI_CONST_FIELD_MATCHEND, start, bufferEnd, resultsBuffer[MATCH_END]);
            client.dumpSubCorpus(subCorpusName, CqiClient.CQI_CONST_FIELD_TARGET, start, bufferEnd, resultsBuffer[TARGET]);
	    // assign to result
	    for (int i = 0; i < bufferSize; i++) {
		result[MATCH_START][resultIndex + i] = resultsBuffer[MATCH_START][i];	
		result[MATCH_END][resultIndex + i] = resultsBuffer[MATCH_END][i];
		result[TARGET][resultIndex + i] = resultsBuffer[TARGET][i];
	    }
	    resultIndex += bufferSize;
	    start += bufferSize;
	}
	return result;
    }
    public void clear() throws CqiClientException {
	client.dropSubCorpus(subCorpusName);
    }

    public int size() {
	return resultSize;
    }
}
