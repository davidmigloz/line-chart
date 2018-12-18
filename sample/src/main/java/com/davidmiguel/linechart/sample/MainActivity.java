package com.davidmiguel.linechart.sample;

import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.davidmiguel.linechart.LineChartAdapter;
import com.davidmiguel.linechart.LineChartFillType;
import com.davidmiguel.linechart.animation.MorphLineChartAnimator;
import com.davidmiguel.linechart.formatter.DefaultYAxisValueFormatter;
import com.davidmiguel.linechart.formatter.EuroYAxisValueFormatter;
import com.davidmiguel.linechart.formatter.PercentYAxisValueFormatter;
import com.davidmiguel.linechart.formatter.YAxisValueFormatter;
import com.davidmiguel.linechart.sample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final float[] DATA_ALL = new float[]{1, 10, 31, 32, 35, 79, 86, 47, 34, 55, 50, 50, 30, 39, 40, 20, 29, 39, 30, 30, 39, 38, 20, 18, 10, 12, 13, 10, 12, 36};
    private static final float[] DATA_YEAR = new float[]{47, 50, 75, 100, 101, 79, 86, 47, 34, 55, 50, 50, 30, 39, 40, 20, 29, 39, 30, 30, 39, 38, 20, 18, 10, 12, 13, 10, 12, 36};
    private static final float[] DATA_MONTH = new float[]{68, 22, 31, 57, 35, 79, 86, 47, 34, 55, 80, 72, 99, 66, 47, 42, 56, 64, 66, 80, 97, 10, 43, 12, 25, 71, 47, 73, 49, 36};

    private ActivityMainBinding binding;
    private MyAdapter adapter;
    private YAxisValueFormatter defaultFormatter = new DefaultYAxisValueFormatter();
    private YAxisValueFormatter percentFormatter = new PercentYAxisValueFormatter();
    private YAxisValueFormatter euroFormatter = new EuroYAxisValueFormatter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        adapter = new MyAdapter();
        binding.lineChart.setAdapter(adapter);
        binding.lineChart.setScrubListener(value -> binding.total.setText(value != null ? Float.toString((float) value) : ""));
        binding.lineChart.setFillType(LineChartFillType.DOWN);
        MorphLineChartAnimator morphSparkAnimator = new MorphLineChartAnimator();
        morphSparkAnimator.setDuration(2000L);
        morphSparkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        binding.lineChart.setLineChartAnimator(morphSparkAnimator);
        binding.lineChart.setYAxisValueFormatter(defaultFormatter);

        binding.allBtn.setOnClickListener(v -> {
            binding.lineChart.setYAxisValueFormatter(defaultFormatter);
            adapter.setData(DATA_ALL);
        });
        binding.yearBtn.setOnClickListener(v -> {
            binding.lineChart.setYAxisValueFormatter(percentFormatter);
            adapter.setData(DATA_YEAR);
        });
        binding.monthBtn.setOnClickListener(v -> {
            binding.lineChart.setYAxisValueFormatter(euroFormatter);
            adapter.setData(DATA_MONTH);
        });
    }

    public static class MyAdapter extends LineChartAdapter {

        private float[] yData = DATA_ALL;

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

        void setData(float[] yData) {
            this.yData = yData;
            notifyDataSetChanged();
        }
    }
}
