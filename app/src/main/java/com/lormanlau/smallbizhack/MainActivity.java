package com.lormanlau.smallbizhack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final String INVENTORY_SET = "INVENTORY_SET";
    final private String TAG = "SBH_MainActivity";

    FloatingActionButton snapPictureButton;
    String mCurrentPhotoPath;
    SharedPreferences sharedPref;
    SwipeRefreshLayout mInventoryRefreshView;
    LinearLayout mInventoryLayout;
    BroadcastReceiver localBroadcastReceiver;
    ProgressBar mLoadingCircle;

    boolean isLoading = false;

    int max_limit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPref = getPreferences(Context.MODE_PRIVATE);

        snapPictureButton = (FloatingActionButton) findViewById(R.id.snapPicture);

        mLoadingCircle = (ProgressBar) findViewById(R.id.loadingCircle);

        mInventoryRefreshView = (SwipeRefreshLayout) findViewById(R.id.inventoryScrollView);
        mInventoryLayout = (LinearLayout) findViewById(R.id.inventoryLayout);

        mInventoryRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sendBroadcastToQuickbooks(QuickBooksService.QUERY_QUICKBOOKS, null);
                mInventoryRefreshView.setRefreshing(false);
            }
        });

        snapPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        max_limit = 0;

        createLocalBroadcastReceiver();
        startService(new Intent(getApplicationContext(), ClarifaiService.class));
        startService(new Intent(getApplicationContext(), QuickBooksService.class));
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
                isLoading = true;
            }
//            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(getApplicationContext(), ClarifaiService.class).setAction("avocado"));
        } else if (requestCode == 123 && resultCode == RESULT_OK) {
            SharedPreferences.Editor editor = sharedPref.edit();
            String itemName = data.getStringExtra("itemName");
            int itemAmount = Integer.parseInt(data.getStringExtra("itemAmount"));
            int inventoryAmount = sharedPref.getInt(itemName, 0);
            if (inventoryAmount == 0) {
                Set<String> set = sharedPref.getStringSet(INVENTORY_SET, new HashSet<String>());
                set.add(itemName);
                editor.putStringSet(INVENTORY_SET, set);
            }
            editor.putInt(itemName, inventoryAmount + itemAmount);
            editor.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateList();
        if (isLoading) {
            snapPictureButton.hide();
            mLoadingCircle.setVisibility(View.VISIBLE);
        } else {
            snapPictureButton.show();
            mLoadingCircle.setVisibility(View.GONE);
        }
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

    private void sendBroadcastToQuickbooks(String action, String param) {
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(getApplicationContext(), QuickBooksService.class)
                .setAction(action)
                .putExtra("param", param));
    }

    private void addOneChild(String name, int num) {
        LinearLayout parent = (LinearLayout) getLayoutInflater().inflate(R.layout.element_inventory_item, null);
        CardView child = (CardView) parent.getChildAt(0);
        ((TextView) child.getChildAt(0)).setText(name);
        ((TextView) child.getChildAt(1)).setText(String.valueOf(num));
        mInventoryLayout.addView(parent);
    }

    private void replaceOneChild(String name, int num, int counter) {
        LinearLayout parent = (LinearLayout) mInventoryLayout.getChildAt(counter);
        CardView child = (CardView) parent.getChildAt(0);
        ((TextView) child.getChildAt(0)).setText(name);
        ((TextView) child.getChildAt(1)).setText(String.valueOf(num));
    }

    private void populateList() {
        Set<String> set = sharedPref.getStringSet(INVENTORY_SET, null);
        if (set == null) return;
        int counter = 0;
        for (String name : set) {
            int num = sharedPref.getInt(name, 0);
            if (max_limit < set.size()) {
                addOneChild(name, num);
                max_limit++;
            } else {
                replaceOneChild(name, num, counter);
            }
            counter++;
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
        filter.addAction(QuickBooksService.QUERY_RESUTLS);
        if (localBroadcastReceiver == null)
            localBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (intent.getAction()) {
                        case ClarifaiService.PREDICT_RESULTS:
                            Log.i("SBH_MainActivity", "onReceive: " + intent.getStringArrayExtra(ClarifaiService.PREDICT));
                            isLoading = false;
                            consumePredictions(intent.getStringArrayExtra(ClarifaiService.PREDICT));
                            break;
                        case ClarifaiService.TRAIN:
                            break;
                        case QuickBooksService.QUERY_RESUTLS:
                            parseJsonData(intent.getStringExtra("jsonObject"));
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

    private void parseJsonData(String json){
        mInventoryRefreshView.setRefreshing(false);
        try {
            JSONObject mJSONObject = new JSONObject(json);
            Log.i(TAG, "parseJsonData: " + mJSONObject.toString());
            JSONObject query_response = mJSONObject.getJSONObject("QueryResponse");
            JSONArray items = query_response.getJSONArray("Item");
            int counter = 0;
            Set<String> set = sharedPref.getStringSet(INVENTORY_SET, null);
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String name = item.getString("Name");
                int amount = 0;
                if (item.has("QtyOnHand")) {
                     amount = item.getInt("QtyOnHand");
                }
                if (max_limit < set.size() + items.length()) {
                    addOneChild(name, amount);
                    max_limit++;
                } else {
                    replaceOneChild(name, amount, counter);
                }
                counter++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
