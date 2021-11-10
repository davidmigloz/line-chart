package com.davidmiguel.linechart.sample

import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.davidmiguel.linechart.LineChartAdapter
import com.davidmiguel.linechart.LineChartFillType
import com.davidmiguel.linechart.animation.MorphLineChartAnimator
import com.davidmiguel.linechart.formatter.DefaultYAxisValueFormatter
import com.davidmiguel.linechart.formatter.EuroYAxisValueFormatter
import com.davidmiguel.linechart.formatter.PercentYAxisValueFormatter
import com.davidmiguel.linechart.formatter.YAxisValueFormatter
import com.davidmiguel.linechart.sample.databinding.ActivityMainBinding
import com.davidmiguel.linechart.touch.OnScrubListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val lineChartAdapter = MyAdapter()
    private val defaultFormatter: YAxisValueFormatter = DefaultYAxisValueFormatter()
    private val percentFormatter: YAxisValueFormatter = PercentYAxisValueFormatter()
    private val euroFormatter: YAxisValueFormatter = EuroYAxisValueFormatter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupView()
    }

    private fun setupView() {
        setupLineChart()
        setupButtons()
    }

    private fun setupLineChart() {
        binding.lineChart.apply {
            adapter = lineChartAdapter
            scrubListener = OnScrubListener { value: Any? ->
                binding.total.text = value?.toString() ?: ""
            }
            fillType = LineChartFillType.DOWN
            lineChartAnimator = MorphLineChartAnimator().apply {
                duration = 2000L
                interpolator = AccelerateDecelerateInterpolator()
            }
            yAxisValueFormatter = defaultFormatter
        }
    }

    private fun setupButtons() {
        binding.allBtn.setOnClickListener {
            binding.lineChart.yAxisValueFormatter = defaultFormatter
            lineChartAdapter.setData(DATA_ALL)
        }
        binding.yearBtn.setOnClickListener {
            binding.lineChart.yAxisValueFormatter = percentFormatter
            lineChartAdapter.setData(DATA_YEAR)
        }
        binding.monthBtn.setOnClickListener {
            binding.lineChart.yAxisValueFormatter = euroFormatter
            lineChartAdapter.setData(DATA_MONTH)
        }
    }

    class MyAdapter : LineChartAdapter() {
        private var yData = DATA_ALL

        override fun getCount() =  yData.size

        override fun getItem(index: Int) = yData[index]

        override fun getY(index: Int) = yData[index]

        fun setData(yData: FloatArray) {
            this.yData = yData
            notifyDataSetChanged()
        }
    }

    companion object {
        private val DATA_ALL = floatArrayOf(1f, 10f, 31f, 32f, 35f, 79f, 86f, 47f, 34f, 55f, 50f, 50f, 30f, 39f, 40f, 20f, 29f, 39f, 30f, 30f, 39f, 38f, 20f, 18f, 10f, 12f, 13f, 10f, 12f, 36f)
        private val DATA_YEAR = floatArrayOf(47f, 50f, 75f, 100f, 101f, 79f, 86f, 47f, 34f, 55f, 50f, 50f, 30f, 39f, 40f, 20f, 29f, 39f, 30f, 30f, 39f, 38f, 20f, 18f, 10f, 12f, 13f, 10f, 12f, 36f)
        private val DATA_MONTH = floatArrayOf(68f, 22f, 31f, 57f, 35f, 79f, 86f, 47f, 34f, 55f, 80f, 72f, 99f, 66f, 47f, 42f, 56f, 64f, 66f, 80f, 97f, 10f, 43f, 12f, 25f, 71f, 47f, 73f, 49f, 36f)
    }
}
