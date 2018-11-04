package com.lormanlau.smallbizhack;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.request.ClarifaiRequest;
import clarifai2.api.request.model.GetModelRequest;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.Model;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

import java.io.File;
import java.util.List;


public class ClarifaiService extends Service {
    private final static String TAG = "SBH_ClarifaiService";
    private final static String TEMP_CLARIFAI_KEY = "ad1b5678586946c9b84addffe7e2d749";
    private final static String CLARIFAI_API_KEY = TEMP_CLARIFAI_KEY;
    private BroadcastReceiver localBroadcastReceiver;
    private ClarifaiClient client;
    private ClarifaiRequest.OnFailure noModelFound;
    private ClarifaiRequest.OnSuccess<Model<?>> modelToPredict;
    private ClarifaiRequest.OnSuccess<Model<?>> modelToTrain;
    private ClarifaiRequest.OnSuccess<List<ClarifaiOutput<Concept>>> predictSuccess;
    private ClarifaiRequest.OnSuccess<Model<?>> trainSuccess;
    private ClarifaiRequest.OnFailure predictFailure;
    private ClarifaiRequest.OnFailure trainFailure;
    private String imageFileName;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");

        client = new ClarifaiBuilder(CLARIFAI_API_KEY)
                .buildSync();

        noModelFound = new ClarifaiRequest.OnFailure() {
            @Override
            public void onClarifaiResponseUnsuccessful(int errorCode) {
                Log.e(TAG, "Model Get Error: " + errorCode);
            }
        };

        modelToPredict = new ClarifaiRequest.OnSuccess<Model<?>>() {
            @Override
            public void onClarifaiResponseSuccess(Model<?> model) {
                model.predict().withInputs(
                        ClarifaiInput.forImage(new File(imageFileName)))
                        .executeAsync(new ClarifaiRequest.OnSuccess() {
                            @Override
                            public void onClarifaiResponseSuccess(Object o) {
                                Log.i(TAG, (((List<ClarifaiOutput<Concept>>)o).toString()));
                            }
                        }, predictFailure);
            }
        };

        modelToTrain = new ClarifaiRequest.OnSuccess<Model<?>>() {
            @Override
            public void onClarifaiResponseSuccess(Model<?> model) {
                model.train().executeAsync(trainSuccess, trainFailure);
            }
        };

//        predictSuccess = new ClarifaiRequest.OnSuccess<List<ClarifaiOutput<Concept>>>() {
//            @Override
//            public void onClarifaiResponseSuccess(List<ClarifaiOutput<Concept>> clarifaiOutputs) {
//                Log.i(TAG, clarifaiOutputs.toString());
//            }
//        };

        trainSuccess = new ClarifaiRequest.OnSuccess<Model<?>>() {
            @Override
            public void onClarifaiResponseSuccess(Model<?> model) {

            }
        };

        predictFailure = new ClarifaiRequest.OnFailure() {
            @Override
            public void onClarifaiResponseUnsuccessful(int errorCode) {

            }
        };

        trainFailure = new ClarifaiRequest.OnFailure() {
            @Override
            public void onClarifaiResponseUnsuccessful(int errorCode) {

            }
        };

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

    private void modelPredict() {
        getModel().executeAsync(modelToPredict, noModelFound);
    }

    private void modelTrain() {
        getModel().executeAsync(modelToTrain, noModelFound);
    }

    private GetModelRequest getModel() {
        return client.getModelByID("MyInventoryModel");
    }

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