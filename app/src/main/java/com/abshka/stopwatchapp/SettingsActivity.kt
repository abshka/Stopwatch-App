package com.abshka.stopwatchapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abshka.stopwatchapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Загрузка сохраненных настроек
        val sharedPreferences = getSharedPreferences("stopwatch_prefs", Context.MODE_PRIVATE)
        val continueRunning = sharedPreferences.getBoolean("continueRunning", true)
        if (continueRunning) {
            binding.radioContinue.isChecked = true
        } else {
            binding.radioPause.isChecked = true
        }

        // Сохранение настроек при нажатии кнопки
        binding.btnSaveSettings.setOnClickListener {
            val selectedOption = binding.radioGroup.checkedRadioButtonId
            val editor = sharedPreferences.edit()
            editor.putBoolean("continueRunning", selectedOption == binding.radioContinue.id)
            editor.apply()

            // Возврат в MainActivity
            finish()
        }
    }
}