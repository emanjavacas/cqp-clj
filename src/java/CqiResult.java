/**
 * 
 * This software is copyright (c) 2015 by Enrique Manjavacas
 * This software is copyright (c) 2012 by - Seminar fuer Sprachwissenschaft
 * (http://www.sfs.uni-tuebingen.de/) This is free software. You can
 * redistribute it and/or modify it under the terms described in the GNU General
 * Public License v3 of which you should have received a copy. Otherwise you can
 * download it from
 *
 * http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Enrique Manjavacas
 * @copyright Seminar fuer Sprachwissenschaft (http://www.sfs.uni-tuebingen.de/)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt GNU General Public License
 * v3
 */

import java.nio.charset.Charset;
import java.lang.Iterable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Arrays;

public class CqiResult {

    private static final int BUFFER_SIZE = 10;
    private static final int MATCH_START = 0;
    private static final int MATCH_END = 1;
    private static final int TARGET = 2;
    private static final int CONTEXT_START = 3;
    private static final int CONTEXT_END = 4;
    private final int[] buffer = new int[BUFFER_SIZE];
    private final int[][] results = new int[5][BUFFER_SIZE];
    private final CqiClient client;
    private final String corpusName;
    private final String subCorpusName;
    private final Charset charset;
    private final int resultSize;
    private final String contextStructuralAttributeName;
    private final String[] metadataStructuralAttributeNames;
    private final String[][] metadataStructuralAttributeValues;
    private int bufferedSize;
    private int bufferedStart;
    private int bufferIndex = -1;

    CqiResult(CqiClient client, String corpusName, String subCorpusName, Charset charset, int resultSize, String contextStructuralAttribute, String[] metadataStructuralAttributes) {
        this.client = client;
        this.corpusName = corpusName;
        this.subCorpusName = subCorpusName;
        this.resultSize = resultSize;
        this.charset = charset;
        this.contextStructuralAttributeName = String.format("%s.%s", corpusName, contextStructuralAttribute);
        if (metadataStructuralAttributes != null) {
            this.metadataStructuralAttributeNames = new String[metadataStructuralAttributes.length];
            for (int i = 0; i < metadataStructuralAttributes.length; i++) {
                this.metadataStructuralAttributeNames[i] = String.format("%s.%s", corpusName, metadataStructuralAttributes[i]);
            }
            this.metadataStructuralAttributeValues = new String[metadataStructuralAttributes.length][BUFFER_SIZE];
        } else {
            this.metadataStructuralAttributeNames = null;
            this.metadataStructuralAttributeValues = null;
        }
    }


    /**
     * Extracts positional attributes for a given range in the total match.
     *
     * @return array of arrays with the values corresponding to the range.
     */
    public int[][] cposRange(int start, int end) throws IllegalArgumentException, CqiClientException {
	if (start < 0 || end >= resultSize || end < start) {
	    throw new IllegalArgumentException("Range out of bounds");
	}
	int range = end - start + 1;
	int[][] cposRangeArray = new int[5][range];
	client.dumpSubCorpus(subCorpusName, CqiClient.CQI_CONST_FIELD_MATCH, start, end, cposRangeArray[MATCH_START]);
	client.dumpSubCorpus(subCorpusName, CqiClient.CQI_CONST_FIELD_MATCHEND, start, end, cposRangeArray[MATCH_END]);
	client.dumpSubCorpus(subCorpusName, CqiClient.CQI_CONST_FIELD_TARGET, start, end, cposRangeArray[TARGET]);
	client.cpos2LBound(contextStructuralAttributeName, cposRangeArray[MATCH_START], cposRangeArray[CONTEXT_START], range);
	client.cpos2RBound(contextStructuralAttributeName, cposRangeArray[MATCH_END], cposRangeArray[CONTEXT_END], range);
	return cposRangeArray;
    }

    /**
     * Retrieves the current match index.
     *
     * @return the current match number; {@code -1} if there is no current match
     */
    public int getIndex() {
        return bufferedStart + bufferIndex;
    }

    /**
     * Moves the cursor to the given match number in this {@code MatchSet}
     * object.
     *
     * @return {@code true} if the cursor is moved to a position in this
     * {@code MatchSet} object; {@code false} if the cursor is before the first
     * match or after the last match
     * @exception CqiClientException
     */
    public boolean absolute(int match) throws CqiClientException {
        if (match < 0 || match >= resultSize) {
            return false;
        }
        bufferedStart = match;
        return true;
    }

    /**
     * Moves the cursor froward one row from its current position. A
     * {@code CqiResult} cursor is initially positioned before the first match;
     * the first call to the method {@code next} makes the first match the
     * current match.
     *
     * @return {@code true} if the new current row is valid; {@code false} if
     * there are no more matches
     * @exception CqiClientException
     */
    public boolean next() throws CqiClientException {
        if (++bufferIndex >= bufferedSize) {
            return bufferResults();
        } else {
            return true;
        }
    }

    private boolean bufferResults() throws CqiClientException {
        bufferedStart += bufferedSize;
        if (bufferedStart < resultSize) {
            final int bufferedEnd = (bufferedStart + BUFFER_SIZE > resultSize) ? resultSize - 1 : bufferedStart + BUFFER_SIZE - 1;
            bufferedSize = bufferedEnd - bufferedStart + 1;
            bufferIndex = 0;
            client.dumpSubCorpus(subCorpusName, CqiClient.CQI_CONST_FIELD_MATCH, bufferedStart, bufferedEnd, results[MATCH_START]);
            client.dumpSubCorpus(subCorpusName, CqiClient.CQI_CONST_FIELD_MATCHEND, bufferedStart, bufferedEnd, results[MATCH_END]);
            client.dumpSubCorpus(subCorpusName, CqiClient.CQI_CONST_FIELD_TARGET, bufferedStart, bufferedEnd, results[TARGET]);
            client.cpos2LBound(contextStructuralAttributeName, results[MATCH_START], results[CONTEXT_START], bufferedSize);
            client.cpos2RBound(contextStructuralAttributeName, results[MATCH_END], results[CONTEXT_END], bufferedSize);
            if (metadataStructuralAttributeNames != null) {
                for (int i = 0; i < metadataStructuralAttributeNames.length; i++) {
                    String attrName = metadataStructuralAttributeNames[i];
                    client.cpos2Struc(attrName, results[MATCH_START], buffer, bufferedSize);
                    metadataStructuralAttributeValues[i] = client.struc2Str(attrName, buffer, charset);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Retrieves the starting position of the current match
     *
     * @return the starting position
     */
    public int getMatchStart() {
        return results[MATCH_START][bufferIndex];
    }

    /**
     * Retrieves the end position of the current match
     *
     * @return the end position
     */
    public int getMatchEnd() {
        return results[MATCH_END][bufferIndex];
    }

    /**
     * Retrieves the target position for the current match
     *
     * @return the target positions
     */
    public int getTarget() {
        return results[TARGET][bufferIndex];
    }

    /**
     * Retrieves the starting position of the context structural attribute
     * surrounding the current match
     *
     * @return the starting positions
     */
    public int getContextStart() {
        return results[CONTEXT_START][bufferIndex];
    }

    /**
     * Retrieves the end position of the context structural attribute
     * surrounding the current match
     *
     * @return the end positions
     */
    public int getContextEnd() {
        return results[CONTEXT_END][bufferIndex];
    }

    /**
     * Retrieves values of a given positional attribute between given positions
     *
     * @param positionalAttribute
     * @param fromPosition
     * @param toPosition
     * @return positional attribute values
     */
    public String[] getValues(String positionalAttribute, int fromPosition, int toPosition) throws CqiClientException {
        return client.cpos2Str(String.format("%s.%s", corpusName, positionalAttribute), fromPosition, toPosition, charset);
    }

    /**
     * Retrieves a value of a structural attribute with an index given
     * surrounding the current match
     *
     * @return the value of a structural attribute
     */
    public String getStructuralAttributeValue(int attributeIndex) {
        if (metadataStructuralAttributeValues != null) {
            return metadataStructuralAttributeValues[attributeIndex][bufferIndex];
        } else {
            throw new UnsupportedOperationException("no structural attributes were provided");
        }
    }

    /**
     * Clears this result from the memory
     *
     * @throws CqiClientException
     */
    public void clear() throws CqiClientException {
        client.dropSubCorpus(subCorpusName);
    }

    public int size() {
        return resultSize;
    }
   
}
