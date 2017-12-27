package com.skiaddict.facerecognition;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Trace;
import android.util.Log;

import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Vector;

/**
 * Created by jewatts on 12/9/17.
 */

class ImageClassifier {
    public static final int IMAGE_WIDTH = 224;
    public static final int IMAGE_HEIGHT = 224;
    private static final int IMAGE_DEPTH = 3;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;

    private static final String TAG = "ImageClassifier";

    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "final_result";

    private static final int MAX_RESULTS = 3;
    private static final float RESULT_THRESHOLD = 0.1f; // Keep threshold low for now.  Want to see how close misclassifications are.
    private static final String MODEL_FILE = "graph.pb";
    private static final String LABEL_FILE = "labels.txt";

    private String[] outputNames;
    private int[] intValues;
    private float[] outputs;
    private float[] floatValues;

    private int classes;
    private Vector<String> labels = new Vector<String>();
    private TensorFlowInferenceInterface inferenceInterface;

    public ImageClassifier(Activity activity) throws IOException {
        AssetManager assetManager = activity.getAssets();

        // Load labels.
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(LABEL_FILE)));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        reader.close();

        // Load model
        inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
        Operation operation = inferenceInterface.graphOperation(OUTPUT_NAME);

        classes = (int) operation.output(0).shape().size(1);
        Log.i(TAG, "Read " + labels.size() + " labels, output layer size is " + classes);

        // Pre-allocate buffers.
        outputNames = new String[] {OUTPUT_NAME};
        intValues = new int[IMAGE_HEIGHT * IMAGE_WIDTH];
        floatValues = new float[IMAGE_HEIGHT * IMAGE_WIDTH * IMAGE_DEPTH];
        outputs = new float[classes];
    }

    private static int callCount = 0;

    public String classifyFrame(Bitmap bitmap) {
        Trace.beginSection("classifyFrame");

        Trace.beginSection("preprocessBitmap");
        bitmap.getPixels(intValues, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3 + 0] = (((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
            floatValues[i * 3 + 1] = (((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
            floatValues[i * 3 + 2] = ((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
        }
        Trace.endSection();

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed");
        inferenceInterface.feed(INPUT_NAME, floatValues, 1, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_DEPTH);
        Trace.endSection();

        // Run the inference call.
        Trace.beginSection("run");
        inferenceInterface.run(outputNames, false);
        Trace.endSection();

        // Copy the output Tensor back into the output array.
        Trace.beginSection("fetch");
        inferenceInterface.fetch(OUTPUT_NAME, outputs);
        Trace.endSection();

        // Sort results
        PriorityQueue<Map.Entry<String, Float>> sortedResults = new PriorityQueue<>(MAX_RESULTS, new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        for (int outputIx = 0; outputIx < outputs.length; outputIx++) {
            if (outputs[outputIx] > RESULT_THRESHOLD) {
                sortedResults.add(new AbstractMap.SimpleEntry<String, Float>(labels.get(outputIx), outputs[outputIx]));
                if (sortedResults.size() > MAX_RESULTS) {
                    sortedResults.poll();
                }
            }
        }

        String textToShow = "";
        int sortedResultsSize = sortedResults.size();
        for (int resultIx = 0; resultIx < sortedResultsSize; resultIx++) {
            Map.Entry<String, Float> label = sortedResults.poll();
            textToShow = String.format("\n%s: %4.2f",label.getKey(),label.getValue()) + textToShow;
        }

        return textToShow;
    }

    public void close () {

    }
}
