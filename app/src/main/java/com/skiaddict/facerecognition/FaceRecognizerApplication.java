package com.skiaddict.facerecognition;

import android.app.Application;
import android.content.Context;

import com.tzutalin.dlib.FaceDet;

import java.io.File;
import java.io.IOException;

/**
 * Created by jewatts on 1/6/18.
 */

public class FaceRecognizerApplication extends Application {

    private FaceDet faceDetector = null;
    private FaceAligner faceAligner = null;
    private FaceRecognizer faceRecognizer = null;
    private UserDb userDb = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public FaceDet getFaceDetector () {
        if (null == faceDetector) {
            faceDetector = new FaceDet(getFaceShapeModelPath());
        }
        return  faceDetector;
    }

    public FaceAligner getFaceAligner () {
        if (null == faceAligner) {
            faceAligner = new FaceAligner();
        }
        return faceAligner;
    }

    public FaceRecognizer getFaceRecognizer () {
        if (null == faceRecognizer) {
            try {
                faceRecognizer = new FaceRecognizer(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return faceRecognizer;
    }

    public UserDb getUserDb () {
        if (null == userDb) {
            userDb = new UserDb();
            userDb.loadDbFromFile(this);
        }

        return userDb;
    }

    private String getFaceShapeModelPath() {
        File externalFilesDir = getExternalFilesDir(null);
        String targetPath = externalFilesDir.getAbsolutePath() + File.separator + "shape_predictor_68_face_landmarks.dat";
        return targetPath;
    }
}
