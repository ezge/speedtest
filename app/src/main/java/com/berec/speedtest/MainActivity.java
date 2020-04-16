package com.berec.speedtest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dinuscxj.progressbar.CircleProgressBar;

import java.util.ArrayList;

import static com.berec.speedtest.ApiConfig.FIRST_ACTION;
import static com.berec.speedtest.ApiConfig.MAX_PROGRESSBAR_SIZE;
import static com.berec.speedtest.ApiConfig.SECOND_ACTION;
import static com.berec.speedtest.ApiConfig.THIRD_ACTION;


public class MainActivity extends AppCompatActivity{

    private CircleProgressBar circleProgressBar;
    private CircleProgressBar circleProgressBarShadowHide;
    private ImageView imageViewNeedle;
    private TextView textViewCurrentDownload;
    private TextView textViewCurrentUpload;
    private TextView textViewCurrentDbCPB;
    private TextView[] textViewCPBLabels = new TextView[9];
    private int[] textViewCPBLabelValues = {0, 15, 30, 45, 60, 75, 90, 105, 120};
    private RelativeLayout relativeLayoutGaugeCurrentDb;
    private Button btnStart;
    ArrayList<Double> first = new ArrayList<Double>();
    ArrayList<Double> second = new ArrayList<Double>();
    ArrayList<Double> third = new ArrayList<Double>();
    Globals g = Globals.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isOnline()){
            Toast.makeText( getBaseContext(), "No internet connection!", Toast.LENGTH_LONG ).show();
            finish();
        }

        relativeLayoutGaugeCurrentDb = (RelativeLayout)findViewById(R.id.relativeLayoutGaugeCurrentDb);
        imageViewNeedle = (ImageView)findViewById(R.id.imageViewNeedle);
        textViewCurrentDownload = (TextView)findViewById(R.id.textViewCurrentDownload);
        textViewCurrentUpload = (TextView)findViewById(R.id.textViewCurrentUpload);
        textViewCurrentDbCPB = (TextView)findViewById(R.id.textViewCurrentDbCPB);
        textViewCPBLabels[0] = (TextView)findViewById(R.id.textView0CPB);
        textViewCPBLabels[1] = (TextView)findViewById(R.id.textView20CPB);
        textViewCPBLabels[2] = (TextView)findViewById(R.id.textView30CPB);
        textViewCPBLabels[3] = (TextView)findViewById(R.id.textView50CPB);
        textViewCPBLabels[4] = (TextView)findViewById(R.id.textView60CPB);
        textViewCPBLabels[5] = (TextView)findViewById(R.id.textView70CPB);
        textViewCPBLabels[6] = (TextView)findViewById(R.id.textView90CPB);
        textViewCPBLabels[7] = (TextView)findViewById(R.id.textView100CPB);
        textViewCPBLabels[8] = (TextView)findViewById(R.id.textView120CPB);
        circleProgressBar = (CircleProgressBar)findViewById(R.id.my_cpb);
        circleProgressBarShadowHide = (CircleProgressBar)findViewById(R.id.my_cpb_shadow_hide);
        circleProgressBar.setMax(MAX_PROGRESSBAR_SIZE);
        circleProgressBarShadowHide.setMax(MAX_PROGRESSBAR_SIZE);
        circleProgressBar.setDrawBackgroundOutsideProgress(true);
        circleProgressBar.setProgressBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorBackground));
        circleProgressBar.setProgressStartColor(ContextCompat.getColor(MainActivity.this, R.color.colorCircularProgressBarBackground));
        circleProgressBar.setProgressEndColor(ContextCompat.getColor(MainActivity.this, R.color.colorCircularProgressBarBackground));
        circleProgressBar.setProgress(0);

        btnStart = (Button)findViewById(R.id.btnStart);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start downloading
                new DownloadFiles(MainActivity.this, MainActivity.this).execute();

                //new UploadFiles(MainActivity.this, MainActivity.this).execute();

            }

        });
    }

    /** starting animation **/
    public void startAnimation() {

        relativeLayoutGaugeCurrentDb.setVisibility(View.INVISIBLE);
        imageViewNeedle.setVisibility(View.INVISIBLE);
        for (int i = 0; i < 9; i++) {
            textViewCPBLabels[i].setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorBackground));
        }

        ValueAnimator animatorGauge = ValueAnimator.ofInt(0, MAX_PROGRESSBAR_SIZE);
        ValueAnimator.AnimatorUpdateListener gaugeListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                circleProgressBar.setProgress(progress);
            }
        };

        animatorGauge.setRepeatCount(0);
        animatorGauge.setDuration(400);
        animatorGauge.addUpdateListener(gaugeListener);
        animatorGauge.start();

        ValueAnimator animatorText = ValueAnimator.ofInt(0, 9);
        ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int i = (int) animation.getAnimatedValue();
                if (i >= 0 && i < 9)
                    textViewCPBLabels[i].setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorActiveGaugeText));
                if (i > 0)
                    textViewCPBLabels[i - 1].setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorNotActiveGaugeText));
                if (i == 9) {
                    imageViewNeedle.setRotation(0);
                    imageViewNeedle.setVisibility(View.VISIBLE);
                    relativeLayoutGaugeCurrentDb.setVisibility(View.VISIBLE);
                    circleProgressBar.setDrawBackgroundOutsideProgress(true);
                    circleProgressBar.setProgressBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorCircularProgressBarBackground));
                    circleProgressBar.setProgressStartColor(ContextCompat.getColor(MainActivity.this, R.color.colorTransparent));
                    circleProgressBar.setProgressEndColor(ContextCompat.getColor(MainActivity.this, R.color.colorTransparent));
                    circleProgressBar.setProgress(0);
                    circleProgressBarShadowHide.setProgress(0);
                    textViewCurrentUpload.setText(R.string.progressValue);
                    textViewCurrentDownload.setText(R.string.progressValue);
                    textViewCurrentDbCPB.setText(R.string.progressValue);
                }
            }
        };

        AnimatorListenerAdapter listener = new AnimatorListenerAdapter(){

            @Override
            public void onAnimationStart(Animator animation){
                btnStart.setText(R.string.btnWait);
                btnStart.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation){

            }
        };

        animatorText.setRepeatCount(0);
        animatorText.setDuration(400);
        animatorText.addUpdateListener(updateListener);
        animatorText.addListener(listener);
        animatorText.setStartDelay(500);
        animatorText.start();
    }

    public void simulateAnimation(final double start, final double end){
            final ValueAnimator animatorValueOnGauge = ValueAnimator.ofFloat((float) start, (float) end);
            ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float progress = (float) animation.getAnimatedValue() ;

                    circleProgressBar.setProgress((int) progress);
                    circleProgressBarShadowHide.setProgress((int) progress);

                    float rotationAngle = (float) ((progress * 131) / 60);
                    imageViewNeedle.setRotation((int) (rotationAngle));

                    textViewCurrentDbCPB.setText(String.format("%.2f", progress));

                   if (g.isDownloading())
                        textViewCurrentDownload.setText(String.format("%.2f", progress));
                    else textViewCurrentUpload.setText(String.format("%.2f", progress));

                    for (int i = 0; i < 9; i++) {
                        if (textViewCPBLabelValues[i] <= progress)
                            textViewCPBLabels[i].setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorActiveGaugeText));
                        else
                            textViewCPBLabels[i].setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorNotActiveGaugeText));
                    }
                }
            };

            AnimatorListenerAdapter listener = new AnimatorListenerAdapter(){
                @Override
                public void onAnimationEnd(Animator animation){
                    if ((!g.isDownloading()) && (!g.isUploading())){
                        btnStart.setText(R.string.btnStarting);
                        btnStart.setEnabled(true);
                        for (int i = 0; i < 9; i++) {
                            textViewCPBLabels[i].setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorNotActiveGaugeText));
                        }
                        textViewCurrentDbCPB.setText(R.string.progressValue);
                        imageViewNeedle.setRotation(0);
                        circleProgressBar.setProgress(0);
                        circleProgressBarShadowHide.setProgress(0);
                    }
                }

                @Override
                public void onAnimationStart(Animator animation){
                    if (g.isDownloading()) {
                        btnStart.setText(R.string.btnDownloading);
                    }else {
                        btnStart.setText(R.string.btnUploading);
                    }
                }
            };

            animatorValueOnGauge.setRepeatCount(0);
            animatorValueOnGauge.setDuration(800);
            animatorValueOnGauge.addUpdateListener(updateListener);
            animatorValueOnGauge.addListener(listener);
            animatorValueOnGauge.start();

    }

     private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (FIRST_ACTION.equals(intent.getAction()))
                //first = (ArrayList<Double>) intent.getSerializableExtra("1gb1.xwd");
                first.add(intent.getDoubleExtra(FIRST_ACTION, 0));
            if (SECOND_ACTION.equals(intent.getAction()))
                //second = (ArrayList<Double>) intent.getSerializableExtra("1gb2.xwd");
                second.add(intent.getDoubleExtra(SECOND_ACTION, 0));
            if (THIRD_ACTION.equals(intent.getAction()))
                //third = (ArrayList<Double>) intent.getSerializableExtra("1gb3.xwd");
                third.add(intent.getDoubleExtra(THIRD_ACTION, 0));

            double a[] = g.setSpeed(calcAvg());
            simulateAnimation(a[0], a[1]);
            g.clear();
            g.setAnimationValues();

        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FIRST_ACTION);
        intentFilter.addAction(SECOND_ACTION);
        intentFilter.addAction(THIRD_ACTION);
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(receiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    public double calcAvg(){
        if (first != null) {
            g.setData(g.avgerage(first));
        }
        if (second != null) {
            g.setData(g.avgerage(second));
        }
        if (third != null) {
            g.setData(g.avgerage(third));
        }

        return g.max();
    }

    private boolean isOnline() {
        boolean wifi = false;
        boolean mobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = cm.getAllNetworkInfo();

        for (NetworkInfo nf : networkInfo) {
            if (nf.getTypeName().equalsIgnoreCase("WIFI"))
                if (nf.isConnected())
                    wifi = true;
            if (nf.getTypeName().equalsIgnoreCase("MOBILE"))
                if (nf.isConnected())
                    mobile = true;
        }
        return wifi || mobile;
    }
}

