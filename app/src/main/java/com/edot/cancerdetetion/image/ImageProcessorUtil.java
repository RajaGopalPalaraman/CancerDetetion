package com.edot.cancerdetetion.image;

import android.graphics.Bitmap;
import android.util.Log;

public class ImageProcessorUtil {

    private static final String LOG_TAG = "FeatureExtractLogTag";

    private static final int[][] filter;
    private static final int sum;

    static {
        filter = new int[][]{
                {1, 2, 1},
                {2, 4, 2},
                {1, 2, 1}
        };
        sum = sum();
    }

    public static void preProcess(Bitmap filtered)
    {
        for (int y = 1; y + 1 < filtered.getHeight(); y++) {
            for (int x = 1; x + 1 < filtered.getWidth(); x++) {
                int tempColor = getFilteredValue(filtered, y, x);
                filtered.setPixel(x, y, tempColor);
            }
        }
    }

    private static int getFilteredValue(Bitmap givenImage, int y, int x) {
        int r = 0, g = 0, b = 0;
        for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {

                r += (filter[1 + j][1 + k] * (getRed(givenImage.getPixel(x + k, y + j))));
                g += (filter[1 + j][1 + k] * (getGreen(givenImage.getPixel(x + k, y + j))));
                b += (filter[1 + j][1 + k] * (getBlue(givenImage.getPixel(x + k, y + j))));
            }

        }
        r = r / sum;
        g = g / sum;
        b = b / sum;
        return groupToPixel(255,r,g,b);
    }

    private static int[] imageHistogram(Bitmap input) {

        int[] histogram = new int[256];

        for (int i = 0; i < input.getWidth(); i++) {
            for (int j = 0; j < input.getHeight(); j++) {
                int red = getRed(input.getPixel(i, j));
                histogram[red]++;
            }
        }
        return histogram;
    }

    private static int otsuTreshold(Bitmap original) {

        int[] histogram = imageHistogram(original);
        int total = original.getHeight() * original.getWidth();

        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) {
                continue;
            }
            wF = total - wB;

            if (wF == 0) {
                break;
            }

            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }
        Log.d(LOG_TAG,"Otsu Threshold value is " + threshold);
        return threshold;

    }

    public static void segment(Bitmap original) {
        int red;
        int newPixel;

        int threshold = otsuTreshold(original);

        for (int i = 0; i < original.getWidth(); i++) {
            for (int j = 0; j < original.getHeight(); j++) {

                // Get pixels
                red = getRed(original.getPixel(i, j));
                int alpha = getAlpha(original.getPixel(i, j));
                if (red > threshold) {
                    newPixel = 0;
                } else {
                    newPixel = 255;
                }
                newPixel = groupToPixel(alpha, newPixel, newPixel, newPixel);
                original.setPixel(i, j, newPixel);
            }
        }
    }

    private static int sum() {
        int sum = 0;
        for (int[] aFilter : filter) {
            for (int anAFilter : aFilter) {
                sum += anAFilter;
            }
        }
        return sum;
    }

    private static int getRed(int rgb)
    {
        return (rgb >> 16) & 0xff;
    }

    private static int getGreen(int rgb)
    {
        return (rgb >> 8) & 0xff;
    }

    private static int getBlue(int rgb)
    {
        return (rgb) & 0xff;
    }

    private static int getAlpha(int rgb) {
        return (rgb >> 24) & 0xff;
    }

    private static int groupToPixel(int a, int r, int g, int b)
    {
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                ((b & 0xFF));
    }

}
