package com.davidmiguel.linechart.sample;

import android.os.Bundle;

import com.davidmiguel.linechart.LineChartAdapter;
import com.davidmiguel.linechart.LineChartFillType;
import com.davidmiguel.linechart.sample.databinding.ActivityMainBinding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class MainActivity extends AppCompatActivity {

    private static final float[] DATA_ALL = new float[]{68, 22, 31, 57, 35, 79, 86, 47, 34, 55, 80, 72, 99, 66, 47, 42, 56, 64, 66, 80, 97, 10, 43, 12, 25, 71, 47, 73, 49, 36};
    private static final float[] DATA_YEAR = new float[]{1, 10, 31, 32, 35, 79, 86, 47, 34, 55, 50, 50, 30, 39, 40, 20, 29, 39, 30, 30, 39, 38, 20, 18, 10, 12, 13, 10, 12, 11};
    private static final float[] DATA_MONTH = new float[]{0, 1, 2, 3, 4, 5, 6, 7};

    private ActivityMainBinding binding;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        adapter = new MyAdapter();
        binding.lineChart.setAdapter(adapter);
        binding.lineChart.setScrubListener(value -> binding.total.setText(value != null ? Float.toString((float) value) : ""));
        binding.lineChart.setFillType(LineChartFillType.DOWN);
//        MorphLineChartAnimator morphSparkAnimator = new MorphLineChartAnimator();
//        morphSparkAnimator.setDuration(2000L);
//        morphSparkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
//        binding.lineChart.setLineChartAnimator(morphSparkAnimator);

        binding.allBtn.setOnClickListener(v -> adapter.setData(DATA_ALL));
        binding.yearBtn.setOnClickListener(v -> adapter.setData(DATA_YEAR));
        binding.monthBtn.setOnClickListener(v -> adapter.setData(DATA_MONTH));
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

        public void setData(float[] yData) {
            this.yData = yData;
            notifyDataSetChanged();
        }
    }
}
