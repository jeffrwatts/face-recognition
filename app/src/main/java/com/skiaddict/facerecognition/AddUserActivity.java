package com.skiaddict.facerecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;

public class AddUserActivity extends AppCompatActivity {

    private static int PICK_IMAGE_REQUEST = 1;

    private Button pickPhotoButton;
    private ImageView foundFaceImage;
    private EditText editUserName;
    private Button addUserButton;
    private Button skipUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        pickPhotoButton = (Button) findViewById(R.id.buttonPickPhoto);
        foundFaceImage = (ImageView) findViewById(R.id.imageFoundFace);
        editUserName = (EditText) findViewById(R.id.editUserName);
        addUserButton = (Button) findViewById(R.id.addUser);
        skipUserButton = (Button) findViewById(R.id.buttonSkipUser);

        pickPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }}
