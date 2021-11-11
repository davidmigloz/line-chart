package com.davidmiguel.linechart.sample

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.davidmiguel.linechart.LineChartAdapter
import com.davidmiguel.linechart.sample.databinding.ChartDefaultActivityBinding

class ChartDefaultActivity : AppCompatActivity() {

    private lateinit var binding: ChartDefaultActivityBinding
    private val lineChartAdapter = MyAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChartDefaultActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
    }

    private fun setupView() {
        setTitle(R.string.chartDefault)
        setupLineChart()
    }

    @SuppressLint("SetTextI18n")
    private fun setupLineChart() {
        binding.lineChart.apply {
            adapter = lineChartAdapter.apply {
                setData(
                    floatArrayOf(68f, 22f, 31f, 57f, 35f, 79f, 86f, 47f, 34f, 55f, -80f, -72f, -99f, -66f, 47f, 42f, 56f, 64f, 66f, 80f, 97f, 10f, 43f, 12f, 25f, 71f, 47f, 73f, 49f, 36f)
                )
            }
        }
    }

    class MyAdapter : LineChartAdapter() {

        private var yData = floatArrayOf()

        override val count: Int
            get() = yData.size

        override fun getItem(index: Int) = yData[index]

        override fun getY(index: Int) = yData[index]

        fun setData(yData: FloatArray) {
            this.yData = yData
            notifyDataSetChanged()
        }
    }
}
