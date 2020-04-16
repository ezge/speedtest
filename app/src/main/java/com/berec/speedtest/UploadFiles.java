package com.berec.speedtest;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import java.util.ArrayList;

public class UploadFiles extends AsyncTask<Void, Void, Void>{
        private Context context;
        private ConnManager connManager;
        private MainActivity activity;
        Globals g;

        public UploadFiles(MainActivity activity, Context context) {
            this.context = context;
            this.connManager = new ConnManager(context);
            this.activity = activity;
            g = Globals.getInstance();
        }

        //Executes in UI thread before task begins
        @Override
        protected void onPreExecute() {

            g.setUpload(true);
            g.setDownload(false);
            g.clearAnimationValues();
            activity.first.clear();
            activity.second.clear();
            activity.third.clear();

        }

        //Runs in a background thread
        @Override
        protected Void doInBackground(Void... params) {

            connManager.startUpload();

            return null;
        }

        //runs on UI thread
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            g.setUpload(false);

        }
}
