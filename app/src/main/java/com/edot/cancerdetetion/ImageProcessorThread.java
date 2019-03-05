package com.edot.cancerdetetion;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.edot.cancerdetetion.image.ImageProcessorUtil;

import java.util.HashMap;

public class ImageProcessorThread extends Thread {

    private final Handler handler;
    private final Bitmap bitmap;

    public ImageProcessorThread(Handler handler, Bitmap bitmap)
    {
        this.handler = handler;
        this.bitmap = bitmap.copy(bitmap.getConfig(),true);
    }

    @Override
    public void run() {
            super.run();
            Bitmap bitmapCopy = bitmap.copy(bitmap.getConfig(), true);
            Bitmap copyToUi = null;
            for (int i=1;i<=25;i++)
            {
                handler.obtainMessage(1, "Applying Filter Stage "+i).sendToTarget();
                ImageProcessorUtil.preProcess(bitmapCopy);
                copyToUi = bitmapCopy.copy(bitmapCopy.getConfig(), false);
                handler.obtainMessage(0, copyToUi).sendToTarget();
            }

            handler.obtainMessage(1, "Segmenting...").sendToTarget();
            ImageProcessorUtil.segment(bitmapCopy);
            copyToUi = bitmapCopy.copy(bitmapCopy.getConfig(),false);
            handler.obtainMessage(0, copyToUi).sendToTarget();
            handler.obtainMessage(1, "Segmentation Completed").sendToTarget();

            final float perimeter = ImageProcessorUtil.getBinaryImagePerimeter(bitmapCopy);
            if (perimeter != 0) {
                float area = ImageProcessorUtil.calculteArea(bitmapCopy);
                float asym = ImageProcessorUtil.asym(bitmap);
                float irreg = asym + perimeter * 0.5f;
                float ed = 3.142f * 9.41f + perimeter * 0.75f;
                float rod = 1 / perimeter;
                float compactness = (4 * 3.142f * perimeter * perimeter) /
                        (asym * (bitmap.getWidth() * bitmap.getHeight()));
                float sol = compactness * 4 * 3.142f * perimeter;
                float extd = asym * 2.1f + perimeter * 1.14f;
                handler.obtainMessage(1,"Shape Features Area:"+area+
                        "; Perimeter:"+perimeter+"; Irregularity:"+irreg+
                        "; Compactness:"+compactness+"; Solidity:"+sol+
                        "; Extend:"+extd+"; Roundness:"+rod).sendToTarget();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
                handler.obtainMessage(1,"Analyzing...").sendToTarget();
                if (ImageProcessorUtil.classify(sol,extd))
                {
                    handler.obtainMessage(1,"Type: Malignant").sendToTarget();
                }
                else
                {
                    handler.obtainMessage(1,"Type: Benign").sendToTarget();
                }
            }
            else
            {
                handler.obtainMessage(1,"No Defected Regions found").sendToTarget();
            }

            handler.sendEmptyMessage(2);
    }

}
