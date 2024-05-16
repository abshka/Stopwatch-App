package com.abshka.stopwatchapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.abshka.stopwatchapp.databinding.ActivityMainBinding
import java.util.*

class MainActivity4 : AppCompatActivity() {

    private var seconds = 0
    private var running = false
    private var wasRunning = false
    private var continueRunning = true

    private val handler = Handler(Looper.getMainLooper())
    private val laps = mutableListOf<String>()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Загрузка настроек
        val sharedPreferences = getSharedPreferences("stopwatch_prefs", MODE_PRIVATE)
        continueRunning = sharedPreferences.getBoolean("continueRunning", true)

        // Восстановление состояния
        if (savedInstanceState != null) {
            seconds = savedInstanceState.getInt("seconds")
            running = savedInstanceState.getBoolean("running")
            wasRunning = savedInstanceState.getBoolean("wasRunning")
            laps.addAll(savedInstanceState.getStringArrayList("laps") ?: mutableListOf())
            displayLaps()
        }

        runTimer()

        binding.btnStart.setOnClickListener {
            running = true
            updateButtons()
        }

        binding.btnStop.setOnClickListener {
            running = false
            updateButtons()
        }

        binding.btnReset.setOnClickListener {
            running = false
            seconds = 0
            laps.clear()
            binding.lapContainer.removeAllViews()
            updateButtons()
        }

        binding.btnLap.setOnClickListener {
            addLap()
        }

        updateButtons()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("seconds", seconds)
        outState.putBoolean("running", running)
        outState.putBoolean("wasRunning", wasRunning)
        outState.putStringArrayList("laps", ArrayList(laps))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        seconds = savedInstanceState.getInt("seconds")
        running = savedInstanceState.getBoolean("running")
        wasRunning = savedInstanceState.getBoolean("wasRunning")
        laps.addAll(savedInstanceState.getStringArrayList("laps") ?: mutableListOf())
        displayLaps()
    }

    override fun onPause() {
        super.onPause()
        wasRunning = running
        if (!continueRunning) {
            running = false
        }
    }

    override fun onResume() {
        super.onResume()
        // Загрузка настроек при возобновлении активности
        val sharedPreferences = getSharedPreferences("stopwatch_prefs", MODE_PRIVATE)
        continueRunning = sharedPreferences.getBoolean("continueRunning", true)

        if (wasRunning) {
            running = true
        }
    }

    private fun runTimer() {
        val runnable = object : Runnable {
            override fun run() {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60

                val time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
                binding.tvTimer.text = time

                if (running) {
                    seconds++
                }

                handler.postDelayed(this, 1000)
            }
        }

        handler.post(runnable)
    }

    private fun updateButtons() {
        binding.btnStart.isEnabled = !running
        binding.btnStop.isEnabled = running
        binding.btnReset.isEnabled = seconds > 0 && !running
        binding.btnLap.isEnabled = running
    }

    private fun addLap() {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        val lapTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
        laps.add(lapTime)
        if (laps.size > 5) {
            laps.removeAt(0)
        }
        displayLaps()
        seconds = 0 // Обнуление переменной seconds после добавления круга
    }

    private fun displayLaps() {
        binding.lapContainer.removeAllViews()
        laps.forEach { lap ->
            val lapTextView = TextView(this).apply {
                textSize = 18f
                text = lap
            }
            binding.lapContainer.addView(lapTextView)
        }
    }
}