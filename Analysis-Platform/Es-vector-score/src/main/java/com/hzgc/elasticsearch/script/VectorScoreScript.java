/*
Based on: https://discuss.elastic.co/t/vector-scoring/85227/4
and https://github.com/MLnick/elasticsearch-vector-scoring

another slower implementation using strings: https://github.com/ginobefun/elasticsearch-feature-vector-scoring

storing arrays is no luck - lucine index doesn't keep the array members orders
https://www.elastic.co/guide/en/elasticsearch/guide/current/complex-core-fields.html

Delimited Payload Token Filter: https://www.elastic.co/guide/en/elasticsearch/reference/2.4/analysis-delimited-payload-tokenfilter.html


 */

package com.hzgc.elasticsearch.script;

import com.hzgc.elasticsearch.Util;
import com.hzgc.elasticsearch.plugin.VectorScoringPlugin;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.store.ByteArrayDataInput;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.LeafSearchScript;
import org.elasticsearch.script.ScriptException;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Script that scores documents based on cosine similarity embedding vectors.
 */
public final class VectorScoreScript implements LeafSearchScript, ExecutableScript {

    public static final String SCRIPT_NAME = "binary_vector_score";

    private static final int DOUBLE_SIZE = 8;

    // the field containing the vectors to be scored against
    public final String field;

    private int docId;
    private BinaryDocValues binaryEmbeddingReader;

    private final float[] inputVector;
    private final byte[] bitFeatures;
    private final int size;

    private final boolean bit;
    private final AtomicInteger integer = new AtomicInteger();

    private static byte[] hamming = new byte[]{
            0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 1, 2, 2, 3, 2, 3,
            3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4,
            3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 1, 2,
            2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5,
            3, 4, 4, 5, 4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5,
            5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 1, 2, 2, 3,
            2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4,
            4, 5, 4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 2, 3, 3, 4, 3, 4,
            4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6,
            5, 6, 6, 7, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 4, 5,
            5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8
    };

    @Override
    public long runAsLong() {
        return ((Number)this.run()).longValue();
    }
    @Override
    public double runAsDouble() {
        return ((Number)this.run()).doubleValue();
    }
    @Override
    public void setNextVar(String name, Object value) {}
    @Override
    public void setDocument(int docId) {
        this.docId = docId;
    }

    public void setBinaryEmbeddingReader(BinaryDocValues binaryEmbeddingReader) {
        if(binaryEmbeddingReader == null) {
            throw new IllegalStateException("binaryEmbeddingReader can't be null");
        }
        this.binaryEmbeddingReader = binaryEmbeddingReader;
    }


    /**
     * Factory that is registered in
     * {@link VectorScoringPlugin#onModule(org.elasticsearch.script.ScriptModule)}
     * method when the plugin is loaded.
     */
    public static class Factory {

        /**
         * This method is called for every search on every shard.
         * 
         * @param params
         *            list of script parameters passed with the query
         * @return new native script
         */
        public ExecutableScript newScript(@Nullable Map<String, Object> params) throws ScriptException {
            return new VectorScoreScript(params);
        }

        /**
         * Indicates if document scores may be needed by the produced scripts.
         *
         * @return {@code true} if scores are needed.
         */
        public boolean needsScores() {
            return false;
        }

    }

    
    /**
     * Init
     * @param params index that a scored are placed in this parameter. Initialize them here.
     */
    @SuppressWarnings("unchecked")
    public VectorScoreScript(Map<String, Object> params) {
        final Object bitBool = params.get("bit");
        bit = bitBool == null || (boolean) bitBool;

        final Object field = params.get("field");
        if (field == null)
            throw new IllegalArgumentException("binary_vector_score script requires field input");
        this.field = field.toString();

        // get query inputVector - convert to primitive
        final Object vector = params.get("vector");
        if(vector == null){
            throw new IllegalArgumentException("Must have at 'vector' as a parameter");
        }
        if(bit){
            inputVector = null;
            bitFeatures = Util.base64Str2BitFeature((String) vector);
            size = bitFeatures.length;
        }else {
            bitFeatures = null;
            final ArrayList<Float> tmp = (ArrayList<Float>) vector;
            inputVector = new float[tmp.size()];
            for (int i = 0; i < inputVector.length; i++) {
                inputVector[i] = tmp.get(i);
            }
            size = tmp.size();
        }
    }


    /**
     * Called for each document
     * @return cosine similarity of the current document against the input inputVector
     */
    @Override
    public final Object run() {
        final byte[] bytes = binaryEmbeddingReader.get(docId).bytes;
        final ByteArrayDataInput input = new ByteArrayDataInput(bytes);
        input.readVInt(); // returns the number of values which should be 1, MUST appear hear since it affect the next calls
        final int len = input.readVInt(); // returns the number of bytes to read
        if(len != size) {
            System.out.println("Size : " + size);
            System.out.println("Len : " + len);
            return 0.0;
        }
        final int position = input.getPosition();
        if(bit){
            int dist = 0;
            for(int i = 0; i < size ; i ++){
                dist += hamming[~ (bitFeatures[i] ^ bytes[i + position]) & 0xFF];
            }
//            System.out.println(Thread.currentThread().getName() + "-" + docId + "-" + integer.incrementAndGet());
            return dist /(size * 8.0);
        } else {
            final FloatBuffer floatBuffer = ByteBuffer.wrap(bytes, position, len).asFloatBuffer();
            final float[] docVector = new float[size];
            floatBuffer.get(docVector);
            double score = 0;
            for(int i = 0; i < size; i++) {
                score += docVector[i] * inputVector[i];
            }
            double actualValue = new BigDecimal((0.5 + (score / 2)) * 100).//余弦相似度表示为cosineSIM=0.5cosθ+0.5
                    setScale(2, BigDecimal.ROUND_HALF_UP).//ROUND_HALF_UP=4 保留两位小数四舍五入
                    doubleValue();
            if (actualValue >= 100) {
                return 100;
            }
            return score;
        }
    }

}