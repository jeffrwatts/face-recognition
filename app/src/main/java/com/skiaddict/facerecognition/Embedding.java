package com.skiaddict.facerecognition;

/**
 * Created by jewatts on 12/29/17.
 */

public class Embedding {
    public static int EMBEDDING_LENGTH = 128;

    public float[] values;

    public Embedding(float[] values) {
        this.values = values.clone();
    }

    public double computeDistance (Embedding embedding) {
        float distance = 0;

        for (int ix = 0; ix < EMBEDDING_LENGTH; ix++) {
            distance += Math.pow(this.values[ix] - embedding.values[ix], 2);
        }
        return Math.sqrt(distance);
    }
}
