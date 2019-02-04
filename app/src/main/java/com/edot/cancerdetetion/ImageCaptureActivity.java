package com.edot.cancerdetetion;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;

public class ImageCaptureActivity extends AppCompatActivity {

    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_capture);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (camera == null)
        {
            camera = Camera.open();
        }
        if(camera == null)
        {
            Log.d("Leaf","Camera can\'t be locked");
            return;
        }
        camera.lock();

        FrameLayout frameLayout = findViewById(R.id.layout_frame);
        frameLayout.addView(new CameraPreview(this,camera));

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (camera != null) {
            camera.stopPreview();
        }
        FrameLayout frameLayout = findViewById(R.id.layout_frame);
        frameLayout.removeAllViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.release();
        }
        camera = null;
    }
}
