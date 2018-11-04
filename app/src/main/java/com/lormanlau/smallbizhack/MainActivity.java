package com.lormanlau.smallbizhack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final String INVENTORY_SET = "INVENTORY_SET";

    FloatingActionButton snapPictureButton;
    String mCurrentPhotoPath;
    SharedPreferences sharedPref;
    ScrollView mInventoryScrollView;
    LinearLayout mInventoryLayout;
    BroadcastReceiver localBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPref = getPreferences(Context.MODE_PRIVATE);

        snapPictureButton = (FloatingActionButton) findViewById(R.id.snapPicture);

        mInventoryScrollView = (ScrollView) findViewById(R.id.inventoryScrollView);
        mInventoryLayout = (LinearLayout) findViewById(R.id.inventoryLayout);

        snapPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        createLocalBroadcastReceiver();

        startService(new Intent(getApplicationContext(), ClarifaiService.class));
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.lormanlau.smallbizhack.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            File imgFile = new File(mCurrentPhotoPath);
            if (imgFile.exists()) {
                sendBroadcastToClarifai(ClarifaiService.PREDICT, mCurrentPhotoPath);
            }
//            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(getApplicationContext(), ClarifaiService.class).setAction("avocado"));
        }
        if (requestCode == 123 && resultCode == RESULT_OK) {
            SharedPreferences.Editor editor = sharedPref.edit();
            String itemName = data.getStringExtra("itemName");
            int itemAmount = Integer.parseInt(data.getStringExtra("itemAmount"));
            int inventoryAmount = sharedPref.getInt(itemName, 0);
            if (inventoryAmount == 0) {
                editor.putInt(itemName, itemAmount);
            } else {
                editor.putInt(itemName, inventoryAmount + itemAmount);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeLocalBroadcastReceiver();
    }

    private void sendBroadcastToClarifai(String action, String filename) {
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(getApplicationContext(), ClarifaiService.class)
                .setAction(action)
                .putExtra("filename", filename));
    }

    private void addOneChild(String name, int num) {
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.element_inventory_item, mInventoryLayout);
        ((TextView) layout.getChildAt(0)).setText(name);
        ((TextView) layout.getChildAt(1)).setText(num);
        mInventoryLayout.addView(layout);
    }

    private void populateList() {
        Set<String> set = sharedPref.getStringSet(INVENTORY_SET, null);
        if (set == null) return;
        for (String name : set) {
            int num = sharedPref.getInt(name, 0);
            addOneChild(name, num);
        }
    }

    private void consumePredictions(String[] results) {
        if (results == null) return;
        String itemName = results[0].split("/")[0];
        Intent intent = new Intent(MainActivity.this, InventoryConfirmationActivity.class);
        intent.putExtra("filePath", mCurrentPhotoPath);
        intent.putExtra("itemName", itemName);
        Log.i("SBH_MainActivity", "consumePredictions: " + itemName);
        startActivityForResult(intent, 123);
    }

    private void createLocalBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ClarifaiService.PREDICT_RESULTS);
        filter.addAction(ClarifaiService.TRAIN);
        if (localBroadcastReceiver == null)
            localBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (intent.getAction()) {
                        case ClarifaiService.PREDICT_RESULTS:
                            Log.i("SBH_MainActivity", "onReceive: " + intent.getStringArrayExtra(ClarifaiService.PREDICT));
                            consumePredictions(intent.getStringArrayExtra(ClarifaiService.PREDICT));
                            break;
                        case ClarifaiService.TRAIN:
                            break;
                        default:
                    }
                }
            };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(localBroadcastReceiver, filter);
    }

    private void removeLocalBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(localBroadcastReceiver);
    }
}
