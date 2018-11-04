package com.lormanlau.smallbizhack;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.JsonNull;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.request.ClarifaiPaginatedRequest;
import clarifai2.api.request.ClarifaiRequest;
import clarifai2.api.request.model.Action;
import clarifai2.api.request.model.GetModelRequest;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.Model;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import clarifai2.dto.prediction.Prediction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ClarifaiService extends Service {
    private final static String TAG = "SBH_ClarifaiService";
    private final static String TEMP_CLARIFAI_KEY = "ad1b5678586946c9b84addffe7e2d749";
    private final static String CLARIFAI_API_KEY = TEMP_CLARIFAI_KEY;
    public static final String PREDICT = "PREDICT";
    public static final String PREDICT_RESULTS = "PREDICT_RESULTS";
    public static final String TRAIN = "TRAIN";
    public static final String TRAIN_RESULT = "TRAIN_RESULT";
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
    private String trainConcept;

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
                                Log.i(TAG, (((List<ClarifaiOutput<Concept>>) o).toString()));
                            }
                        }, predictFailure);
            }
        };

        trainSuccess = new ClarifaiRequest.OnSuccess<Model<?>>() {
            @Override
            public void onClarifaiResponseSuccess(Model<?> model) {

            }
        };

        predictFailure = new ClarifaiRequest.OnFailure() {
            @Override
            public void onClarifaiResponseUnsuccessful(int errorCode) {
                Log.e(TAG, "prediction failure: " + errorCode);
            }
        };

        trainFailure = new ClarifaiRequest.OnFailure() {
            @Override
            public void onClarifaiResponseUnsuccessful(int errorCode) {
                Log.e(TAG, "train failure :" + errorCode);
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
        Log.i(TAG, imageFileName);
        getModel().executeAsync(modelToPredict, noModelFound);
    }

    private void modelTrain() {
        getModel().executeAsync(modelToTrain, noModelFound);
    }

    private GetModelRequest getModel() {
        return client.getModelByID("test");
    }

    private void testTrain() {
        client.getModelByID("test").executeAsync(new ClarifaiRequest.OnSuccess<Model<?>>() {
            @Override
            public void onClarifaiResponseSuccess(Model<?> model) {
                model.asConceptModel().modify().withConcepts(Action.MERGE, Concept.forID(trainConcept)).executeAsync(new ClarifaiRequest.OnSuccess<ConceptModel>() {

                    @Override
                    public void onClarifaiResponseSuccess(ConceptModel conceptModel) {
                        client.addInputs().plus(
                                ClarifaiInput.forImage(new File(imageFileName)).withConcepts(Concept.forID(trainConcept))
                        ).executeAsync(new ClarifaiRequest.OnSuccess<List<ClarifaiInput>>() {

                            @Override
                            public void onClarifaiResponseSuccess(List<ClarifaiInput> clarifaiInputs) {
                                client.trainModel("test").executeAsync(new ClarifaiRequest.OnSuccess<Model<?>>() {

                                    @Override
                                    public void onClarifaiResponseSuccess(Model<?> model) {
                                        Log.i(TAG, "successfully retrained with new item of type " + trainConcept);
                                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(getApplicationContext(), ClarifaiService.class)
                                        .setAction(TRAIN_RESULT));
                                    }
                                }, new ClarifaiRequest.OnFailure() {
                                    @Override
                                    public void onClarifaiResponseUnsuccessful(int errorCode) {
                                        Log.e(TAG, "failed to retrain model: " + errorCode);
                                    }
                                });
                            }

                        }, new ClarifaiRequest.OnFailure() {
                            @Override
                            public void onClarifaiResponseUnsuccessful(int errorCode) {
                                Log.e(TAG, "client failed to add new images: " + errorCode);
                            }
                        });
                    }
                }, new ClarifaiRequest.OnFailure() {
                    @Override
                    public void onClarifaiResponseUnsuccessful(int errorCode) {
                        Log.e(TAG, "failed to merge new concept: " + errorCode);
                    }
                });
            }
        }, new ClarifaiRequest.OnFailure() {
            @Override
            public void onClarifaiResponseUnsuccessful(int errorCode) {
                Log.e(TAG, "get model failed: " + errorCode);
            }
        });
    }

    private void testPredict() {
        client.predict("test").withInputs(
                ClarifaiInput.forImage(new File(imageFileName))
        ).executeAsync(new ClarifaiRequest.OnSuccess<List<ClarifaiOutput<Prediction>>>() {
            @Override
            public void onClarifaiResponseSuccess(List<ClarifaiOutput<Prediction>> clarifaiOutputs) {
                Log.i(TAG, clarifaiOutputs.toString());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(getApplicationContext(), MainActivity.class)
                        .setAction(PREDICT_RESULTS)
                        .putExtra(PREDICT, parseOutputs(clarifaiOutputs)));
            }
        }, new ClarifaiRequest.OnFailure() {
            @Override
            public void onClarifaiResponseUnsuccessful(int errorCode) {
                Log.e(TAG, "test predict fail: " + errorCode);
            }
        });
    }

    private String[] parseOutputs(List<ClarifaiOutput<Prediction>> clarifaiOutputs) {
        List<Prediction> list = clarifaiOutputs.get(0).data();
        String[] result = new String[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            Concept concept = (Concept) list.get(i);
            result[i] = concept.name() + "/" + concept.value();
        }
        return result;
    }

    private void createLocalBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("avocado");
        filter.addAction(PREDICT);
        filter.addAction(TRAIN);

        if (localBroadcastReceiver == null)
            localBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (intent.getAction()) {
                        case "avocado":
                            mockClarifaiPredict();
                            break;
                        case PREDICT:
                            imageFileName = intent.getStringExtra("filename");
                            testPredict();
                            break;
                        case TRAIN:
                            imageFileName = intent.getStringExtra("filename");
                            trainConcept = intent.getStringExtra("concept");
                            testTrain();
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