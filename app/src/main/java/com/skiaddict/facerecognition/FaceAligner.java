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

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;


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

    private Activity activity;
    private static PointF projectedLandmark36 = new PointF(0.194157f, 0.16926692f);      // Outer left eye.
    private static PointF projectedLandmark45 = new PointF(0.78885913f, 0.15817115f);    // Outer right eye.
    private static PointF projectedLandmark33 = new PointF(0.49495089f, 0.51444137f);    // Nose

    public FaceAligner (Activity activity) {
        this.activity = activity;
    }

    public void Test (FaceRecognizer faceRecognizer) {

        AssetManager assetManager = activity.getAssets();

        Bitmap bitmap = loadBitmapFromAsset(assetManager, "DSC_3505.jpg");

        // Face landmarks.
        PointF actualLandmark36 = new PointF(2064.0f, 555.0f);
        PointF actualLandmark45 = new PointF(2252.0f, 572.0f);
        PointF actualLandmark33 = new PointF(2163.0f, 671.0f);

        List<Landmark> landmarks = new ArrayList<>();
        landmarks.add(new Landmark(actualLandmark36, Landmark.LEFT_EYE));
        landmarks.add(new Landmark(actualLandmark45, Landmark.RIGHT_EYE));
        landmarks.add(new Landmark(actualLandmark33, Landmark.NOSE_BASE));

        Bitmap aligned = alignFace(bitmap, landmarks);
        String result = faceRecognizer.recognizeFace(aligned);
        Log.i("FaceAlign", result);
    }

    public Bitmap alignFace (Bitmap bitmapSource, List<Landmark> landmarks) {
        // OUTER_EYES_AND_NOSE = [36, 45, 33]
        PointF landmarkLeftEye = null;
        PointF landmarkRightEye = null;
        PointF landmarkNoseBase = null;

        for (Landmark landmarkIx : landmarks) {
            if (Landmark.LEFT_EYE == landmarkIx.getType()) {
                landmarkLeftEye = landmarkIx.getPosition();
            }
            if (Landmark.RIGHT_EYE == landmarkIx.getType()) {
                landmarkRightEye = landmarkIx.getPosition();
            }
            if (Landmark.NOSE_BASE == landmarkIx.getType()) {
                landmarkNoseBase = landmarkIx.getPosition();
            }
        }

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

    private static void SaveImage(Activity activity, Bitmap finalBitmap) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");
        String filename = dateFormat.format(Calendar.getInstance().getTime())+".jpg";
        File file = new File(activity.getExternalFilesDir(null), filename);

        if (file.exists ())
            file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Bitmap loadBitmapFromAsset(AssetManager assetManager, String filePath) {
        Bitmap bitmap = null;
        try {
            InputStream inputStream = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
        }

        return bitmap;
    }
}
