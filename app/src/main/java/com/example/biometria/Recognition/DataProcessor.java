package com.example.biometria.Recognition;

import java.io.*;
import java.util.*;

public class DataProcessor {

    private static int SILENCE_LEVEL = 25;
    private static float MEAN_L = -1.1550478456581255f;
    private static float MEAN_R = -0.8646158076607898f;
    private static float STD_L = 1196.3138965717174f;
    private static float STD_R = 441.9923867554516f;

    public Map<Integer, float[]> processData(byte[] array) {
        try {

            //TODO: zastąpić nagranym plikiem

            int fileLen = array.length;
            int channelLen = fileLen / 2;
            int fileInfoLen = 21;

            float[] floatArr = new float[fileLen - fileInfoLen];

            for (int i = 0; i <array.length ; i++)
            {
                if(i > fileInfoLen && i < channelLen) {
                    floatArr[i-21] = ((float) ((array[i * 2] & 0xff) | (array[i * 2 + 1] << 8)));
                }
            }

            Map<String, List<Float>> map = splitChannels(floatArr);
            List<Float> L = map.get("L");
            List<Float> R = map.get("R");

            int startInd;
            if(getFirstNonSilentSampleIndex(parseFloatArray(L)) < getFirstNonSilentSampleIndex(parseFloatArray(R))) {
                startInd = getFirstNonSilentSampleIndex(parseFloatArray(L));
            } else {
                startInd = getFirstNonSilentSampleIndex(parseFloatArray(R));
            }

            Map<Integer, float[]> splittedData = new HashMap<>();

            L = L.subList(startInd, L.size());
            R = R.subList(startInd, R.size());

            L = standardizeSamples(L, "L");
            R = standardizeSamples(R, "R");

            int c = 0;
            for(int i = 0; i < L.size() / 2 - (L.size() % 200); i = i + 100) {
                float[] data = new float[400];
                c = c + 1;
                for(int j = i; j < i + 200; j++) {
                    data[j - i] = L.get(j);
                    data[j + 200 - i] = R.get(j);
                }

                splittedData.put(i / 100, data);
            }

            return splittedData;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<Float> standardizeSamples(List<Float> samples, String channel) {
        List<Float> list = new ArrayList<>();
        for (Float sample : samples) {
            Float standardized = (sample - (channel.equals("L") ? MEAN_L : -MEAN_R)) / (channel.equals("L") ? STD_L : STD_R);
            list.add(standardized);
        }
        return list;
    }

    private float[] parseFloatArray(List<Float> list) {
        float[] parsed = new float[list.size()];
        for(int i = 0; i < list.size(); i++) {
            parsed[i] = list.get(i);
        }

        return parsed;
    }

    private Map<String, List<Float>> splitChannels(float[] floatArr) {
        Map<String, List<Float>> map = new HashMap<>();
        List<Float> L = new ArrayList<>();
        List<Float> R = new ArrayList<>();
        for(int i = 0; i < floatArr.length; i++) {
            if(i % 2 == 0) {
                L.add(floatArr[i]);
            } else {
                R.add(floatArr[i]);
            }
        }

        map.put("L", L);
        map.put("R", R);
        return map;
    }


    private int getFirstNonSilentSampleIndex(float[] floatArr) {
        int endindex = 20;
        for(int startindex = 0; startindex < floatArr.length; startindex += 10) {
            endindex = endindex + 10;
            float sumOfSquares = 0;

            for (int i = startindex; i < endindex; i += 2) {
                if(i < floatArr.length)
                    sumOfSquares = sumOfSquares + floatArr[i] * floatArr[i];
            }

            int numberOfSamples = endindex - startindex + 1;
            float rms = (float) Math.sqrt(sumOfSquares / (numberOfSamples));

            if(rms > SILENCE_LEVEL) {
                return startindex;
            }
        }

        return -1;
    }
}
