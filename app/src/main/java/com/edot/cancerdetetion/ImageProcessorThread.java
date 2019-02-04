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

            handler.sendEmptyMessage(2);
    }

}
