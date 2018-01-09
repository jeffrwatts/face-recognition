package com.skiaddict.facerecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class AddUserActivity extends AppCompatActivity {

    private static final String TAG = AddUserActivity.class.getName();

    private static int PICK_IMAGE_REQUEST = 1;

    private Button pickPhotoButton;
    private ImageView foundFaceImage;
    private EditText editUserName;
    private Button addUserButton;
    private Button skipUserButton;
    private Button loadDefaultsButton;

    Stack<Bitmap> facesToAdd = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        pickPhotoButton = (Button) findViewById(R.id.buttonPickPhoto);
        foundFaceImage = (ImageView) findViewById(R.id.imageFoundFace);
        editUserName = (EditText) findViewById(R.id.editUserName);
        addUserButton = (Button) findViewById(R.id.buttonAddUser);
        skipUserButton = (Button) findViewById(R.id.buttonSkipUser);
        loadDefaultsButton = (Button) findViewById(R.id.loadDefaults);

        pickPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editUserName.getText().toString();

                if ((null != name) && (!TextUtils.isEmpty(name))) {
                    Bitmap bitmap = facesToAdd.pop();
                    FaceRecognizer faceRecognizer = ((FaceRecognizerApplication)(getApplication())).getFaceRecognizer();
                    Embedding embedding = faceRecognizer.generateEmbedding(bitmap);
                    UserDb userDb = ((FaceRecognizerApplication)(getApplication())).getUserDb();
                    userDb.addUser(new User(name, embedding));
                    updateUI();
                    Toast.makeText(AddUserActivity.this, "Number of users: " + userDb.getNumberOfUsers(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddUserActivity.this, "Enter Name", Toast.LENGTH_SHORT).show();
                }

            }
        });

        skipUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facesToAdd.pop();
                updateUI();
            }
        });

        loadDefaultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDefaultsButton.setEnabled(false);
                loadDefaultUsers();
                loadDefaultsButton.setEnabled(true);
            }
        });

        updateUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                // Work around "Invalid SOS Parameters" when picture is from Samsung phone.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                processImage(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUI () {
        boolean facesOnStack = !facesToAdd.isEmpty();
        pickPhotoButton.setEnabled(!facesOnStack);
        addUserButton.setEnabled(facesOnStack);
        skipUserButton.setEnabled(facesOnStack);

        if (true == facesOnStack) {
            Bitmap bitmap = facesToAdd.peek();
            // Scale up to make it visible.
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()*4, bitmap.getHeight()*4, true);

            foundFaceImage.setVisibility(View.VISIBLE);
            foundFaceImage.setImageBitmap(scaledBitmap);
        } else {
            foundFaceImage.setVisibility(View.GONE);
        }
    }

    private void processImage (Bitmap bitmap) {
        FaceDet faceDetector = ((FaceRecognizerApplication)(getApplication())).getFaceDetector();
        FaceAligner faceAligner = ((FaceRecognizerApplication)(getApplication())).getFaceAligner();

        List<VisionDetRet> faces = faceDetector.detect(bitmap);

        for (VisionDetRet face : faces) {
            ArrayList<Point> landmarks = face.getFaceLandmarks();
            Log.i(TAG, "Landmarks: " + landmarks.size());
            Bitmap alignedFace = faceAligner.alignFace(bitmap, face.getFaceLandmarks());
            facesToAdd.push(alignedFace);
        }
        updateUI();
    }

    private Bitmap loadBitmapFromAsset(String filePath) {
        Bitmap bitmap = null;
        try {
            InputStream inputStream = getAssets().open(filePath);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
        }

        return bitmap;
    }

    private void addDefaultUser (String name, String filePath) {
        FaceDet faceDetector = ((FaceRecognizerApplication)(getApplication())).getFaceDetector();
        FaceAligner faceAligner = ((FaceRecognizerApplication)(getApplication())).getFaceAligner();
        FaceRecognizer faceRecognizer = ((FaceRecognizerApplication)(getApplication())).getFaceRecognizer();
        UserDb userDb = ((FaceRecognizerApplication)(getApplication())).getUserDb();
        Bitmap bitmap = loadBitmapFromAsset(filePath);

        List<VisionDetRet> faces = faceDetector.detect(bitmap);

        if (1 == faces.size()) {
            VisionDetRet face = faces.get(0);
            ArrayList<Point> landmarks = face.getFaceLandmarks();
            Bitmap alignedFace = faceAligner.alignFace(bitmap, face.getFaceLandmarks());

            if (null != alignedFace) {
                Embedding embedding = faceRecognizer.generateEmbedding(alignedFace);
                userDb.addUser(new User(name, embedding));
            }
        }
    }

    private void loadDefaultUsers () {
        UserDb userDb = ((FaceRecognizerApplication)(getApplication())).getUserDb();
        userDb.removeAll();
        addDefaultUser("Andrew Steinmetz", "andrew.jpg");
        addDefaultUser("Andy Sheehan", "andy.jpg");
        addDefaultUser("Ben Daschel", "ben.jpg");
        addDefaultUser("Jeff Watts", "jeff.jpg");
        addDefaultUser("Ryan Bruels", "ryan.jpg");
        addDefaultUser("Zahra Sedghinasab", "zahra.jpg");
        addDefaultUser("Carl Tydingco", "carl.jpg");
        addDefaultUser("Ionut Burete", "ionut.jpg");
        addDefaultUser("Andy Scearce", "andys.jpg");
        addDefaultUser("David Brunelle", "dave.jpg");
        userDb.saveDbToFile(this);
        Toast.makeText(this, "Number of users: " + userDb.getNumberOfUsers(), Toast.LENGTH_LONG).show();
    }
}
