package com.davidmiguel.linechart.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.davidmiguel.linechart.sample.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView(binding)
    }

    private fun setupView(binding: MainActivityBinding) {
        binding.chartDefault.setOnClickListener {
            startActivity(Intent(this, ChartDefaultActivity::class.java))
        }
        binding.chartCustom.setOnClickListener {
            startActivity(Intent(this, ChartCustomActivity::class.java))
        }
        binding.chartAnimation.setOnClickListener {
            startActivity(Intent(this, ChartAnimationActivity::class.java))
        }
    }
}
