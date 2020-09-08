package com.example.biometria.Recognition;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class Prediction {

    private static final String ASSETS_PATH = "file:///android_asset/";
    private static final String MODEL_NAME = "model.tflite";

    private Interpreter model;

    public Prediction() {

    }

    public void fetchInterpreter(Context context) throws IOException {
        model = new Interpreter(loadModel(context.getAssets(), (ASSETS_PATH + MODEL_NAME).split(ASSETS_PATH, -1)[1]));
    }

    public int predict(Map<Integer, float[]> samples) {
        Map<Integer, Integer> scores = new HashMap<>();
        for(int i = 0; i < 20; i++) {
            scores.put(i, 0);
        }

        float[][] outputScores = new float[1][20];
        for (float[] input : samples.values()) {
            model.run(input, outputScores);

            float max = outputScores[0][0];
            float index = 0;
            for(int i = 0; i < 20; i++) {
                if(max < outputScores[0][i]) {
                    max = outputScores[0][i];
                    index = i;
                }
            }
            Log.d("jooo", String.valueOf(index));
            scores.put((int) index, scores.get((int)index) + 1);
        }

        int bestResult = 0;
        int bestResultIndex = 0;
        for (Map.Entry<Integer, Integer> entry : scores.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();

            if (value > bestResult) {
                bestResult = value;
                bestResultIndex = key;
            }
        }

        return bestResultIndex;
    }

    private MappedByteBuffer loadModel(AssetManager assets, String modelFilename) throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}
