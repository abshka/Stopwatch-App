package com.abshka.stopwatchapp

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class ParticipantNameDialog(context: Context, private val onNameEntered: (String) -> Unit) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_participant_name)

        val etParticipantName: EditText = findViewById(R.id.etParticipantName)
        val btnSave: Button = findViewById(R.id.btnSave)

        btnSave.setOnClickListener {
            val name = etParticipantName.text.toString().trim()
            if (name.isNotEmpty()) {
                onNameEntered(name)
                dismiss()
            } else {
                etParticipantName.error = "Введите имя участника"
            }
        }
    }
}