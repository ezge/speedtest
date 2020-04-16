package com.berec.speedtest;

import java.util.ArrayList;

public class Globals {
        private boolean downloading = false;
        private boolean uploading = false;
        private static Globals instance;
        private ArrayList<Double> arrayAvg = new ArrayList<Double>();
        private double[] a = new double[2];

        // Restrict the constructor from being instantiated
        private Globals(){}

        public static synchronized Globals getInstance(){
            if(instance==null){
                instance=new Globals();
            }
            return instance;
        }

        public void setData(double d){
            this.arrayAvg.add(d);
        }

        public void setDownload(boolean flag){
            this.downloading = flag;
        }

        public void setUpload(boolean flag){
            this.uploading = flag;
        }

        public boolean isDownloading(){return this.downloading;}

        public boolean isUploading(){return this.uploading;}

        public void clear(){
            this.arrayAvg.clear();
        }


        public double max(){
            double sum = 0;
            for (Double item : this.arrayAvg){
                sum += item;
            }

            return sum;
        }

        public double avgerage(ArrayList<Double> array){
            double sum = 0.0;
            int count = 0;
            for (Double item : array) {
                sum += item;
                count++;
            }
            return (count > 0) ? (double)(sum/count) : 0.0;
        }

        public double[] setSpeed(double speed){
            a[1] = speed;

            return a;
        }

        public void setAnimationValues(){
            a[0] = a[1];
        }

        public void clearAnimationValues(){
            a[0] = 0;
            a[1] = 0;
        }
}
