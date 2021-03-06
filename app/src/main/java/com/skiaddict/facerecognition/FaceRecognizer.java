package com.skiaddict.facerecognition;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Trace;
import android.support.annotation.NonNull;
import android.util.ArrayMap;
import android.util.Log;


import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;

/**
 * Created by jewatts on 12/9/17.
 */

class FaceRecognizer {
    public static final int IMAGE_WIDTH = 96;
    public static final int IMAGE_HEIGHT = 96;
    public static final int IMAGE_DEPTH = 3;

    private static final int IMAGE_MEAN = 0;
    private static final float IMAGE_STD = 255.0f;

    private static final String TAG = "FaceRecognizer";

    private static final String INPUT_NAME = "input_1";
    private static final String INPUT_NAME_TRAINING = "bn1/keras_learning_phase";
    private static final String OUTPUT_NAME = "lambda_1/l2_normalize";

    private static final String MODEL_FILE = "fr_graph.pb";

    private String[] outputNames;
    private int[] intValues;
    private float[] embedding;
    private float[] floatValues;

    private int embeddingLength;
    private TensorFlowInferenceInterface inferenceInterface;

    public FaceRecognizer(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();

        // Load model
        inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
        Operation operation = inferenceInterface.graphOperation(OUTPUT_NAME);

        embeddingLength = (int) operation.output(0).shape().size(1);

        // Pre-allocate buffers.
        outputNames = new String[] {OUTPUT_NAME};
        intValues = new int[IMAGE_HEIGHT * IMAGE_WIDTH];
        floatValues = new float[IMAGE_HEIGHT * IMAGE_WIDTH * IMAGE_DEPTH];
        embedding = new float[embeddingLength];
    }

    public Embedding generateEmbedding(Bitmap bitmap) {
        long startTime = System.currentTimeMillis();
        Trace.beginSection("generateEmbedding");

        // Process the input bitmap.  Model expects shape of IMAGE_DEPTH x IMAGE_WIDTH x IMAGE_HEIGHT.
        // Data is also organized by BGR (instead of RGB).
        Trace.beginSection("preprocessBitmap");
        bitmap.getPixels(intValues, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i+IMAGE_HEIGHT*IMAGE_WIDTH*2] = (((val) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;                                // "B" channel
            floatValues[i+IMAGE_HEIGHT*IMAGE_WIDTH] = (((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;  // "G" channel
            floatValues[i] = (((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD; // "R" channel
            //floatValues[i] = (((val) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;                                // "B" channel
            //floatValues[i+IMAGE_HEIGHT*IMAGE_WIDTH] = (((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;  // "G" channel
            //floatValues[i+IMAGE_HEIGHT*IMAGE_WIDTH*2] = (((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD; // "R" channel
        }
        Trace.endSection();

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed");
        inferenceInterface.feed(INPUT_NAME, floatValues, 1, IMAGE_DEPTH, IMAGE_WIDTH, IMAGE_HEIGHT);
        inferenceInterface.feed(INPUT_NAME_TRAINING, new boolean[]{false});
        Trace.endSection();

        // Run the inference call.
        Trace.beginSection("run");
        inferenceInterface.run(outputNames, false);
        Trace.endSection();

        // Copy the output Tensor back into the output array.
        Trace.beginSection("fetch");
        inferenceInterface.fetch(OUTPUT_NAME, embedding);
        Trace.endSection();

        return new Embedding(embedding);
    }

    public void close () {
        inferenceInterface.close();
    }
}
