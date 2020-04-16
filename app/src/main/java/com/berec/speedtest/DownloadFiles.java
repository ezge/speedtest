package com.berec.speedtest;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

public class DownloadFiles extends AsyncTask<Void, Void, Void> {
        private Context context;
        private ConnManager connManager;
        private MainActivity activity;
        Globals g;

        public DownloadFiles(MainActivity activity, Context context) {
            this.context = context;
            this.connManager = new ConnManager(context);
            this.activity = activity;
            g = Globals.getInstance();
        }

        //Executes in UI thread before task begins
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            activity.startAnimation();
            g.setDownload(true);
            g.setUpload(false);

        }

        //Runs in a background thread
        @Override
        protected Void doInBackground(Void... params) {

            connManager.startDownload();

            return null;
        }

        //runs on UI thread
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // start uploading with delay of 6 sec
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new UploadFiles(activity, context).execute();
                }
            }, 4000);


        }


}
