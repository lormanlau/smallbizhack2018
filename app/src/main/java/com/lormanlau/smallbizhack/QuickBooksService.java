package com.lormanlau.smallbizhack;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QuickBooksService extends Service {
    public static final String TAG = "SBH_Quickbooks";
    final public static String QUERY_QUICKBOOKS = "QUERY_QUICKBOOKS";
    final public static String CREATE_QUICKBOOKS_ITEM = "CREATE_QUICKBOOKS_ITEM";
    final public static String UPDATE_QUICKBOOKS_ITEM = "UPDATE_QUICKBOOKS_ITEM";
    final public static String DELETE_QUICKBOOKS_ITEM = "DELETE_QUICKBOOKS_ITEM";
    final public static String QUERY_RESUTLS = "QUERY_RESULTS";
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
        API_KEY = sp.getString("API_KEY", "eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiZGlyIn0..R-FVO3Qsqr2GfKWvOCijBw.hQNsXyAn8MHjVRU2veVLvZNWHDVA1uSCSOo_EolJWDwHAywLi6zPHPXEgNr8Eu7qRst2wzXAFzu0sdHd7EdRbbtpryBfzD3ia6lhgAXfZTNVigTrClv4RmIuqw4jt_ELcFeqDwtBF6tPHYqLcX4Tsu2XdHoE6IhwZvS6PFcwKkjjQ-lOlN2ammYy78WFXFvsZ7WOB78lW3Y4gvQPyOOC5Ql8nbR_npTImHrJR3jF_TR0pDp4TTz1uxQk_2a7L7qXdusKXegTv-WIvfWFyXz1QmBOYf5ycwX7gaplzK_wy2Ae0JdZyzaFu6iEMxHfOIS8Bb3JO36s8Hxpl607I-oyvsus850v8lCZQ68dod-Lk-dusGH3JO-N8zAcDTfSupLVvPsTo4_6sJaMadWcLAFD0NyAztgRIEQ9gA530uz3NXvifBvYlfzgI_AWGKgO_XMTbuXukz0z0hW92KvMHAB8-ler_9oaLATnTHaWGuWKrlYqLqH1o8i9Hq3q8Rr2hQlTbx8pxnmQko-p0i2XPnKLygbNGqiLffgRv1eMLX2ByfyHPyi73ayb1RAGmsan_HUEznzSC22u4gu6Xt2dOZcZnbVfKHNa-bAXzLV0mRJ-TdNoBB9FPpObPoOV8RrakJOUQ6NARib0GFCeaDJF895Xg4QZfe2YsWmkPDUt5wEsWkoCp3VQvnf86nhycIX-RWQoGQF2jbfs_lxic3OPb7i_xzqap02pSo_bJn9i6a-sunIq5Wi5k3FzmawW20DPtqgGA56WmXFGJ7MWcGHwP8dVhx7avC0jzAXXceKv1KGKUmrzlpq5bKpUI78TTP2YjZKn1ItBk9FNLfJh9aHDf_1PqiFYkGuiJ6S8y3F44w7F-mbqFLaU8vE_r22t_UHl1rF5.dlfUxv8zVtJUQTS9Ayun-A");
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

        JsonObjectRequest stringRequest = new JsonObjectRequest("https://sandbox-quickbooks.api.intuit.com/v3/company/123146164371189/query?query=select%20%2a%20from%20Item%20maxresults%205&minorversion=4", null,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Log.i(TAG, "Response is: " + response.toString());
                        sendJSONback(response.toString());
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
                params.put("Accept", "application/json");
                params.put("Authorization", "Bearer " + API_KEY);
                return params;
            }

//            //Pass Your Parameters here
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("query", "Select%20*%20from%20Item%20maxresults%202");
//                return params;
//            }
        };

//                StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://sandbox-quickbooks.api.intuit.com/v3/company/123146164371189/query?query=select%20%2a%20from%20Item%20maxresults%2010&minorversion=4",
//                new Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
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
////            @Override
////            protected Map<String, String> getParams() {
////                Map<String, String> params = new HashMap<String, String>();
////                params.put("query", "select * from Item maxresults 2");
////                params.put("minorversion", "4");
////                return params;
////            }
//        };

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

    private void sendJSONback(String json){
        Intent intent = new Intent(QUERY_RESUTLS);
        intent.putExtra("jsonObject", json);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocalBroadcastReceiver();
    }
}
