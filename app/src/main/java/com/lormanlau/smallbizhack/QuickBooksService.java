package com.lormanlau.smallbizhack;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

public class QuickBooksService extends Service {

    final public static String QUERY_QUICKBOOKS = "QUERY_QUICKBOOKS";
    final public static String CREATE_QUICKBOOKS_ITEM = "CREATE_QUICKBOOKS_ITEM";
    final public static String UPDATE_QUICKBOOKS_ITEM = "UPDATE_QUICKBOOKS_ITEM";
    final public static String DELETE_QUICKBOOKS_ITEM = "DELETE_QUICKBOOKS_ITEM";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createLocalBroadcastReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void createLocalBroadcastReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(QUERY_QUICKBOOKS);
        filter.addAction(CREATE_QUICKBOOKS_ITEM);
        filter.addAction(UPDATE_QUICKBOOKS_ITEM);
        filter.addAction(DELETE_QUICKBOOKS_ITEM);
        LocalBroadcastManager.getInstance(this).registerReceiver(LocalQuickbooksBroadcastReceiver, filter);
    }

    private void stopLocalBroadcastReceiver(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(LocalQuickbooksBroadcastReceiver);
    }

    private BroadcastReceiver LocalQuickbooksBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case QUERY_QUICKBOOKS:
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

    private class QueryQuickBooksTask extends AsyncTask<Void, Void, Void> {

        QueryQuickBooksTask(){

        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocalBroadcastReceiver();
    }
}
