package com.davidmiguel.linechart.sample;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.davidmiguel.linechart.LineChartFillType;
import com.davidmiguel.linechart.LineChartAdapter;
import com.davidmiguel.linechart.LineChartView;
import com.davidmiguel.linechart.animation.MorphLineChartAnimator;
import com.davidmiguel.linechart.touch.OnScrubListener;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private LineChartView lineChartView;
    private RandomizedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lineChartView = findViewById(R.id.line_chart);

        adapter = new RandomizedAdapter();
        lineChartView.setAdapter(adapter);
        lineChartView.setScrubListener(value -> Log.d(TAG, "onScrubbed: " + value));

        findViewById(R.id.random_button).setOnClickListener(view -> adapter.randomize());

        lineChartView.setFillType(LineChartFillType.DOWN);
        MorphLineChartAnimator morphSparkAnimator = new MorphLineChartAnimator();
        morphSparkAnimator.setDuration(2000L);
        morphSparkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        lineChartView.setLineChartAnimator(morphSparkAnimator);
    }

    public static class RandomizedAdapter extends LineChartAdapter {
        private final float[] yData;
        private final Random random;

        RandomizedAdapter() {
            random = new Random();
            yData = new float[500 + random.nextInt(100)];
            randomize();
        }

        void randomize() {
            for (int i = 0, count = yData.length; i < count; i++) {
                yData[i] = 50F + random.nextInt(50);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return yData.length;
        }

        @NonNull
        @Override
        public Object getItem(int index) {
            return yData[index];
        }

        @Override
        public float getY(int index) {
            return yData[index];
        }

        @Override
        public boolean hasBaseLine() {
            return true;
        }

        @Override
        public float getBaseLine() {
            return 0;
        }
    }
}
