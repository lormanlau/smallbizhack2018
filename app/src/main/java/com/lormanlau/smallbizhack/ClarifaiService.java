package com.lormanlau.smallbizhack;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.request.ClarifaiRequest;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

import java.util.List;


public class ClarifaiService extends Service {
    private final static String TAG = "SBH_ClarifaiService";
    private final static String TEMP_CLARIFAI_KEY = "ad1b5678586946c9b84addffe7e2d749";
    private final static String CLARIFAI_API_KEY = TEMP_CLARIFAI_KEY;
    private static BroadcastReceiver localBroadcastReceiver;
    private ClarifaiClient client;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");

        client = new ClarifaiBuilder(CLARIFAI_API_KEY)
                .buildSync();

        createLocalBroadcastReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");

        removeLocalBroadcastReceiver();
    }

    private void mockClarifaiPredict() {
        client.getDefaultModels().generalModel() // You can also do client.getModelByID("id") to get your custom models
                .predict()
                .withInputs(
                        ClarifaiInput.forImage("https://samples.clarifai.com/metro-north.jpg"))
                .executeAsync(new ClarifaiRequest.OnSuccess<List<ClarifaiOutput<Concept>>>() {
                    @Override
                    public void onClarifaiResponseSuccess(List<ClarifaiOutput<Concept>> clarifaiOutputs) {
                        Log.i(TAG, clarifaiOutputs.toString());
                    }
                });
    } // mock clarifai predict

    private void createLocalBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("avocado");
        if (localBroadcastReceiver == null)
            localBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (intent.getAction()) {
                        case "avocado":
                            mockClarifaiPredict();
                            break;
                        default:
                    }
                }
            };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(localBroadcastReceiver, filter);
    } // broadcast receiver

    private void removeLocalBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(localBroadcastReceiver);
    } // broadcast receiver


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}