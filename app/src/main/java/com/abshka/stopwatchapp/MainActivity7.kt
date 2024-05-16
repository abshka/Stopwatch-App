package com.abshka.stopwatchapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.abshka.stopwatchapp.databinding.ActivityMainBinding
import java.util.*

class MainActivity7 : AppCompatActivity() {

    private var seconds = 0
    private var running = false
    private var wasRunning = false
    private var continueRunning = true

    private val handler = Handler(Looper.getMainLooper())
    private val laps = mutableListOf<String>()
    private val participants = mutableListOf<String>()

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
            participants.addAll(savedInstanceState.getStringArrayList("participants") ?: mutableListOf())
            displayLaps()
            displayParticipantResults()
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
            participants.clear()
            binding.lapContainer.removeAllViews()
            binding.participantContainer.removeAllViews()
            updateButtons()
        }

        binding.btnLap.setOnClickListener {
            addLap()
        }

        binding.btnCompetition.setOnClickListener {
            saveParticipantResult()
        }

        binding.btnShareResults.setOnClickListener {
            val shareIntent = createShareIntent()
            startActivity(shareIntent)
        }


        updateButtons()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("seconds", seconds)
        outState.putBoolean("running", running)
        outState.putBoolean("wasRunning", wasRunning)
        outState.putStringArrayList("laps", ArrayList(laps))
        outState.putStringArrayList("participants", ArrayList(participants))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        seconds = savedInstanceState.getInt("seconds")
        running = savedInstanceState.getBoolean("running")
        wasRunning = savedInstanceState.getBoolean("wasRunning")
        laps.addAll(savedInstanceState.getStringArrayList("laps") ?: mutableListOf())
        participants.addAll(savedInstanceState.getStringArrayList("participants") ?: mutableListOf())
        displayLaps()
        displayParticipantResults()
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
        binding.btnCompetition.isEnabled = running
        binding.btnShareResults.isVisible = !running && participants.isNotEmpty()
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

    private fun saveParticipantResult() {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        val participantTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)

        ParticipantNameDialog(this) { name ->
            val participantResult = "$name: $participantTime"
            participants.add(participantResult)
            displayParticipantResults()
        }.show()
    }

    private fun displayParticipantResults() {
        binding.participantContainer.removeAllViews()
        participants.forEach { result ->
            val resultTextView = TextView(this).apply {
                textSize = 18f
                text = result
            }
            binding.participantContainer.addView(resultTextView)
        }
    }

    private fun buildResultsMessage(): String {
        val messageBuilder = StringBuilder()
        messageBuilder.append("Результаты секундомера:\n\n")

        participants.forEach { result ->
            messageBuilder.append("$result\n")
        }

        messageBuilder.append("\nКруги:\n")
        laps.forEach { lap ->
            messageBuilder.append("$lap\n")
        }

        return messageBuilder.toString()
    }

    private fun createShareIntent(): Intent {
        val resultsMessage = buildResultsMessage()
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, resultsMessage)
        }
        return Intent.createChooser(shareIntent, "Поделиться результатами")
    }
}