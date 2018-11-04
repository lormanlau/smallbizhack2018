package com.lormanlau.smallbizhack;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QuickBooksService extends Service {
    public static final String TAG = "SBH_Quickbooks";
    final public static String QUERY_QUICKBOOKS = "QUERY_QUICKBOOKS";
    final public static String CREATE_QUICKBOOKS_ITEM = "CREATE_QUICKBOOKS_ITEM";
    final public static String UPDATE_QUICKBOOKS_ITEM = "UPDATE_QUICKBOOKS_ITEM";
    final public static String DELETE_QUICKBOOKS_ITEM = "DELETE_QUICKBOOKS_ITEM";
    private static String API_KEY;
    private static String REFRESH_TOKEN;
    private static String REALMID = "123146164371189";
    private static String url = "https://sandbox-quickbooks.api.intuit.com";
    private static String path = "/v3/company/" + REALMID;
    private static String baseurl = url + path;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createLocalBroadcastReceiver();
        SharedPreferences sp = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        API_KEY = sp.getString("API_KEY", "eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiZGlyIn0..IPqP3BfRb6HCLL0Uf32jzg.yT6aEoKdFRjlP5P76Jcm3yHx_vB4HeUiE00Wt4nduSr6U6BkGqtS7_J4q1Q_9z4Gw0NU1ScckP1KLg3fBkvD-w2K98wLeh15ib9CW8waaa1osnvliFzALoZL6Gw0ofJV8sBwpPhamq6ViGuJEI_MKurcoanfxTxJcE7S-5eu9x6A2zCVgVG3YKuM9phu6I8yOqdMfMCgXdxaNnk9rLGa4UXMUlSNybkxd6F-zEphZH2AVo50OIPbhiIiB-54MRMggHMTwaFU0eZY0yJslVPTtbPP-cEEluHLOHeFlxK5oropGNTfcoewZ2GHdO5WKF2YT20ZUIA2BJ0cYgzGioNxxlTZS559RgViCuAtvDqw6ZXYnDdXv7DMsmSZiiYzQth2zFbkqE9sQH4vellarBVUdLJchlLyya6Xd2fqaNA0td6-lMJjY0vOVGvox5jc6qNVFSUeAD-DcBoWKFN0lZ5OV5491OO-WZ3yanU3JNp_QR34_OE9bITJtvwWnh8nhq1Bh5MjaUdEQKvr1Y0EJkQPwBFoKvKHlfcJ4j41vDR-wnSbHeOYXbHewqBMPUP0ot_gIPaE-Q3Vv9f0nIqXi8woKPtO7cIEZIRjv2LSTXqcwaC7eAVU_YmEIIGAqMEJqWRg5wq3rvtrnbcr2P5T4P_Xb3qNPuAJqzZlCWF0WyJ9QyBEggnkh59_aIQuI3v91Mb_ehF6J5A2M1KZ2VO5a1oQGz1KOMpkMZPWNyZIK6XHOb_98wnxq6Ub2I-WbBebAc2l7RCZR8OwdRa7vk5qxX6n1Go6CGMB6Bpz06Kztw56ENYkTki_MT0eF907xv3ttWPqWFPM4-k3zHava0wAE3_jLNSGPWRfP1ir89dNRd_FwC9V2ygTAzZLpE52JXq3TRwZ.mcG1z9LmELW3Pt-Z34aExw");
        REFRESH_TOKEN = sp.getString("REFRESH_TOKEN", "L011550085485n4x7NQFcsB0bLsGxHpLpmWvHpne3Zu9a47TWk");
        Log.i(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private RequestQueue requestQueue() {
        return Volley.newRequestQueue(this);
    }

    private void queryAll() {
        //todo hardcoded limit of 10
        Log.i(TAG, "enter queryAll");

//        JsonObjectRequest stringRequest = new JsonObjectRequest(baseurl + "/query", null,
//                new Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        // Display the first 500 characters of the response string.
//                        Log.i(TAG, "Response is: " + response.toString());
//                    }
//
//
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                if (error.networkResponse != null) {
//                    if (error.networkResponse.allHeaders != null) {
//                        Iterator<Header> it = error.networkResponse.allHeaders.iterator();
//                        while (it.hasNext()) {
//                            Log.e(TAG, it.next().toString());
//                        }
//                    }
//                    Log.e(TAG, "" + error.networkResponse.statusCode);
//                    try {
//                        Log.e(TAG, "VolleyError: " + new String(error.networkResponse.data, "UTF-8"));
//                    } catch (Exception e) {
//                        Log.e(TAG, "VolleyError error failed");
//                    }
//                }
//                Log.e(TAG, "VolleyError: ", error);
//            }
//        }) {
//            //This is for Headers If You Needed
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("Content-Type", "application/json; charset=UTF-8");
//                params.put("Authorization", "Bearer " + API_KEY);
//                return params;
//            }
//
//            //Pass Your Parameters here
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("query", "Select%20*%20from%20Item%20maxresults%202");
//                return params;
//            }
//        };

                StringRequest stringRequest = new StringRequest(Request.Method.GET, baseurl + "/query",
                new Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.i(TAG, "Response is: " + response.toString());
                    }


                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    if (error.networkResponse.allHeaders != null) {
                        Iterator<Header> it = error.networkResponse.allHeaders.iterator();
                        while (it.hasNext()) {
                            Log.e(TAG, it.next().toString());
                        }
                    }
                    Log.e(TAG, "" + error.networkResponse.statusCode);
                    try {
                        Log.e(TAG, "VolleyError: " + new String(error.networkResponse.data, "UTF-8"));
                    } catch (Exception e) {
                        Log.e(TAG, "VolleyError error failed");
                    }
                }
                Log.e(TAG, "VolleyError: ", error);
            }
        }) {
            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Bearer " + API_KEY);
                return params;
            }

            //Pass Your Parameters here
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("query", "Select%20*%20from%20Item%20maxresults%202");
                params.put("minorversion", "4");
                return params;
            }
        };

        requestQueue().add(stringRequest);
    }

    private void createLocalBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(QUERY_QUICKBOOKS);
        filter.addAction(CREATE_QUICKBOOKS_ITEM);
        filter.addAction(UPDATE_QUICKBOOKS_ITEM);
        filter.addAction(DELETE_QUICKBOOKS_ITEM);
        LocalBroadcastManager.getInstance(this).registerReceiver(LocalQuickbooksBroadcastReceiver, filter);
    }

    private void stopLocalBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(LocalQuickbooksBroadcastReceiver);
    }

    private BroadcastReceiver LocalQuickbooksBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case QUERY_QUICKBOOKS:
                    queryAll();
                    break;
                case CREATE_QUICKBOOKS_ITEM:
                    break;
                case UPDATE_QUICKBOOKS_ITEM:
                    break;
                case DELETE_QUICKBOOKS_ITEM:
                    break;
            }
        }
    };

//    private class QueryQuickBooksTask extends AsyncTask<Void, Void, Void> {
//
//        QueryQuickBooksTask() {
//
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            return null;
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocalBroadcastReceiver();
    }
}
