package com.namespace.labs;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import ai.fritz.core.Fritz;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.FritzVisionImage;

import ai.fritz.vision.FritzVisionObject;
import ai.fritz.vision.ModelVariant;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictor;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationResult;
import ai.fritz.vision.imagesegmentation.MaskClass;
import ai.fritz.vision.imagesegmentation.SegmentationOnDeviceModel;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictor;
import ai.fritz.vision.objectdetection.FritzVisionObjectResult;
import ai.fritz.vision.objectdetection.ObjectDetectionOnDeviceModel;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    ImageView imageView;
    Button buttonClick;

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fritz.configure(this, "YOUR_API_KEY");
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        buttonClick = findViewById(R.id.buttonClick);
        buttonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){

                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else {
                        pickImage();
                    }
                }
                else {
                    pickImage();
                }
            }
        });
    }
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                if (grantResults.length >0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                    pickImage();
                }
                else {
                    Toast.makeText(this, "Permission not Granted", Toast.LENGTH_LONG).show();
                }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            Uri selectedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA}; // fix this
            Cursor c = getContentResolver()
                    .query(selectedImage, filePath, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePath[0]);
            String picturePath = c.getString(columnIndex);
            c.close();
            Bitmap image = (BitmapFactory.decodeFile(picturePath));

            // For fast
            SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getLivingRoomSegmentationOnDeviceModel(ModelVariant.FAST);
            FritzVisionSegmentationPredictor predictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel);

            FritzVisionImage visionImage = FritzVisionImage.fromBitmap(image);

            FritzVisionSegmentationResult segmentationResult = predictor.predict(visionImage);
            // Create a mask
            Bitmap sofaMask = segmentationResult.buildSingleClassMask(MaskClass.SOFA);
            Bitmap imageWithMask = visionImage.overlay(sofaMask);
            imageView.setImageBitmap(imageWithMask);




        }
    }
}

