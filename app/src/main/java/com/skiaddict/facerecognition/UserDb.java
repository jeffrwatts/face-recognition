package com.skiaddict.facerecognition;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;

import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jewatts on 1/6/18.
 */

public class UserDb {

    private static final String TAG = UserDb.class.getName();
    private static final String DB_FILENAME = "userDb.csv";
    private static final int USER_FIELD_LENGTH = 129;
    private static final float MIN_THRESHOLD = 0.86f;

    private ArrayList<User> userDb = new ArrayList<>();

    public int getNumberOfUsers() {
        return userDb.size();
    }

    public void addUser (User user) {
        userDb.add(user);
    }

    public void removeAll () {
        userDb.clear();
    }

    public String recognizeFace (Embedding embedding) {
        String user = "";
        String result = "No Match";
        double minDistance = 100;

        for (User userIx : userDb) {
            double distance = userIx.embedding.computeDistance(embedding);

            Log.i(TAG, userIx.name + "distance: " + distance);
            if (distance < minDistance) {
                minDistance = distance;
                user = userIx.name;
            }
        }

        if (minDistance < MIN_THRESHOLD) {
            result = user + " (" + Math.round(minDistance * 1000) / 1000.0f + ")";
        }

        return result;
    }

    public void loadDbFromFile(Context context) {
        File file = new File(context.getExternalFilesDir(null), DB_FILENAME);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] rowData = line.split(",");

                if (rowData.length == USER_FIELD_LENGTH) {
                    userDb.add(User.fromFileRow(rowData));
                } else {
                    Log.e(TAG, "User Data Validation Failed.  length = " + rowData.length);
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "userDb.csv file not found: " + e.getLocalizedMessage());
        } catch (IOException e) {
            Log.e(TAG, "Failed to read line from userDb.csv: " + e.getLocalizedMessage());
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to close userDb.csv reader: " + e.getLocalizedMessage());
            }
        }
    }

    public void saveDbToFile(Context context) {

        File file = new File(context.getExternalFilesDir(null), DB_FILENAME);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (User userIx : userDb) {
                bufferedWriter.write(userIx.toFileRow());
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fileWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close userDb.csv writer: " + e.getLocalizedMessage());
        }
    }
}
