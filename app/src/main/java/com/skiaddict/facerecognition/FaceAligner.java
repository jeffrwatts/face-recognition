package com.skiaddict.facerecognition;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by jewatts on 12/30/17.
 */

public class FaceAligner {
    private static PointF projectedLandmark36 = new PointF(0.194157f, 0.16926692f);      // Outer left eye.
    private static PointF projectedLandmark45 = new PointF(0.78885913f, 0.15817115f);    // Outer right eye.
    private static PointF projectedLandmark33 = new PointF(0.49495089f, 0.51444137f);    // Nose

    public Bitmap alignFace (Bitmap bitmapSource, List<Point> landmarks) {
        if (landmarks.size() < 68) {
            return null;
        }

        // OUTER_EYES_AND_NOSE = [36, 45, 33]
        Point landmarkLeftEye = landmarks.get(36);
        Point landmarkRightEye = landmarks.get(45);
        Point landmarkNoseBase = landmarks.get(33);

        if ((null == landmarkLeftEye) || (null == landmarkRightEye) || (null == landmarkNoseBase)) {
            return null;
        }

        float[] src = new float[6];
        src[0] = landmarkLeftEye.x;
        src[1] = landmarkLeftEye.y;
        src[2] = landmarkRightEye.x;
        src[3] = landmarkRightEye.y;
        src[4] = landmarkNoseBase.x;
        src[5] = landmarkNoseBase.y;

        float[] dst = new float[6];
        dst[0] = projectedLandmark36.x * 96;
        dst[1] = projectedLandmark36.y * 96;
        dst[2] = projectedLandmark45.x * 96;
        dst[3] = projectedLandmark45.y * 96;
        dst[4] = projectedLandmark33.x * 96;
        dst[5] = projectedLandmark33.y * 96;

        Matrix matrix = new Matrix();
        matrix.setPolyToPoly(src, 0, dst, 0, 3);

        Bitmap aligned = Bitmap.createBitmap( 96, 96, Bitmap.Config.RGB_565 );

        Canvas canvas = new Canvas(aligned);
        canvas.drawBitmap(bitmapSource, matrix, new Paint());

        return aligned;
    }
}
